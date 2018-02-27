;    Copyright (C) 2018  Joseph Fosco. All Rights Reserved
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

(ns transit.player.structures.listen
  (:require
   [clojure.core.async :refer [<! chan close! go-loop sub]]
   [overtone.live :refer [apply-at]]
   [transit.melody.dur-info :refer [create-dur-info]]
   [transit.melody.melody-event :refer [create-melody-event]]
   [transit.player.player-utils :refer [get-player-id]]
   [transit.player.player-play-note :refer [play-next-note]]
   [transit.util.random :refer [random-int]]
   [transit.player.structures.base-structure :refer [create-base-structure
                                                     get-internal-strength
                                                     get-structr-id
                                                     set-internal-strength
                                                     set-remove-structr]]
   [transit.util.util :refer [drain-chan get-msg-pub]]
   )
)

(defrecord Listen [base])

(defn sub-ensemble-status
  [structr player]
  (println "$$$$$$$$$$$ sub-ensemble-status player-id: " (get-player-id player) " $$$$$$$$$$$$$$$")
  (let [ens-out-chan (chan)
        player-id (get-player-id player)
        ]
    (sub (get-msg-pub) :ensemble-status ens-out-chan)
    (go-loop [status-msg (<! ens-out-chan)]
      (println "!!!!!!!!!! GOT STATUS MESSAGE player-id: " player-id " !!!!!!!!!!!")
      (if (>= (:density (:status status-msg)) (random-int 0 9))
        (do
          (close! ens-out-chan)
          (drain-chan ens-out-chan)
          (apply-at 0
                    play-next-note
                    [player-id (System/currentTimeMillis)]
                    ))

        (recur (<! ens-out-chan))
        )
      )
    )
  )

(defn get-rest-event
  [player next-id listen-structr]
  (create-melody-event
   :melody-event-id next-id
   :note nil
   :dur-info nil
   :volume nil
   :instrument-info nil
   :player-id (:id player)
   :event-time nil
   :structr-id (get-structr-id listen-structr)
   )
  )

(defn get-listen-melody-event
  [ensemble player melody player-id listen-structr next-id]
  (let [new-int-strength (- (get-internal-strength listen-structr)
                            (random-int
                             1
                             (max 1
                                  (int (/ (get-internal-strength listen-structr)
                                          2)))))
        ]
    [
     (if (not= new-int-strength 0)
       (set-internal-strength listen-structr new-int-strength)
       (set-internal-strength (set-remove-structr listen-structr true)
                              new-int-strength)
       )
     (get-rest-event player next-id listen-structr)
     ]
    ))

(defn create-listen-structr
  [& {:keys [structr-id
             internal-strength
             external-strength
             ] :or
      {internal-strength 0
       external-strength 0
       }}]
  (Listen. (create-base-structure :structr-id structr-id
                                       :internal-strength internal-strength
                                       :external-strength external-strength
                                       :melody-fn get-listen-melody-event
                                       :post-play-fn sub-ensemble-status)
                 )
  )
