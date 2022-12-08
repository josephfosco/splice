;    Copyright (C) 2019  Joseph Fosco. All Rights Reserved
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
   [overtone.live :refer [apply-at now stop]]
   [splice.effects.effects :refer [reverb]]
   [splice.ensemble.ensemble :refer [init-ensemble]]
   [splice.ensemble.ensemble-status :refer [start-ensemble-status]]
   [splice.player.player :refer [create-player]]
   [splice.player.player-play-note :refer [play-next-note]]
   [splice.melody.melody-event :refer [create-melody-event]]
   [splice.util.log :as log]
   [splice.util.print :refer [print-banner]]
   [splice.util.random :refer [random-int]]
   [splice.util.settings :refer [load-settings get-setting set-setting!]]
   [splice.util.util :refer [close-msg-channel start-msg-channel]]
   )
  )

(def ^:private is-playing? (atom false))

(def valid-loop-keys (set '(:instrument-name
                            :loop-type
                            :melody-info
                            :name
                            )))

(defn init-splice
  "Initialize splice to play.

   args -
   players - list of all initial players
   melodies - list of all initial melodies (1 for each player)
   msgs -  an empty list to be used for msgs sent to the player
  "
  [players melodies msgs]
  (start-msg-channel)
  (init-ensemble players melodies msgs)
  (start-ensemble-status)
  )

(defn validate-loop-keys
  [loop-settings]
  (flatten
   (for [loop loop-settings]
     (let [loop-keys (keys loop)]
       (for [loop-key loop-keys
             :when (not (contains? valid-loop-keys loop-key))]
         (str "Invalid loop key " loop-key " in player-settings")
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
                  (range (get-setting :num-players))
                  (:loops player-settings)))
      ))
 )

(defn init-melody
  [player-id]

  (vector (create-melody-event :melody-event-id 0
                               :freq nil
                               :dur-info nil
                               :volume nil
                               :instrument-info nil
                               :player-id player-id
                               :event-time 0
                               ))
  )

(defn init-main-bus-effects
  [effects]
  (dorun (for [effect effects]
           (cond (= (first effect) :reverb) (apply reverb (second effect))
                 )
           )
         )
  )

(defn- play-first-note
  [player-id min-time-offset max-time-offset]

  (let [note-time (+ (now) (random-int (* min-time-offset 1000)
                                       (* max-time-offset 1000)))]
    (apply-at note-time
              play-next-note
              [player-id note-time]
              ))
  )

(defn- start-playing
  "calls play-note the first time for every player in ensemble"
  [min-start-offset max-start-offset]
  (log/info "********** Start-playing ****************")
  (dotimes [id (get-setting :num-players)]
    (play-first-note id min-start-offset max-start-offset))
  )

(defn start-splice
  [{loops :loops :or {loops "src/splice/loops.clj"} :as args}]
  (println "about to start with args: " args)
  (if (false? is-playing?)
    (println "STARTING")
    (let [player-settings (load-settings loops)
          number-of-players (set-setting! :num-players
                                          (count (:loops player-settings)))
          initial-players (init-players player-settings)
          init-melodies (map init-melody (range number-of-players))
          init-msgs (for [x (range number-of-players)] [])
          ]
      (init-main-bus-effects (:main-bus-effects player-settings))
      (set-setting! :volume-adjust (min (/ 32 number-of-players) 1))
      (init-splice initial-players init-melodies init-msgs)
      (start-playing (or (:min-start-offset player-settings) 0)
                     (or (:max-start-offset player-settings) 0))
      (reset! is-playing? true)
      ))
  )

(defn clear-splice
  []
  (println "*** clear-splice not implemented ***")
  )

(defn pause-splice
  []
  (printn "*** pause-splice not implemented ***")
  )

(defn quit-splice
  []
  (stop)
  (close-msg-channel)
  (reset! is-playing? false)
  )
