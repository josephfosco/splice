;    Copyright (C) 2013-2019  Joseph Fosco. All Rights Reserved
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

(ns splice.instr.instruments.trad-instruments
  ^{:author "Joseph Fosco"
    :doc "Electronic imitations of traditional instruments"}
  (:require
   [overtone.live :refer :all]
   ))

(definst bassoon
  [freq 110 vol 1.0 release 0.1 attack 0.01 sustain 0.3 gate 1.0 action FREE]
  (-> (saw freq)
      (bpf (* 2 freq) 2.0)
      (* (env-gen (asr attack sustain release) gate vol 0 1 action))
      (* vol 4)
      )
  )

(definst clarinet
  [freq 440 vol 1.0 release 0.1 attack 0.01 sustain 0.3 gate 1.0 action FREE]
  (-> (square [freq (* freq 1.01) (* freq 0.99)])
      (lpf (line:kr (* freq 8) (* freq 2) 0.5))
      (* (env-gen (asr attack sustain release) gate vol 0 1 action))
      (* vol 1.2)
      )
  )

(definst pluck-string
  [freq 440 vol 1.0 gate 1.0 action FREE]
  (let [dur (/ 440 freq)]
    (-> (pluck (white-noise) 1 1 (/ 1 freq) dur)
        (* (env-gen (perc 0.0 dur) :action action))
        (* vol)
        ))
)

(defsynth flute
  [freq 440 vol 1.0 attack 0.15 sustain 1.0 release 0.1 gate 1.0 action FREE]
     (let[eg  (env-gen (asr attack sustain release :curve [-3 1 -2]) gate 1 0 1 FREE)
          ]
       (out [0 1]
        (-> (rlpf
             (-> (* (lf-saw :freq freq) 0.5) )
             (+ (* eg 60) freq (* (sin-osc:kr 3.5) 40))
             0.7
             )
            (* eg)
            (* vol)
            ))
       ))

(defsynth string-sect
  [freq 440 vol 1 attack 0.3 sustain 1.0 release 0.3 gate 1.0]
     (let[eg  (env-gen (asr attack sustain release :curve [-3 1 -2]) gate 1 0 1 FREE)
          ]
       (out [0 1]
        (-> (lpf
             (-> (pulse :freq freq :width (+ 0.5 (* 0.4 (sin-osc:kr 3))))
                 (+ (var-saw :freq (+ freq (* freq 0.01)) :width 0))
                 (* 0.3)
                 )
             2000
             )
            (* eg vol)
            ))
       ))
