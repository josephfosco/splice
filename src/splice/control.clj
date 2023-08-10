;    Copyright (C) 2019, 2023  Joseph Fosco. All Rights Reserved
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

(ns splice.control
  (:require
   [clojure.core.async :refer [<! go timeout]]
   [sc-osc.sc :refer [sc-allocate-bus-id
                      sc-debug
                      sc-event
                      sc-free-id
                      sc-send-msg
                      sc-next-id
                      sc-oneshot-sync-event
                      sc-reset-counter!
                      sc-uuid
                      sc-with-server-sync]]
   [splice.ensemble.ensemble :refer [clear-ensemble init-ensemble]]
   [splice.ensemble.ensemble-status :refer [start-ensemble-status stop-ensemble-status]]
   [splice.player.loops.base-loop :refer [init-base-loop]]
   [splice.player.player :refer [create-player]]
   [splice.player.player-play-note :refer [init-player-play-note play-first-note play-next-note]]
   [splice.sc.groups :refer [base-group-ids* setup-base-groups]]
   [splice.melody.melody-event :refer [create-rest-event]]
   [splice.sc.sc-constants :refer [head tail]]
   [splice.util.log :as log]
   [splice.util.settings :refer [load-settings get-setting set-setting!]]
   [splice.util.util :refer [close-msg-channel compute-volume-adjust start-msg-channel]]
  ))

(def ^:private splice-status (atom ::stopped))
(def main-fx-bus-first-in-chan (atom nil))
(def main-fx-bus-first-out-chan (atom nil))

(def valid-loop-keys (set '(:instrument-name
                            :loop-type
                            :melody-info
                            :name
                            :max-num-mult-loops
                            :reps-before-multing
                            :num-mult-loops-started
                            :loop-mult-probability
                            )))

(defn remove-synths-effects-busses
  "Removes and frees all synths, effects, and main effect busses"
  []
  (log/info "freeing all synths, effects, and main effect busses....")
  (sc-with-server-sync #(sc-send-msg
                         "/g_deepFree"
                         (:splice-group-id @base-group-ids*))
                       "while freeing all synthes and effects")
  (when @main-fx-bus-first-in-chan
    (sc-free-id :audio-bus @main-fx-bus-first-in-chan 2)
    (reset! main-fx-bus-first-in-chan nil)
    )
  (when @main-fx-bus-first-out-chan
    (sc-free-id :audio-bus @main-fx-bus-first-out-chan 2)
    (reset! main-fx-bus-first-out-chan nil))
  )

(defn reset-control
  [event]
  (log/info "resetting control....")

  (remove-synths-effects-busses)
  ;; delete :node counters so when starting again it will start at 0 for root_goup_
  (sc-reset-counter! :node)
  (reset! splice-status ::stopped)
  )

(defn init-control
  []
  (sc-oneshot-sync-event :reset reset-control (sc-uuid))
  )

(defn init-splice
  "Initialize splice to play.

   args -
   players - list of all initial players
   melodies - list of all initial melodies (1 for each player)
   msgs -  an empty list to be used for msgs sent to the player
  "
  [players melodies msgs]
  (start-msg-channel)
  (init-player-play-note)
  (init-base-loop)
  (init-ensemble players melodies msgs)
  (start-ensemble-status)
  )

(defn validate-loop-keys
  [loop-settings]
  ;; TODO validate loop keys based on type of loop
  (flatten
   (for [loop loop-settings]
     (let [loop-keys (keys loop)]
       (for [loop-key loop-keys
             :when (not (contains? valid-loop-keys loop-key))]
         (str "control.clj - validate-loop-keysInvalid loop key " loop-key " in player-settings")
         )
       )
     ))
  )

(defn validate-player-settings
  [player-settings]
  (let [loop-key-msgs (validate-loop-keys (:loops player-settings))]
    (cond-> '()
      (< (count (:loops player-settings)) 1)
      (conj ":loops not found in player-settings file")
      (not= (count loop-key-msgs) 0)
      ((partial reduce conj) loop-key-msgs)
      )
    ))

(defn new-player
  [player-id loop-setting]
  (create-player :id player-id :loop-settings loop-setting)
  )

(defn init-players
  [player-settings]
  (log/info "******* init-players ********")
  (let [errors (validate-player-settings player-settings)]
    (if (not= 0 (count errors))
      (do
        (doseq [error-msg errors]
          (log/error error-msg))
        (throw (Throwable. "Validation error(s) in player loops"))
        )
      (doall (map new-player
                  (range (get-setting :number-of-players))
                  (:loops player-settings)))
      ))
 )

(defn init-melody
  [player-id]
  (vector (create-rest-event player-id 0 0))
  )

