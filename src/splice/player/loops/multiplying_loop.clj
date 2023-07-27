;    Copyright (C) 2023  Joseph Fosco. All Rights Reserved
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

(ns splice.player.loops.multiplying-loop
  (:require
   [splice.player.loops.base-loop :refer [LoopType]]
   [splice.player.loops.loop :refer [create-loop
                                     get-melody-info
                                     get-next-melody-event-ndx
                                     get-next-melody
                                     ]]
   [splice.util.log :as log]
   [splice.util.settings :refer [update-settings!]]
   [splice.util.util :refer [compute-volume-adjust]]
   )
  )

(defrecord MultiplyingLoop [max-num-mult-loops ;; will add this many loops - nil if loop copy
                            original-loop?  ;; true - first loop, false - loop copy
                            loop-repitition ;; number of this repetition (1 - n) nil for copy
                            core-loop
                            ]
  LoopType
  (get-name [loop] (LoopType/get-name (:core-loop loop)))
  )

(defn add-player-to-settings
  [settings]
  (log/data "add-player-to-settings settings: " settings)
  (let [new-num-players (inc (:number-of-players settings))
        new-vol-adjust (compute-volume-adjust new-num-players)
        ]
    (assoc settings :number-of-players new-num-players :volume-adjust new-vol-adjust)
    )
  )

(defn build-loop-structr
  [loop instrument-name]
  ;; Building a Loop record structure here because the loop we are creating will not
  ;; multiply. Only the original loop will multiply

  (let [loop-record (:core-loop loop)]
    {:name (get-name loop-record)
     :loop-type :loop
     :instrument-name instrument-name
     :melody-info (get-melody-info loop-record)
     })
  )

(defn create-new-player
  [player loop]
  (update-settings! add-player-to-settings)
  )

(defn get-next-mult-melody
  [player melody loop-structr next-id event-time]
  (let [[upd-core-loop-structr melody-event]
        ;; Since here core-loop is a Loop record and get-next-melody is in loop.clj,
        ;; just core-loop to get-next-melody
        (get-next-melody player melody (:core-loop loop-structr) next-id event-time)
        ;; this melody-event is the start of the loop melody if the next-melody-event-ndx is 1
        begining-of-loop (= (get-next-melody-event-ndx upd-core-loop-structr) 1)
        loop-rep (if (and (:original-loop? loop-structr)
                          begining-of-loop)
                   (inc (:loop-repitition loop-structr))
                   (:loop-repitition loop-structr)
                   )
        upd-loop-structr (assoc loop-structr
                                :core-loop upd-core-loop-structr
                                :loop-repitition loop-rep
                                )

        ]
    (when begining-of-loop
      (create-new-player player upd-loop-structr)
      )

    ;; since the Loop get-next-melody fn is being used, the returned upd-loop is a Loop
    ;; record. This is placed in the core-loop of this multiplying-loop
    [
     upd-loop-structr
     melody-event
     ]
    )
  )

(defn create-multiplying-loop
  [& {:keys [name
             melody-info
             next-melody-event-ndx
             next-melody-fn
             max-num-mult-loops
             ]
      :or {next-melody-fn get-next-mult-melody max-num-mult-loops nil}}]
  (MultiplyingLoop. max-num-mult-loops
                    (if max-num-mult-loops true false)
                    (if max-num-mult-loops 1 nil)
                    (create-loop :name name
                                 :melody-info melody-info
                                 :next-melody-event-ndx next-melody-event-ndx
                                 :next-melody-fn next-melody-fn)
                    )
  )
