;    Copyright (C) 2017-2019, 2023  Joseph Fosco. All Rights Reserved
;
;    This program is free software: you can redistribute it and/or modify
;    it under the terms of the GNU General Public License as published by
;    the Free Software Foundation, either version 3 of the License, or
;    (at your option) any later version.
;
;    This program is distributed in the hope that it will be useful,
;    but WITHOUT ANY WARRANTY; without even the implied warranty of
;    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;    GNU General Public License for more details.
;
;    You should have received a copy of the GNU General Public License
;    along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns splice.player.player-play-note
  (:require
   [clojure.core.async :refer [>!! <! alts! chan go put! take! timeout]]
   [sc-osc.sc :refer [sc-deref!
                      sc-event
                      sc-next-id
                      sc-now
                      sc-on-event
                      sc-oneshot-sync-event
                      sc-remove-event-handler
                      sc-send-bundle
                      sc-send-msg
                      sc-uuid ]]
   [splice.instr.instrumentinfo :refer [get-instrument-from-instrument-info
                                        get-note-off-from-instrument-info]]
   [splice.instr.sc-instrument :refer [get-release-millis-from-instrument
                                       sched-control-val
                                       sched-gate-off]]
   [splice.config.constants :refer [SAVED-MELODY-LEN]]
   [splice.ensemble.ensemble :refer [get-ensemble-clear-msg-for-player-id
                                     get-melody-for-player-id-from-ensemble
                                     get-melody-for-player-id
                                     get-player
                                     update-melody-note-off-for-player-id
                                     update-player-and-melody]]
   [splice.melody.dur-info :refer [get-dur-millis-from-dur-info]]
   [splice.melody.melody-event :refer [get-dur-info-from-melody-event
                                       get-dur-millis-from-melody-event
                                       get-event-time-from-melody-event
                                       get-freq-from-melody-event
                                       get-instrument-info-from-melody-event
                                       get-instrument-settings-from-melody-event
                                       get-melody-event-id-from-melody-event
                                       get-note-off-from-melody-event
                                       get-player-id-from-melody-event
                                       get-play-time-from-melody-event
                                       get-sc-synth-id-from-melody-event
                                       get-volume-from-melody-event
                                       set-play-info]]
   [splice.music.music :refer [midi->hz]]
   [splice.player.player-utils :refer [get-final-rest-event
                                       get-loop-name
                                       get-next-melody-event
                                       NEXT-METHOD]]
   [splice.sc.groups :refer [get-instrument-group-id]]
   [splice.sc.sc-constants :refer [tail]]
   [splice.util.log :as log]
   [splice.util.random :refer [random-int]]
   [splice.util.settings :refer [get-setting]]
   [splice.util.util :refer [get-msg-channel]]
   ))

(def ^:private num-players-stopped (atom 0))
(def ^:private cancel-control-chan (chan))  ; Control channel to cansel pending melody-events
(def ^:private cancel-response-chan (chan)) ; Channel to receive msgs acknowledging processing
                                            ;   of cancel msgs

(def NEXT-NOTE-PROCESS-MILLIS 200)
(def synth-melody-map (atom {}))


