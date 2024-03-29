;    Copyright (C) 2018, 2022-2023  Joseph Fosco. All Rights Reserved
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

(ns splice.version)

(def SPLICE-VERSION {:major 0
                     :minor 3
                     :patch 0
                     :development false})

(def SPLICE-VERSION-STR
  (let [version SPLICE-VERSION]
    (str (:major version)
         "."
         (:minor version)
         (if-not (= 0 (:patch version)) (str "." (:patch version)) "")
         (if (:development version) "-dev" ""))))
