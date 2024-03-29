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

(ns splice.player.player-utils
  (:require
   [splice.ensemble.melody :refer [get-next-melody-event-id]]
   [splice.melody.melody-event :refer [create-rest-event]]
   [splice.melody.pitch :refer [select-random-pitch]]
   [splice.player.loops.base-loop :refer [get-melody-fn
                                          get-base-loop-name]]
   [splice.util.log :as log]
   ))

;; method return values
(def OK 1)  ;; Method completed normally
(def CONTINUE 1)  ;; Processing complete - do not call additional methods
(def NEW-MELODY 2)  ;; Processing complete - last melody event is new
(def NEXT-METHOD 3)  ;; Select and call another method

(defn get-player-id
  [player]
  (:id player))

(defn get-player-instrument-info
  [player]
  (:instrument-info player))

(defn get-loop-structr
  [player]
  (:loop-structr player))

(defn get-loop-name
  [player]
  (get-base-loop-name (:loop-structr player)))

(defn select-random-pitch-for-player
  ([player]
   (select-random-pitch (:range-lo (:instrument-info player))
                        (:range-hi (:instrument-info player))
                        )
   )
  ([player lo hi]
   (select-random-pitch (:range-lo (:instrument-info player))
                        (:range-hi (:instrument-info player))
                        )
   )
  )

(defn get-next-melody-event
  "Returns an updated player and a melody-event"
  [ensemble player melody player-id event-time]

  (log/trace "player-utils.clj get-next-melody-event \n    PLAYER: " (pr-str player) "\n    MELODY: " melody)
  (let [loop-structr (get-loop-structr player)
        [upd-loop-structr melody-event]
        (( get-melody-fn loop-structr) :player player
                                       :melody melody
                                       :loop-structr loop-structr
                                       :next-melody-event-id (get-next-melody-event-id melody)
                                       :event-time event-time)
        ]
    [
     (assoc player :loop-structr upd-loop-structr)
     melody-event
     ]
    )
  )

(defn get-final-rest-event
  [player melody player-id event-time]
  (log/info "GETTING FINAL REST EVENT FOR player-id: " player-id)
  [player (create-rest-event player-id (get-next-melody-event-id melody) event-time)]
  )

(defn print-player
  "Pretty Print a player map

  player - the player map to print"
  [player & {:keys [prnt-full-inst-info]
             :or {prnt-full-inst-info false}}]
  (let [sorted-keys (sort (keys player))]
    (println "player: " (get-player-id player) "current time: " (System/currentTimeMillis))
    (doseq [player-key sorted-keys]
      (cond
        (and (= player-key :instrument-info) (= prnt-full-inst-info false))
        (do
          (println (format "%-29s" (str "  " player-key " :name")) "-" (:name (:instrument (:instrument-info player))))
          (println (format "%-29s" (str "  " player-key " :range-lo")) "-" (:range-lo (:instrument-info player)))
          (println (format "%-29s" (str "  " player-key " :range-hi")) "-" (:range-hi (:instrument-info player))))
        :else
        (println (format "%-20s" (str "  " player-key)) "-" (get player player-key)))
      )
    (println "end player: " (get-player-id player) "current time: " (System/currentTimeMillis))
    (prn)
    )
  )
