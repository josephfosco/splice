;    Copyright (C) 2017-2018  Joseph Fosco. All Rights Reserved
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
   [splice.util.log :as log]
   )
  )

(defrecord Player [id
                   loop-structr
                   key
                   scale
                   mm
                   instrument-info
                   can-schedule? ;; is not waiting for msg(s)
                   ])

(defn build-loop-structr
  [loop-settings]
  (log/info "@@@@@@@@@ build-loop-structr @@@@@@@@@@@@")
  (log/info loop-settings)
  (cond (= (:loop-type loop-settings) :loop)
        (create-loop :melody-events (:melody-events loop-settings))
        (= (:loop-type loop-settings) nil)
        (log/error ":loop-type missing")
        :else
        (do
          (log/error "Invalid :loop-type - " (:loop-type loop-settings))
          nil
          )
        )
  )

(defn create-player
  [& {:keys [id loop-settings]}]
  (Player. id
           (build-loop-structr loop-settings)
           nil  ;; key
           nil  ;; scale
           nil  ;; mm
           nil  ;; instrument
           true ;; can-schedule?
           )
  )
