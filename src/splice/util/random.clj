;    Copyright (C) 2013 - 2018, 2023  Joseph Fosco. All Rights Reserved
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

(ns splice.util.random)

(defn rand-range
  "Returns a random number between lo and hi
  Does not check that lo < hi"
  [lo hi]
  (+ lo (rand (- hi lo)))
  )

(defn random-int
  "Returns a random integer between lo (inclusive) and hi (inclusive).
  Does not check that lo < hi"
  [lo hi]
  (+ (rand-int (inc (- hi lo)))lo))

(defn weighted-choice
  "Makes a random selection based on a vector of weights.
   Returns the index into the vector of the selection
   Will throw an IndexOutOfBounds exception if the vector is empty

   weight-vector - vector of the form [x1 x2 x3 x4 ....]
                   where each entry is the relative weight of that entry"
  [weight-vector]
  (let [rand-num (rand (reduce + weight-vector))]
    (loop [i 0 sum 0]
      (if (< rand-num (+ (weight-vector i) sum))
        i
        (recur (inc i) (+ (weight-vector i) sum)))))
  )

(defn random-probability-result
  [prob]
  (if (< (rand-int 100) prob)
    true
    false)
  )

(defn add-probabilities
  "Adds to probabilities in prob-vector. This function does not
   do any error checking. If multiple values are specified for
   a single index, all the values will be added to the value in
   prob-vector.

  prob-vector - vector of probabilities to add probabilities to
  prob-to-add-map - a map of probabilities to add where each entry is
                    key - the index to add to
                    value - the amount to add to the probability"
  [prob-vector prob-to-add-map]
  (loop [cur-prob-vector prob-vector
         prob-indexes (keys prob-to-add-map)
         prob-values (vals prob-to-add-map)]
    (if (= (count prob-indexes) 0)
      cur-prob-vector
      (recur (assoc cur-prob-vector (first prob-indexes) (+ (first prob-values) (nth cur-prob-vector (first prob-indexes))))
             (next prob-indexes)
             (next prob-values)))
    )
  )
