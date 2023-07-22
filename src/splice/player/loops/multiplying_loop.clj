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
   [splice.player.loops.loop :refer [create-loop get-next-melody]])
  )

(defrecord MultiplyingLoop [max-num-mult-loops
                            loop
                            ]
  LoopType
  )

(defn get-next-mult-melody
  [player melody loop-structr next-id event-time]
  (get-next-melody player melody loop-structr next-id event-time)
  )

(defn create-multiplying-loop
  [& {:keys [name melody-info next-melody-event-ndx next-melody-fn max-num-mult-loops]
      :or {next-melody-fn get-next-mult-melody}}]
  (MultiplyingLoop. max-num-mult-loops
                    (create-loop :name name
                                 :melody-info melody-info
                                 :next-melody-event-ndx next-melody-event-ndx
                                 :next-melody-fn next-melody-fn)
         )
  )
