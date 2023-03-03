;    Copyright (C) 2017-2019  Joseph Fosco. All Rights Reserved
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
   [splice.melody.melody-event :refer [create-melody-event]]
   [splice.melody.pitch :refer [select-random-pitch]]
   [splice.melody.rhythm :refer [select-random-dur-info]]
   [splice.player.loops.base-loop :refer [get-melody-fn
                                          get-base-loop-name]]
   )
  )

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

(defn create-random-rest-melody-event
  [player-id event-id]
  (create-melody-event :melody-event-id event-id
                       :freq nil
                       :dur-info (select-random-dur-info)
                       :volume nil
                       :instrument-info nil
                       :player-id player-id
                       :event-time nil
                       )
  )

(defn create-nodur-rest-melody-event
  [player-id event-id]
  (create-melody-event :melody-event-id event-id
                       :freq nil
                       :dur-info nil
                       :volume nil
                       :instrument-info nil
                       :player-id player-id
                       :event-time nil
                       )
  )

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
  [ensemble player melody player-id]

  (println player-id " PLAYER: " player " MELODY: " melody)
  (let [loop-structr (get-loop-structr player)
        [upd-loop-structr melody-event ]
        ((get-melody-fn loop-structr) player
                                      melody
                                      loop-structr
                                      (inc (:melody-event-id (last melody))))
        ]
    [
     (assoc player :loop-structr upd-loop-structr)
     melody-event
     ]
    )
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
