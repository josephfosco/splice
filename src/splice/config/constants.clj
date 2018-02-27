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

(ns splice.config.constants
  (:require
   [overtone.live :refer [midi->hz MIDI-RANGE]]
   )
  )

(def SAVED-MELODY-LEN 64)
;; STATUS_UPDATE_MILLIS is the number of millis between updates to
;; player or ensemble status information
(def STATUS-UPDATE-MILLIS 2000)

(def FREE 0)
(def METERED 1)

(def DECREASING 0)
(def STEADY 1)
(def INCREASING 2)

;; midi notes below 16 are less than 20 hz
(def MIN-NOTE 16)
(def MAX-NOTE (last MIDI-RANGE))
(def MIN-FREQ (midi->hz MIN-NOTE))
(def MAX-FREQ (midi->hz MAX-NOTE))
