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

(ns splice.melody.rhythm
  (:require
   [splice.melody.dur-info]
   [splice.util.random :refer [random-int]]
   )
  (:import
   [splice.melody.dur_info DurInfo]
   )
  )

(def min-dur 37)    ;; min duration 37 milliseconds
(def max-dur 12000) ;; max duration 12 seconds

(defn get-random-dur-millis
  ([] (random-int min-dur max-dur))
  ([min max] (random-int min max))
  )

(defn select-random-dur-info
  ([]
   (let [base-dur (get-random-dur-millis min max)]
     (DurInfo. base-dur nil base-dur)))
  ([min max]
   (let [base-dur (get-random-dur-millis min max)]
     (DurInfo. base-dur nil base-dur)))
  )
