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

(ns splice.util.random-test
  (:use clojure.test
        splice.util.random
        )
  )

(deftest test-weighted-choice
  (testing "returns index into array"
    (is (<=
         0
         (weighted-choice [10 10 10 10])
         3
         )
        )
    )

  (testing "returns different indexes"
    ;; The last "is" statement might not be true, but is highly unlikely
    ;; that it will return 5 of one number
    (let [result-1 (weighted-choice [10 10])
          result-2 (weighted-choice [10 10])
          result-3 (weighted-choice [10 10])
          result-4 (weighted-choice [10 10])
          result-5 (weighted-choice [10 10])
          ]
      (is (<= 0 result-1 1))
      (is (<= 0 result-2 1))
      (is (<= 0 result-3 1))
      (is (<= 0 result-4 1))
      (is (<= 0 result-5 1))
      (is (not (= result-1 result-2 result-3 result-4 result-5)))
      ))
  )
