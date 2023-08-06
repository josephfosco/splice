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
   [splice.ensemble.ensemble :refer [update-player-and-melody get-ensemble]]
   [splice.instr.instrumentinfo :refer [get-instrument-from-instrument-info]]
   [splice.player.loops.loop :refer [create-loop
                                     get-melody-info
                                     get-next-melody-event-ndx
                                     get-next-melody
                                     ]]
   [splice.player.loops.looptype :refer [LoopType get-name]]
   [splice.player.player-play-note :refer [play-first-note play-next-note]]
   [splice.player.player-utils :refer [get-player-instrument-info]]
   [splice.melody.melody-event :refer [create-rest-event]]
   [splice.util.log :as log]
   [splice.util.settings :refer [get-setting set-setting! update-settings!]]
   [splice.util.util :refer [compute-volume-adjust]]
   )
  )

(defrecord MultiplyingLoop [max-num-mult-loops     ;; will add this many loops - nil if loop copy
                            reps-before-multing    ;; num repititions before creating the
                                                   ;;   first multiplying loop
                            num-mult-loops-started ;; num of mult-loops that have been started
                            loop-mult-probability  ;; percent probability that loop will
                                                   ;;   mult this rep
                            original-loop?         ;; true - first loop, false - loop copy
                            loop-repetition    ;; number of this repetition (0 - n) nil for copy
                            create-player-fn   ;; need to pass in to avoid circular dependency
                                               ;;   with player.clj
                            core-loop
                            ]
  LoopType
  (get-name [loop] (get-name (:core-loop loop)))
  )

(defn build-new-loop-structr
  [loop instrument-name]
  ;; Building a Loop record structure here because the loop we are creating will not
  ;; multiply. The original multiplying-loop will multiply as Loop(s).

  {:name (get-name loop)
   :loop-type :loop
   :instrument-name instrument-name
   :melody-info (get-melody-info (:core-loop loop))
   }
  )

(defn add-player
  [player loop]
  (dosync
   (let [new-num-players
         (set-setting! :number-of-players (inc (get-setting :number-of-players)))
         new-player-id (dec new-num-players)
         new-vol-adjust (compute-volume-adjust new-num-players)
         instrument-name (keyword (get-instrument-from-instrument-info
                                   (get-player-instrument-info player)))
         new-player ((:create-player-fn loop) :id new-player-id
                     :loop-settings (build-new-loop-structr loop
                                                            instrument-name))
         new-melody (vector (create-rest-event new-player-id 0 0))
         ]
     (update-player-and-melody new-player new-melody new-player-id)
     (set-setting! :volume-adjust new-vol-adjust)
     new-player-id
     ))
  )

(defn create-new-loop?
  [loop-structr begining-of-loop loop-rep]
  (println loop-structr)
  (and begining-of-loop
       (> loop-rep (:reps-before-multing loop-structr))
       (< (:num-mult-loops-started loop-structr) (:max-num-mult-loops loop-structr))
       (< (rand-int 100) (:loop-mult-probability loop-structr))
       )
  )

(defn get-next-mult-melody
  [player melody loop-structr next-melody-event-id event-time]
  (let [core-loop (:core-loop loop-structr)
        ;; if next-melody-event-ndx is 0, this melody event will be the start of the loop
        begining-of-loop (= (get-next-melody-event-ndx core-loop) 0)
        ;; Since here core-loop is a Loop record and get-next-melody is in loop.clj,
        ;; just core-loop to get-next-melody
        [upd-core-loop-structr melody-event]
        (get-next-melody player melody core-loop next-melody-event-id event-time)
        loop-rep (if (and (:original-loop? loop-structr)
                          begining-of-loop)
                   (inc (:loop-repetition loop-structr))
                   (:loop-repetition loop-structr)
                   )
        make-new-loop (create-new-loop? loop-structr begining-of-loop loop-rep)
        ;; since the Loop get-next-melody fn is being used, the returned upd-loop is a Loop
        ;; record. This is placed in the core-loop of this multiplying-loop
        upd-loop-structr (assoc loop-structr
                                :core-loop upd-core-loop-structr
                                :loop-repetition loop-rep
                                :num-mult-loops-started (if make-new-loop
                                                          (inc (:num-mult-loops-started loop-structr))
                                                          (:num-mult-loops-started loop-structr))
                                )
        ]
    (when make-new-loop
      (let [new-player-id (add-player player upd-loop-structr)]
        ;; need to wait till the dosync in add-player commits before calling play-first-note
        (play-first-note new-player-id 0 0))
      )

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
             reps-before-multing
             loop-mult-probability
             create-player-fn
             ]
      :or {next-melody-fn get-next-mult-melody
           max-num-mult-loops nil
           reps-before-multing 1
           loop-mult-probability 100}
      }]
  (MultiplyingLoop. max-num-mult-loops
                    reps-before-multing
                    0
                    loop-mult-probability
                    (if max-num-mult-loops true false)
                    (if max-num-mult-loops 0 nil)
                    create-player-fn
                    (create-loop :name name
                                 :melody-info melody-info
                                 :next-melody-event-ndx next-melody-event-ndx
                                 :next-melody-fn next-melody-fn)
                    )
  )
