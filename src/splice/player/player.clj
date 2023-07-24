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

(ns splice.player.player
  (:require
   [splice.player.loops.loop :refer [create-loop]]
   [splice.player.loops.multiplying-loop :refer [create-multiplying-loop]]
   [splice.instr.instrument :refer [get-instrument]]
   [splice.util.log :as log]
   )
  )

(defrecord Player [id
                   loop-structr
                   key
                   scale
                   mm
                   instrument-info
                   ])

(defn build-loop-structr
  [loop-settings]
  (condp = (:loop-type loop-settings)
    :loop (create-loop :name (:name loop-settings)
                       :melody-info (:melody-info loop-settings)
                       :next-melody-event-ndx 0
                       )

    :multiplying-loop (create-multiplying-loop
                       :name (:name loop-settings)
                       :melody-info (:melody-info loop-settings)
                       :next-melody-event-ndx 0
                       :max-num-mult-loops (:max-num-mult-loops loop-settings)
                       )

    nil (throw (Throwable. (str ":loop-type missing")))
    (do
      (throw (Throwable. (str "Invalid :loop-type "
                              (:loop-type loop-settings))))
      )
    )
  )

(defn create-player
  [& {:keys [id loop-settings]}]
  (log/info (str "player/create-player - Creating player " id))
  (when (nil? (:instrument-name loop-settings))
    (throw (Throwable. "Missing :instrument-name in loop")))
  (Player. id
           (build-loop-structr loop-settings)
           nil  ;; key
           nil  ;; scale
           nil  ;; mm
           (get-instrument (:instrument-name loop-settings))
           )
  )