(defn- send-load-msg
  [filename]
  (sc-with-server-sync #(sc-send-msg "/d_load" filename))
  )

(defn init-main-bus-effects
  [effects]

  (println "init-main-bus-effects")
  (if (nil? @main-fx-bus-first-in-chan)
    (reset! main-fx-bus-first-in-chan (sc-allocate-bus-id :audio-bus 2)))
  (if (nil? @main-fx-bus-first-out-chan)
    (reset! main-fx-bus-first-out-chan (sc-allocate-bus-id :audio-bus 2)))

  (let [fx-path "/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/"]
    (send-load-msg (str fx-path "fx-snd-rtn-2ch" ".scsyndef"))
    ;; Create effects send from mains (ch 0 and 1)
    (sc-with-server-sync #(sc-send-msg
                           "/s_new"
                           "fx-snd-rtn-2ch"
                           (sc-next-id :node)
                           head
                           (:pre-fx-group-id @base-group-ids*)
                           "in" 0.0
                           "out" (float @main-fx-bus-first-in-chan))
                         "while starting up the main effect send")
    ;; create effects return to mains
    (sc-with-server-sync #(sc-send-msg
                           "/s_new"
                           "fx-snd-rtn-2ch"
                           (sc-next-id :node)
                           head
                           (:post-fx-group-id @base-group-ids*)
                           "in" (float @main-fx-bus-first-out-chan)
                           "out" 0.0)
                         "while starting up the main effect return")
    (dorun (for [effect effects]
             (cond (= (first effect) "reverb-2ch")
                   (do
                     ;; TODO will eventually need to keep track of which effects are loaded so
                     ;; an effect is not "double loaded"
                     (send-load-msg (str fx-path "reverb-2ch" ".scsyndef"))
                     (sc-with-server-sync #(apply sc-send-msg "/s_new"
                                                  "reverb-2ch"
                                                  (sc-next-id :node)
                                                  tail
                                                  (:main-fx-group-id @base-group-ids*)
                                                  "in" (float @main-fx-bus-first-in-chan)
                                                  "out" (float @main-fx-bus-first-out-chan)
                                                  (second effect))
                                          "while starting the main reverb-2ch effect"))

                   )))
    ))

(defn- load-sc-synthdefs
  [loops]
  (println "loading synthdefs....")
  (let [synth-files (set (for [loop loops]
                          (str
                           "/home/joseph/src/clj/splice/src/splice/instr/instruments/sc/"
                           (name (:instrument-name loop))
                           ".scsyndef")
                          ))

        missing-files (filter #(not (.exists (java.io.File. %))) synth-files)
        ]
    (if (seq missing-files)
      (throw (Throwable.
              (str "MISSING SYNTHDEF FILES\n"
                   "The following SynthDef files are missing:\n"
                   (clojure.string/join "\n" missing-files))
              ))
      (doall (map send-load-msg synth-files))
      )
    ))

(defn- start-playing
  "calls play-note the first time for every player in ensemble"
  [min-start-offset max-start-offset]
  (log/info "********** start-playing ****************")
  (dotimes [id (get-setting :number-of-players)]
    (play-first-note id min-start-offset max-start-offset))
  )

(defn- reserve-root-node-val
  []
  (dosync
   (set-setting! :root-group_ (sc-next-id :node)))
  (log/error (get-setting :root-group_))
  (when (not= (get-setting :root-group_) 0)
    (throw (Throwable.
            (str "root-group_ NOT 0\n"
                 "root-group_ must be 0 in the :node table because supercollider\n"
                 "sets the root node to zero and this cannot be changed\n"
                 "Somehow sc-next-id or sc_osc.counters/next-id was called before"
                 ":root-group_ was assigned an id in control/reserve-root-node-val"
                 )
            )))
  )

(defn start-splice
  [{:keys [loops] :or {loops "src/splice/loops.clj"} :as args}]
  (println "about to start with args: " args)
  (if (= @splice-status ::stopped)
    (do
      (reset! splice-status ::starting)
      (log/info "STARTING")
      ;; Set _root-group_ right away to make certain it is set to o in the :node counter
      ;; It MUST be zero because in supercollider the root_node is always 0 and
      ;; cannot be changed
      (reserve-root-node-val)
      (init-control)
      (let [player-settings (load-settings loops)
            number-of-players (dosync (set-setting! :number-of-players
                                                     (count (:loops player-settings))))
            initial-players (init-players player-settings)
            init-melodies (map init-melody (range number-of-players))
            init-msgs (for [x (range number-of-players)] [])
            ]
        (setup-base-groups)
        (load-sc-synthdefs (:loops player-settings))
        (if-let [effects (get player-settings :main-bus-effects)]
          (init-main-bus-effects effects))
        (dosync
         (set-setting! :volume-adjust (compute-volume-adjust number-of-players)))
        (init-splice initial-players init-melodies init-msgs)
        (start-playing (or (:min-start-offset player-settings) 0)
                       (or (:max-start-offset player-settings) 0))
        (reset! splice-status ::playing)
        ))
    (log/warn "******* CAN NOT START - SPLICE IS NOT STOPPED *******")
    ))

(defn clear-splice
  []
  (log/warn "*** clear-splice not implemented ***")
  )

(defn pause-splice
  []
  "Stop player scheduling everything else remains active"
  (sc-event :stop-player-scheduling)
  )

(defn reset-splice
  [event]
  (sc-event :reset))

(defn quit-splice
  []
  ;; The way splice-stop works at this point is
  ;; - this module registers a :player-scheduling-stopped event here
  ;; - then this modules fires a :stop-player-scheduling event.
  ;; - player/player-play-note.clj has registered to receive the :player-stop-scheduling
  ;;   event, and when it receives the event it stops scheduling new notes/events for
  ;;   all players.
  ;; - When all players have been stopped, player/player-play-note.clj fires a
  ;; - :player-scheduling-stopped event
  ;; - the :player-scheduling-stopped event is picked up in this module and calls
  ;;   the function reset-splice.
  ;; - reset-splice fires a :reset event
  ;; - modules have registered to receive the :reset event in order to shut down or
  ;;   reset themselves.

  ;; Wait for player scheduling to stop before resetting the rest of the app
  (reset! splice-status ::stopping)
  (sc-oneshot-sync-event :player-scheduling-stopped reset-splice (sc-uuid))
  (sc-event :stop-player-scheduling)
  )
