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
   [splice.player.loops.looptype :refer [LoopType get-name
                                         get-loop-repetition
                                         set-loop-repetition
                                         ]]
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
                            create-player-fn       ;; need to pass in to avoid circular dependency
                                                   ;;   with player.clj
                            min-new-loop-delay-ms  ;; the minimum number of millis to wait
                                                   ;;   before creating a new loop.
                            max-new-loop-delay-ms  ;; the maximum number of millis to wait
                                                   ;;   before creating a new loop.
                            core-loop
                            ]
  LoopType
  (get-name [loop] (get-name (:core-loop loop)))
  (get-loop-repetition [loop] (get-loop-repetition (:core-loop loop)))
  (set-loop-repetition
    [loop loop-rep]
    (assoc loop :core-loop (set-loop-repetition (:core-loop loop) loop-rep)))
  )

(defn build-new-loop-structr
  [loop instrument-name]
  ;; Building a Loop record structure here because the loop we are creating will not
  ;; multiply. The original multiplying-loop will multiply as Loop(s).
  {:name (str (get-name loop) "-R" (get-loop-repetition loop))
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
         new-player-id (dec new-num-players)   ;; player-ids start from 0
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
  [loop-structr begining-of-loop? loop-rep]
  (and begining-of-loop?
       (> loop-rep (:reps-before-multing loop-structr))
       (< (:num-mult-loops-started loop-structr) (:max-num-mult-loops loop-structr))
       (< (rand-int 100) (:loop-mult-probability loop-structr))
       )
  )

(defn- update-loop-structr
  [loop-structr core-loop-structr loop-rep make-new-loop?]
  (if loop-rep
    (assoc loop-structr
           :core-loop (set-loop-repetition core-loop-structr loop-rep)
           :num-mult-loops-started (if make-new-loop?
                                     (inc (:num-mult-loops-started loop-structr))
                                     (:num-mult-loops-started loop-structr))
           )
    (assoc loop-structr
           :num-mult-loops-started (if make-new-loop?
                                     (inc (:num-mult-loops-started loop-structr))
                                     (:num-mult-loops-started loop-structr))
           )
    )
  )

(defn get-next-mult-melody
  [& {:keys [player melody loop-structr next-melody-event-id event-time inc-reps?]
      :or {inc-reps? true}
      }]
  (let [core-loop (:core-loop loop-structr)
        ;; if next-melody-event-ndx is 0, this melody event will be the start of the loop
        begining-of-loop? (= (get-next-melody-event-ndx core-loop) 0)
        ;; Since here core-loop is a Loop record and get-next-melody is in loop.clj,
        ;; just core-loop to get-next-melody
        [upd-core-loop-structr melody-event]
        (get-next-melody :player player
                         :melody melody
                         :loop-structr core-loop
                         :next-melody-event-id next-melody-event-id
                         :event-time event-time
                         :inc-reps? false)
        loop-rep (if (and inc-reps?
                          (:original-loop? loop-structr)
                          begining-of-loop?)
                   (inc (get-loop-repetition loop-structr))
                   (get-loop-repetition loop-structr)
                   )
        make-new-loop? (create-new-loop? loop-structr begining-of-loop? loop-rep)
        ;; since the Loop get-next-melody fn is being used, the returned upd-loop is a Loop
        ;; record. This is placed in the core-loop of this multiplying-loop
        upd-loop-structr (update-loop-structr loop-structr
                                              upd-core-loop-structr
                                              loop-rep
                                              make-new-loop?)
        ]
    (println "multiplying_loop/get-next-mult-melody inc-reps? " inc-reps? " original-loop?: " (:original-loop? loop-structr)
             " begining-of-loop: " begining-of-loop?)
    (when make-new-loop?
      (let [new-player-id (add-player player upd-loop-structr)]
        ;; need to wait till the dosync in add-player commits before calling play-first-note
        (play-first-note new-player-id
                         (:min-new-loop-delay-ms loop-structr)
                         (:max-new-loop-delay-ms loop-structr)
                         ))
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
             min-new-loop-delay-ms
             max-new-loop-delay-ms
             max-num-mult-loops
             reps-before-multing
             loop-mult-probability
             create-player-fn
             ]
      :or {next-melody-fn get-next-mult-melody
           min-new-loop-delay-ms 0
           max-new-loop-delay-ms 0
           max-num-mult-loops nil
           }
      }]
  (MultiplyingLoop. max-num-mult-loops
                    (or reps-before-multing 1)
                    0
                    (or loop-mult-probability 100)
                    true                            ;; original-loop?
                    create-player-fn
                    (or min-new-loop-delay-ms 0)
                    (or max-new-loop-delay-ms 0)
                    (create-loop :name name
                                 :melody-info melody-info
                                 :next-melody-event-ndx next-melody-event-ndx
                                 :next-melody-fn next-melody-fn)
                    )
  )
