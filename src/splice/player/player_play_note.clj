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
   [clojure.core.async :refer [>!! <! go timeout]]
   [sc-osc.sc :refer [sc-next-id sc-on-event sc-send-bundle sc-send-msg sc-uuid ]]
   [splice.instr.instrumentinfo :refer [get-instrument-from-instrument-info
                                        get-note-off-from-instrument-info]]
   [splice.instr.sc-instrument :refer [get-release-millis-from-instrument
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
                                       get-sc-instrument-id-from-melody-event
                                       get-volume-from-melody-event
                                       set-play-info]]
   [splice.music.music :refer [midi->hz]]
   [splice.player.player-utils :refer [get-loop-name
                                       get-next-melody-event
                                       NEXT-METHOD]]
   [splice.sc.groups :refer [base-group-ids*]]
   [splice.sc.sc-constants :refer [tail]]
   [splice.util.log :as log]
   [splice.util.settings :refer [get-setting]]
   [splice.util.util :refer [get-msg-channel]]
   ))

(def NEXT-NOTE-PROCESS-MILLIS 200)
(def synth-melody-map (atom {}))

(declare sched-release)
(defn init-player-play-note
  []

  (swap! synth-melody-map empty)
  (sc-on-event "/n_go" sched-release (sc-uuid))

  )

(defn is-playing?
 "Returns:
   true - if player is playing now
   false - if player is not playing now
 "
 [player]
  )

(defn update-melody-with-event
  [melody melody-event]
  ;; Adds melody-event to the end of the melody vector
  ;; Does not allow melody-vector to have more than SAVED-MELODY-LEN
  ;; elements in it
  (if (>= (get-melody-event-id-from-melody-event melody-event) SAVED-MELODY-LEN)
    (assoc (subvec melody 1) (dec SAVED-MELODY-LEN) melody-event)
    (assoc melody (get-melody-event-id-from-melody-event melody-event) melody-event)
    )
  )

(defn check-prior-event-note-off
   " if the prior note was not turned off and
       either this note is a rest or
         this note has a different instrument than the prior note
     then
       turn off the prior note

     First this function checks if the prior-melody-event has
     :note-off nil and sc-instrument-id <not-nil (some number)>
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
         ]
    (if (and (nil? (get-note-off-from-melody-event prior-melody-event))
             (get-sc-instrument-id-from-melody-event prior-melody-event))
      (do
        (Thread/sleep NEXT-NOTE-PROCESS-MILLIS)
        (println "%%%%%%%% ABOUT TO RECUR " player-id " %%%%%%%%")
        (recur player-id
               prior-melody-event-ndx
               (nth (get-melody-for-player-id player-id) prior-melody-event-ndx)
               cur-melody-event))
      ;; If the note-off for the prior-melody-event is true, then a gate-off event
      ;; has already been scheduled for the prior-melody-event. If it is false, we need to
      ;; check if the current event is a different instrument than the prior event
      ;; (sc-instrument-id different in events) or if the current event is a rest (freq=nil).
       (if (and (false? (get-note-off-from-melody-event prior-melody-event))
                (or (nil? (get-freq-from-melody-event cur-melody-event))
                    (not=
                     (get-sc-instrument-id-from-melody-event prior-melody-event)
                     (get-sc-instrument-id-from-melody-event cur-melody-event)
                     )
                    )
                )
         (sched-gate-off (get-sc-instrument-id-from-melody-event prior-melody-event)
                         (+ (get-play-time-from-melody-event prior-melody-event)
                            (get-dur-millis-from-melody-event prior-melody-event)))
         )))
  )

(defn play-note-prior-instrument
  [prior-melody-event melody-event play-time]
  (println "*****************************************************************")
  (println "play_note_prior_instrument")
  (println "*****************************************************************")
  ;; TODO
  ;; Might not need to do this, but check it out when this is implemented!
  ;; since this instrument is already started we will not get a /n_go msg from
  ;; supercollider. It might be possible to schedule the gate off here, except
  ;; it could be a problem if the next note will also play with this same
  ;; supercollider instrument (in that case the gate-off will be scheduled, and
  ;; when the program tries to retrigger the supercollider instrument for the
  ;; next note, the instrument will be gone).
  (let [inst-id (get-sc-instrument-id-from-melody-event prior-melody-event)]
    ;; apply not tested???
    ;; (apply ctl inst-id
    ;;      :freq (get-freq-from-melody-event melody-event)
    ;;      :vol (* (get-volume-from-melody-event melody-event)
    ;;              (get-setting :volume-adjust))
    ;;      (get-instrument-settings-from-melody-event melody-event)
    ;;      )
    inst-id
    )
  )

