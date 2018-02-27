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

(ns splice.player.player)

(defrecord Player [id
                   key
                   scale
                   mm
                   instrument-info
                   sampled-melodies
                   can-schedule? ;; is not waiting for msg(s)
                   ])

(defn create-player
  [& {:keys [:id]}]
  (Player. id
           nil  ;; key
           nil  ;; scale
           nil  ;; mm
           nil  ;; instrument
           nil  ;; sampled-melodies
           true ;; can-schedule?
           )
  )

(defn get-player-instrument-info
  [player]
  (:instrument-info player))
