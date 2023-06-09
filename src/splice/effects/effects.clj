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

;; (ns splice.effects.effects
;;   (:require
;;    [overtone.live :refer [fx-freeverb defsynth replace-out in free-verb2]]
;;    )
;;   )

;; (defsynth fx-freeverb2
;;   "Uses the free-verb2 (stereo) ugen."
;;   [bus1 0 bus2 1 wet-dry 0.5 room-size 0.5 dampening 0.5]
;;   (let [source1 (in bus1)
;;         source2 (in bus2)
;;         verbed (free-verb2 source1 source2 wet-dry room-size dampening)]
;;     (replace-out [bus1 bus2] (* 1.4 verbed))))

;; (defn reverb
;;   [& {:keys [wet-dry room-size dampening]
;;     :or {wet-dry 0.5 room-size 0.5 dampening 0.2}}
;;    ]
;;   (println wet-dry room-size dampening)
;;   (def fxrvb (fx-freeverb2
;;                         :wet-dry wet-dry
;;                         :room-size room-size
;;                         :dampening dampening))
;;   )
