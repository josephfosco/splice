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

(ns splice.instr.sc-instrument
  (:require
   [sc-osc.sc :refer [sc-send-msg]]
  ;;  [overtone.live :refer :all]
    )
  )

(defn stop-instrument
  [sc-instrument-id]
  (sc-send-msg "n_set" sc-instrument-id 0.0)
  )

(defn get-release-millis-from-instrument
  ""
  [sc-instrument-id]
  ;; (*  (node-get-control sc-instrument-id :release) 1000)
  (throw (Throwable. "COMMENTED OUT CODE in splice.instr.sc-instrument/get-release-millis-from-instrument"))
  )
