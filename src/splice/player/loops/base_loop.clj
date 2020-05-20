;    Copyright (C) 2018-2019  Joseph Fosco. All Rights Reserved
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

(ns splice.player.loops.base-loop
  (:require
   [splice.melody.dur-info :refer [create-dur-info]]
   [splice.melody.melody-event :refer [create-melody-event]]
   [splice.melody.rhythm :refer [select-random-dur-info]]
   [splice.player.player-info :refer [get-player-id
                                       get-player-instrument-info]]
   )
  )

(defrecord BaseLoop [name
                     next-melody-fn
                     pitch-fn
                     ])

(defn get-base-loop-name
  [loop-structr]
  (:name (:base-loop loop-structr))
  )

(defn get-melody-fn
  [loop-structr]
  (:next-melody-fn (:base-loop loop-structr))
 )

(defn get-loop-dur-info
  [dur-info]
  (condp = (:type dur-info)
    :fixed (create-dur-info
            :dur-millis (:dur-millis dur-info)
            :dur-beats (:dur-beats dur-info)
            )
    :variable-millis (select-random-dur-info
                      (:min-millis dur-info)
                      (:max-millis dur-info)
                      )
    :variable-inc-millis (let [base-dur (:dur-millis dur-info)]
                           (select-random-dur-info
                            (- base-dur (:dec-millis dur-info))
                            (+ base-dur (:inc-millis dur-info))
                            ))
    :variable-pct nil
    )
  )

(defn play-event?
  [play-prob]
  (if (< (rand-int 100) play-prob)
    true
    false)
  )

(defn get-next-loop-event-ndx
  "Returns the next loop-event index to use starting at start-ndx
   checks each loop-events :play-prob (play probability) if it exists
  "
  [loop-structr start-ndx]
  (first
   (take 1
         (for [ndx (iterate
                    #(mod (inc %1)
                          (count (:melody-info  loop-structr))) start-ndx)
               :when (let [play-prob (:play-prob ((:melody-info loop-structr)
                                                  ndx))]
                       (if play-prob
                         (play-event? play-prob)
                         true
                         ))
               ]
           ndx)
         ))
  )

(defn get-next-melody
  "Returns an updated loop structure a melody-event, and the loop name"
  [player melody loop-structr next-id]
  (let [melody-ndx (get-next-loop-event-ndx loop-structr
                                            (:next-melody-event-ndx loop-structr))
        melody-info ((:melody-info loop-structr) melody-ndx)
        ;; melody-event (create-melody-event
        ;;               :melody-event-id next-id
        ;;               :freq ((:pitch-fn loop-structr) (:pitch melody-info))
        ;;               :dur-info ((:dur-fn loop-structr) (:dur melody-info))
        ;;               :volume (:volume melody-info)
        ;;               :instrument-info (get-player-instrument-info player)
        ;;               :instrument-settings (:instrument-settings melody-info)
        ;;               :player-id (get-player-id player)
        ;;               :event-time nil
        ;;               )
        melody-event (create-melody-event
                      :melody-event-id next-id
                      :freq ((:pitch-fn (:base-loop loop-structr)) (:pitch melody-info))
                      :dur-info (get-loop-dur-info (:dur melody-info))
                      :volume (:volume melody-info)
                      :instrument-info (get-player-instrument-info player)
                      :instrument-settings (:instrument-settings melody-info)
                      :player-id (get-player-id player)
                      :event-time nil
                      )
        ]
    [
     (assoc loop-structr :next-melody-event-ndx (mod (inc melody-ndx)
                                                     (count (:melody-info
                                                             loop-structr))))
     melody-event
     (:name melody-info)
     ]

    )
  )

(defn create-base-loop
  [& {:keys [name
             next-melody-fn
             pitch-fn]
      :or {next-melody-fn get-next-melody}}
   ]
  (BaseLoop. name
             next-melody-fn
             pitch-fn
             )
  )