(defn play-note-new-instrument
  [melody-event play-time]
  (let [synth-id (sc-next-id :node)]
    ;; Need to use apply here to unpack the args from get-instrument-settings-from-melody-event
    (sc-send-bundle play-time
                    (apply sc-send-msg
                           "/s_new"
                           (get-instrument-from-instrument-info
                            (get-instrument-info-from-melody-event melody-event))
                           synth-id
                           tail
                           (:instrument-group-id @base-group-ids*)
                           "freq" (get-freq-from-melody-event melody-event)
                           "vol" (* (get-volume-from-melody-event melody-event) (get-setting :volume-adjust))
                           (get-instrument-settings-from-melody-event melody-event)))
    synth-id
    )
  )

(declare play-next-note)
(defn sched-next-note
  [melody-event]
  (when-let [d-info (get-dur-info-from-melody-event melody-event)]
    (let [event-time (get-event-time-from-melody-event melody-event)
          next-time (+ event-time (get-dur-millis-from-dur-info d-info))
          ]
      (go (<! (timeout (- (get-dur-millis-from-dur-info d-info) (- (System/currentTimeMillis) event-time))))
          (play-next-note (get-player-id-from-melody-event melody-event) next-time))
      )))

(defn sched-release
  [event]
  (println "***** sched-release event: " event)
  (if (= (nth (event :args) 4) 0)  ;; 0 means this is a synth
      (let [sc-instrument-id (first (event :args))
            melody-event (get @synth-melody-map sc-instrument-id)
            ]
        (when melody-event
          (if (get-note-off-from-instrument-info
                 (get-instrument-info-from-melody-event melody-event))
            (let [player-id (get-player-id-from-melody-event melody-event)
                  melody-event-id (get-melody-event-id-from-melody-event melody-event)
                  release-millis (get-release-millis-from-instrument sc-instrument-id)
                  note-off-val
                  (> (get-dur-millis-from-melody-event melody-event) release-millis)
                  ]
              (update-melody-note-off-for-player-id player-id melody-event-id note-off-val)
              (if note-off-val
                (sched-gate-off sc-instrument-id
                                (- (+ (get-play-time-from-melody-event melody-event)
                                      (get-dur-millis-from-dur-info
                                       (get-dur-info-from-melody-event melody-event)))
                                   release-millis))
                ;; (let [melody (get-melody-for-player-id player-id)]
                ;;   (if (not= melody-event-id
                ;;             (get-melody-event-id-from-melody-event (last melody)))
                ;;     (check-prior-event-note-off (nth melody (mod (- melody-event-id 1) SAVED-MELODY-LEN))
                ;;                                 (nth melody (mod melody-event-id SAVED-MELODY-LEN)))
                ;;     ))
                )
              )
            )
          (swap! synth-melody-map dissoc sc-instrument-id))))
  nil
  )

(defn play-melody-event
  [prior-melody-event melody-event play-time]
  (let [cur-inst-id
        (cond (nil? (get-freq-from-melody-event melody-event))
              nil
              ;; Need to use (not (false? here because note-off can be false or nil
              (not (false? (get-note-off-from-melody-event prior-melody-event)))
              (play-note-new-instrument melody-event play-time)
              :else
              (play-note-prior-instrument prior-melody-event melody-event play-time)
              )
        full-melody-event (set-play-info melody-event
                                         cur-inst-id
                                         play-time
                                         )
        ]
    (when cur-inst-id
      (swap! synth-melody-map assoc cur-inst-id full-melody-event))

    full-melody-event
    )
  )

(defn play-next-note
  [player-id sched-time]
  (println "-")
  (println "start -" player-id)
  (let [event-time sched-time
        play-time (+ sched-time NEXT-NOTE-PROCESS-MILLIS)
        ;; TODO document why we are clearing messages here
        [ensemble player-msgs] (get-ensemble-clear-msg-for-player-id player-id)
        player (get-player ensemble player-id)
        melody (get-melody-for-player-id-from-ensemble ensemble player-id)
        [upd-player next-melody-event] (get-next-melody-event
                                        ensemble
                                        player
                                        melody
                                        player-id
                                        event-time)
        upd-melody-event (play-melody-event (last melody)
                                            next-melody-event
                                            play-time)
        upd-melody (update-melody-with-event melody upd-melody-event)
        ]
    (println player-id
             (get-loop-name player)
             (if (get-freq-from-melody-event upd-melody-event)
               ""
               "REST"))
    (check-prior-event-note-off player-id upd-melody-event)
    (update-player-and-melody upd-player upd-melody player-id)
    (sched-next-note upd-melody-event)
    (>!! (get-msg-channel) {:msg :melody-event
                            :data upd-melody-event
                            :time (System/currentTimeMillis)})
    (println "end:   " player-id  " melody-event: " (:melody-event-id upd-melody-event))
    (println "\n\n\n")
    )
 )
