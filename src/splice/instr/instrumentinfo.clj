;    Copyright (C) 2014-2019, 2023  Joseph Fosco. All Rights Reserved
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

(ns splice.instr.instrumentinfo)

(defrecord InstrumentInfo [instrument envelope-type range-hi range-lo note-off])

(defn create-instrument-info
  "Used to create an InstrumentInfo record"
  [& {:keys [instrument envelope-type range-hi range-lo note-off]}]
  (InstrumentInfo. instrument
                   envelope-type
                   range-hi
                   range-lo
                   note-off
                   )
  )

(defn get-instrument-from-instrument-info
  [inst-info]
  (:instrument inst-info))

(defn get-envelope-type-from-instrument-info
  [inst-info]
  (:envelope-type inst-info))

(defn get-range-hi-from-instrument-info
  [inst-info]
  (:range-hi inst-info))

(defn get-range-lo-from-instrument-info
  [inst-info]
  (:range-lo inst-info))

(defn get-note-off-from-instrument-info
  [inst-info]
  (:note-off inst-info))

(defn get-all-instrument-info
  [inst-info]
  {:envelope-type (:envelope-type inst-info)
   :range-hi (:range-hi inst-info)
   :range-lo (:range-lo inst-info)
   :instrument (:instrument inst-info)
   :note-off (:note-off inst-info)}
  )
