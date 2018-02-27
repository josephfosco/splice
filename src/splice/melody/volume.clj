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

(ns splice.melody.volume
  (:require
   [splice.util.settings :refer [get-setting]]
   ))

(def min-vol (get-setting :min-volume))

(defn select-random-volume
  " Returns a random value between min-vol and 1"
  ([]
   (+ (rand (- 1 min-vol)) min-vol))
  ([lo hi]
   (rand lo hi)))
