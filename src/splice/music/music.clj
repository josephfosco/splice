(ns splice.music.music)

;; Function copied from the Overtone project overtone.sc.overtone.music.pitch.clj

(def MIDI-RANGE (range 128))

; midicps
(defn midi->hz
  "Convert a midi note number to a frequency in hz."
  [note]
  (* 440.0 (java.lang.Math/pow 2.0 (/ (- note 69.0) 12.0))))

;; Function copied from the Overtone project overtone.sc.overtone.music.pitch.clj
; cpsmidi
(defn hz->midi
  "Convert from a frequency to the nearest midi note number."
  [freq]
  (java.lang.Math/round (+ 69
                 (* 12
                    (/ (java.lang.Math/log (* freq 0.0022727272727))
                       (java.lang.Math/log 2))))))