;; **** SPLICE SHUTDOWN PROCESS ****
;; The following 5 methods (mae-cancel-msg, cancel-pending-melody-event,
;; check-ivstrument-group, and process-cancel-response-msg, stop-scheduling)
;; are used in shutting down splice.
;; 1- On init, player-play-note will register a one-shot-sync-event to call
;;    stop-scheduling when a :stop-player-scheduling ecent is posted. Also,
;;    it executes a take! to park on the cancel-response-chan and listening for a msg
;;    on the cancel-response-chan to call process-cancel-msg if a msg is received
;; 2- :stop-player-scheduing is posted when splice-stop or splice-pause are executed
;;    from the repl (main.clj)
;; 3- stop-scheduling calls cansel-pending-melody-event which calls make-cancel-msg
;;    and it places this msg on the cancel-control-chan
;; 4- Every time a next note is scheduled, the go block is listening to the
;;    cancel-control-chan as well as the timeout. If it receives a msg on the
;;    cancel-control-chan the timeout is cancelled. Also, the cancel-msg is
;;    updated and placed on the cancel-response-chsn.
;; 5- The msg placed on the cancel-response-chan is picked up by the take! and
;;    process-cancel-response-msg is called.
;; 6- process-cancel-response-msg tracks the number of times it is called. If it
;;    has not been called at least once for each player (recorded in settings
;;    :number-of-players)
;; 7- Each time process-cancel-response-msg is called it executes a take! on the
;;    cancel-response-chan and then calls cancel-pending-melody-event to place a
;;    new msg on the cancel-control-chan to stop another player.
;; 8- This process is repeated from step 5 until all scheduled events for all players
;;    have been cancelled
;; 9 - When all scheduled events have been cancelled, process-cancel-response-msg
;;     checks supercollider to make certain there are no synths playing (using the OSC
;;     /g_gueryTree command. The cpommand is set up to check the instrument-group
;;     when it returns 0 as the number of synths in the group it is ok to complete
;;     the shutdown porcess by posting a :player-scheduling-stopped msg

(defn make-cancel-msg
  []
  {:content :cancel
   :player-id nil
   :status :pending}
  )

(defn cancel-pending-melody-event
  []
  (put! cancel-control-chan (make-cancel-msg))
  )

(defn check-instruent-group
  []
  (let [p (promise)
        key (sc-uuid)
        res (sc-oneshot-sync-event "/g_queryTree.reply"
                                   (fn [info]
                                     (deliver p info)
                                     :sc-osc/remove-handler)
                                   key)
        query-vals (do (sc-send-msg "/g_queryTree"
                                    (get-instrument-group-id)
                                    0)
                       (:args (sc-deref! p
                                         (str "attempting to get response from queryTree in SHUTDOWN "))))
        ]
    ;; returns the number of items in the instrument group
    (nth query-vals 2)
    )
  )

(defn process-cancel-response-msg
  [msg]
  (swap! num-players-stopped inc)
  (cond
    (< @num-players-stopped (get-setting :number-of-players))
    (do
      (take! cancel-response-chan process-cancel-response-msg)
      (cancel-pending-melody-event)
      )
    (= @num-players-stopped (get-setting :number-of-players))
    (do
      (println "*** SHUTDOWN *** player-play-note.clj/process-cancel-response-msg -"
               "stopped player schedulingv for"
               (get-setting :number-of-players)
               "players....")

      (let [recursion-counter (atom 0)]
        (loop []
          (if (not= 0 (check-instruent-group))
            (do
              (Thread/sleep 2000)
              (if (> (swap! recursion-counter inc) 10)
                (log/warn (str"player-play-note.clj/process-cancel-response-msg - "
                              "Recuring " @recursion-counter " times waiting for instrument "
                              "groups to clear")))
              (recur)
              )))
        )

      (sc-event :player-scheduling-stopped)
      (reset! num-players-stopped 0)
      )
    )
  )

(defn stop-scheduling
  " sets a flag to stop sched-next-note scheduling notes"
  [event]
  (println "*** SHUTDOWN *** player-play-note.clj/stop-scheduling -"
           "stopping player scheduling for"
           (get-setting :number-of-players)
           "players....")
  (cancel-pending-melody-event)
  )

(declare sched-release)
(defn init-player-play-note
  []
  (swap! synth-melody-map empty)
  ;; The ::go-key is not removed when splice is stopped. This is ok becaue if we start again
  ;; it will not add another handler, instead, the existing one will be replaced with this
  ;; identical one.
  (sc-on-event "/n_go" sched-release ::go-key)
  (take! cancel-response-chan process-cancel-response-msg)
  (sc-oneshot-sync-event :stop-player-scheduling stop-scheduling (sc-uuid))
  )

(defn update-melody-with-event
  [melody melody-event]
  ;; Adds melody-event to the end of the melody vector
  ;; Does not allow melody-vector to have more than SAVED-MELODY-LEN
  ;; elements in it
  (if (>= (get-melody-event-id-from-melody-event melody-event) SAVED-MELODY-LEN)
    (assoc (subvec melody 1) (dec SAVED-MELODY-LEN) melody-event)
    (assoc melody (get-melody-event-id-from-melody-event melody-event) melody-event)
    ))

(defn check-prior-event-note-off
   " if the prior note was not turned off and
       either this note is a rest or
         this note has a different instrument than the prior note
     then
       turn off the prior note

     First this function checks if the prior-melody-event has
     :note-off nil and sc-synth-id <not-nil (some number)>
     If this is the case it means that the prior event is an instrument that
     is going to play, but supercollider has not yet started or notified this program
     that the instrument has started to play. If that is the case it is not yet possible
     to send a message to supercollider to schedule the gate-off. So we delay and recur
     until a value other than nill exists for note-off.

     The delay is set to be NEXT-NOTE-PROCESS-MILLIS because this is likely what is
     holding up the creation and notification of the instrument.

     What is likely happening is that the time for the prior note has not yet arrived and the
     next note is already being created due to this NEXT-NOTE-PROCESS-MILLIS. This function
     needs to wait till the prior instrument has been created before we can tell if it needs
     to set a note off for its instrument.
  "
  [cur-player-id melody-event]
  (loop [player-id cur-player-id
         prior-melody-event-ndx (if (>= (get-melody-event-id-from-melody-event melody-event)
                                        SAVED-MELODY-LEN)
                                  (- SAVED-MELODY-LEN 1)
                                  (- (get-melody-event-id-from-melody-event melody-event) 1))
         prior-melody-event (nth (get-melody-for-player-id player-id) prior-melody-event-ndx)
         cur-melody-event melody-event
         recur-count 0
         ]
    (if (and (nil? (get-note-off-from-melody-event prior-melody-event))
             (get-sc-synth-id-from-melody-event prior-melody-event))
      (do
        (println "recur-count: " recur-count "player-ud: " player-id)
        (Thread/sleep (/ NEXT-NOTE-PROCESS-MILLIS 2.5))
        (when (> recur-count 0)
          (log/warn "player_play_note.clj/check-prior-event-note-off - "
                    "%%%%%%%% ABOUT TO RECUR time number " recur-count
                    " for player-id: " player-id " %%%%%%%%"))
        (recur player-id
               prior-melody-event-ndx
               (nth (get-melody-for-player-id player-id) prior-melody-event-ndx)
               cur-melody-event
               (inc recur-count)))
      ;; If the note-off for the prior-melody-event is true, then a gate-off event
      ;; has already been scheduled for the prior-melody-event. If it is false, we need to
      ;; check if the current event is a different instrument than the prior event
      ;; (sc-synth-id different in events) or if the current event is a rest (freq=nil).
       (if (and (false? (get-note-off-from-melody-event prior-melody-event))
                (or (nil? (get-freq-from-melody-event cur-melody-event))
                    (not=
                     (get-sc-synth-id-from-melody-event prior-melody-event)
                     (get-sc-synth-id-from-melody-event cur-melody-event)
                     )
                    )
                )
         (sched-gate-off (get-sc-synth-id-from-melody-event prior-melody-event)
                         (+ (get-play-time-from-melody-event prior-melody-event)
                            (get-dur-millis-from-melody-event prior-melody-event)))
         )))
  )

(defn send-gate-off
  [sc-synth-id melody-event play-time]
  (if (get-note-off-from-instrument-info
       (get-instrument-info-from-melody-event melody-event))
    (let [player-id (get-player-id-from-melody-event melody-event)
          melody-event-id (get-melody-event-id-from-melody-event melody-event)
          release-millis (get-release-millis-from-instrument sc-synth-id)
          note-off-val (> (get-dur-millis-from-melody-event melody-event) release-millis)
          ]
      (if note-off-val
        (sched-gate-off sc-synth-id
                        (- (+ play-time (get-dur-millis-from-dur-info
                                         (get-dur-info-from-melody-event melody-event)))
                           release-millis))
        )
      note-off-val
      )
    )
  )

(defn sched-release
  " This method is called whe supercollider sends a /n_go message indicating that
  a new supercollider synth has been started. This call is set up at the top
  of this file in init_player_play_note. If the melody event for this synth indicates
  that a gate-off does not need to be scheduled in supercollider
  (melody-event :note-off=true) than nothing is done. Otherwise, a gate-off event
  is sheduled if appropriate and the melody event is updated with note-off=true.
  "
  [event]
  (log/debug "***** sched-release event: " event)
  (if (= (nth (event :args) 4) 0)  ;; 0 means this is a synth
      (let [sc-synth-id (first (event :args))
            melody-event (get @synth-melody-map sc-synth-id)
            ]
        (when (and melody-event
                   (not (true? (get-note-off-from-melody-event melody-event))))
          (let [player-id (get-player-id-from-melody-event melody-event)
                melody-event-id (get-melody-event-id-from-melody-event melody-event)
                play-time (get-play-time-from-melody-event melody-event)
                note-off-val (send-gate-off sc-synth-id melody-event play-time)]
            (update-melody-note-off-for-player-id player-id melody-event-id note-off-val)
            (swap! synth-melody-map dissoc sc-synth-id)))))
  nil
  )

(defn play-note-prior-instrument
  [prior-melody-event melody-event play-time]
  (let [sc-synth-id (get-sc-synth-id-from-melody-event prior-melody-event)]
    (apply sched-control-val sc-synth-id
           play-time
           "freq" (get-freq-from-melody-event melody-event)
           "vol" (get-volume-from-melody-event melody-event)
           (get-instrument-settings-from-melody-event melody-event)
           )
    (let [note-off-val (send-gate-off sc-synth-id melody-event play-time)]
      [sc-synth-id note-off-val])
    )
  )

(defn play-note-new-instrument
  [melody-event play-time]
  (let [sc-synth-id (sc-next-id :node)]
    ;; Need to use apply here to unpack the args from get-instrument-settings-from-melody-event
    (sc-send-bundle play-time
                    (apply sc-send-msg
                           "/s_new"
                           (get-instrument-from-instrument-info
                            (get-instrument-info-from-melody-event melody-event))
                           sc-synth-id
                           tail
                           (get-instrument-group-id)
                           "freq" (get-freq-from-melody-event melody-event)
                           "vol" (* (get-volume-from-melody-event melody-event)
                                    (get-setting :volume-adjust))
                           (get-instrument-settings-from-melody-event melody-event)))
    sc-synth-id
    )
  )

(defn play-melody-event
  [prior-melody-event melody-event play-time]
  (let [synth-type
        (cond (nil? (get-freq-from-melody-event melody-event)) nil ;; This is a rest
              ;; Need to use (not (false? here because note-off can be true or nil
              ;; if true at this point, it means the prior event has handled the note-off.
              ;; If nil at this point, the prior instrument will not have a note-off
              ;; scheduled, and this note should be scheduled with the prior instrument.
              (not (false? (get-note-off-from-melody-event prior-melody-event))) :new
              :else :prior
              )]
    (let [[sc-synth-id note-off-val]
          (cond (nil? synth-type) [nil :none]
                (= synth-type :new) [(play-note-new-instrument melody-event play-time) :none]
                :else (play-note-prior-instrument prior-melody-event melody-event play-time)
                )
          full-melody-event (set-play-info melody-event
                                           sc-synth-id
                                           play-time
                                           note-off-val)
          ]
      (when (= :new synth-type)  ;; Do not add :prior to synth-map
        (swap! synth-melody-map assoc sc-synth-id full-melody-event))

      (log/debug "play-melody-event: " (pr-str full-melody-event))
      full-melody-event
      )
    ))

(declare sched-next-note)
(defn play-next-note
  [player-id sched-time]
  (log/info "--- start player-id: " player-id "---")
  (let [event-time sched-time
        play-time (+ sched-time NEXT-NOTE-PROCESS-MILLIS)
        ;; TODO document why we are clearing messages here
        [ensemble player-msgs] (get-ensemble-clear-msg-for-player-id player-id)
        player (get-player ensemble player-id)
        melody (get-melody-for-player-id-from-ensemble ensemble player-id)
        [upd-player next-melody-event] (get-next-melody-event ensemble
                                                              player
                                                              melody
                                                              player-id
                                                              event-time)
        upd-melody-event (play-melody-event (last melody)
                                            next-melody-event
                                            play-time)
        upd-melody (update-melody-with-event melody upd-melody-event)
        ]
    (log/debug player-id " "
             (get-loop-name player) " "
             (if (get-freq-from-melody-event upd-melody-event)
               ""
               "REST"))
    (check-prior-event-note-off player-id upd-melody-event)
    (update-player-and-melody upd-player upd-melody player-id)
    (sched-next-note upd-melody-event)
    (>!! (get-msg-channel) {:msg :melody-event
                            :data upd-melody-event
                            :time (System/currentTimeMillis)})
    (log/debug "end:   " player-id  " melody-event: " (:melody-event-id upd-melody-event) "\n\n\n")

    ))

(defn sched-next-note
  [melody-event]
  (when-let [d-info (get-dur-info-from-melody-event melody-event)]
    (let [event-time (get-event-time-from-melody-event melody-event)
          next-time (+ event-time (get-dur-millis-from-dur-info d-info))
          timeout-ms (- (get-dur-millis-from-dur-info d-info)
                        (- (System/currentTimeMillis) event-time))
          next-melody-event-chan (timeout timeout-ms)
          player-id (get-player-id-from-melody-event melody-event)
          ]
      (go
        (let [result (alts! [next-melody-event-chan cancel-control-chan])]
          (if (= (second result) cancel-control-chan)
            (do
              (println "*** SHUTDOWN *** player-play-note.clj/sched-next-note -"
                       "Stopping next-melody-event thread for player-id:"
                       player-id)
              (put! cancel-response-chan (assoc (first result) :status :processed :player-id player-id))
              )
            (play-next-note (get-player-id-from-melody-event melody-event) next-time)))
        ))
    ))

(defn play-first-note
  [player-id min-time-offset max-time-offset]
  (let [delay-millis (+ (random-int (* min-time-offset 1000)
                                    (* max-time-offset 1000)))
        note-time (+ (sc-now) delay-millis)
        next-melody-event-chan (timeout delay-millis)
        ]
    (go
      (let [result (alts! [next-melody-event-chan cancel-control-chan])]
        (if (= (second result) cancel-control-chan)
          (do
            (println "*** SHUTDOWN *** player-play-note.clj/play-first-note -"
                     "Stopping first next-melody-event thread for player-id:"
                     player-id)
            (put! cancel-response-chan (assoc (first result) :status :processed :player-id player-id))
            )
          (play-next-note player-id note-time)))
      )
    ))
