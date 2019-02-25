;    Copyright (C) 2019  Joseph Fosco. All Rights Reserved
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

(ns splice.effects.effects
  (:require
   [overtone.live :refer [fx-freeverb]]
   )
  )

(defn reverb
  [& {:keys [wet-dry room-size dampening]
    :or {wet-dry 0.5 room-size 0.5 dampening 0.2}}
   ]
  (println wet-dry room-size dampening)
  (def fxl (fx-freeverb :bus 0
                        :wet-dry wet-dry
                        :room-size room-size
                        :dampening dampening))
  (def fxr (fx-freeverb :bus 1
                        :wet-dry wet-dry
                        :room-size room-size
                        :dampening dampening))
  )
