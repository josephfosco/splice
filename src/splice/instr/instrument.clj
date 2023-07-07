;    Copyright (C) 2013-2019, 2023  Joseph Fosco. All Rights Reserved
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

(ns splice.instr.instrument
  (:require
   [splice.instr.instrumentinfo :refer [create-instrument-info
                                         get-instrument-from-instrument-info
                                         get-envelope-type-from-instrument-info
                                         get-range-hi-from-instrument-info
                                         get-range-lo-from-instrument-info
                                         get-note-off-from-instrument-info
                                         ]]
   [splice.music.music :refer [midi->hz MIDI-RANGE]]
   [splice.util.log :as log]
   [splice.util.print :refer [print-msg]]
   )
  )

(def perc-env (sorted-set "AD" "NE"))
(def non-perc-env (sorted-set "ADSR" "ASR"))

;; Note: when defining instruments
;;       :freq should always be the first param
;;       :vol should always be the second param
;;       :pan working to standardize on pan being the third param
;;       :release should be the param name for :envelope release time in secs
(def all-instruments {:bass-m1
                      {:instrument "bass-m1"
                       :envelope-type "NE"
                       :range-lo 25
                       :range-hi 60
                       :note-off false}
                      :bassoon
                      {:instrument "bassoon"
                       :envelope-type "ASR"
                       :range-lo 25
                       :range-hi 84
                       :note-off true}
                      :clarinet
                      {:instrument "clarinet"
                       :envelope-type "ASR"
                       :range-lo 20
                       :range-hi 100
                       :note-off true}
                      :drum-m1
                      {:instrument "drum-m1"
                       :envelope-type "AD"
                       :range-lo 50
                       :range-hi 90
                       :note-off false}
                      :flute
                      {:instrument "flute"
                       :envelope-type "ASR"
                       :range-lo 59
                       :range-hi 96
                       :note-off true}
                      :gong
                      {:instrument "gong"
                       :envelope-type "AR"
                       :range-lo 20
                       :range-hi 40
                       :note-off false}
                      :kalimba
                      {:instrument "kalimba"
                       :envelope-type "AR"
                       :range-lo 35
                       :range-hi 101
                       :note-off false}
                      :rhodes-piano
                      {:instrument "rhodes-piano"
                       :envelope-type "ADSR"
                       :range-lo 40
                       :range-hi (last MIDI-RANGE)
                       :note-off true}
                      :organ-m1
                      {:instrument "organ-m1"
                       :envelope-type "ADSR"
                       :range-lo 40
                       :range-hi (last MIDI-RANGE)
                       :note-off true}
                      :plink-m1
                      {:instrument "plink-m1"
                       :envelope-type "AD"
                       :range-lo 33
                       :range-hi (last MIDI-RANGE)
                       :note-off false}
                      :pluck-string
                      {:instrument "pluck-string"
                       :envelope-type "NE"
                       :range-lo 37
                       :range-hi 89
                       :note-off false}
                      :reedy-organ
                      {:instrument "reedy-organ"
                       :envelope-type "ASR"
                       :range-lo 20
                       :range-hi (last MIDI-RANGE)
                       :note-off true}
                      :steel-drum
                      {:instrument "steel-drum"
                       :envelope-type "AD"
                       :range-lo 45
                       :range-hi (last MIDI-RANGE)
                       :note-off false}
                      :sine-wave-sus
                      {:instrument "sine-wave-sus"
                       :envelope-type "ASR"
                       :range-lo (first MIDI-RANGE)
                       :range-hi (last MIDI-RANGE)
                       :note-off true}
                      :saw-wave-sus
                      {:instrument "saw-wave-sus"
                       :envelope-type "ASR"
                       :range-lo 25
                       :range-hi (last MIDI-RANGE)
                       :note-off true}
                      :string-sect
                      {:instrument "string-sect"
                       :envelope-type "ASR"
                       :range-lo 40
                       :range-hi 96
                       :note-off true}
                      :tri-wave-sus
                      {:instrument "tri-wave-sus"
                       :envelope-type "ASR"
                       :range-lo 15
                       :range-hi (last MIDI-RANGE)
                       :note-off true}
                      :woosh
                      {:instrument "woosh"
                       :envelope-type "AR"
                       :range-lo 15
                       :range-hi 40
                       :note-off false}
                      :wurlitzer-piano
                      {:instrument "wurlitzer-piano"
                       :envelope-type "ADSR"
                       :range-lo 40
                       :range-hi (last MIDI-RANGE)
                       :note-off true}
                      })

(defn non-perc-instruments
  [instrument-list]
  (filter #(not (contains? perc-env (:envelope-type %1))) instrument-list)
  )

(defn note->hz
  [music-note]
  (midi->hz music-note))

(defn get-instrument
  [instrument-name]
  (let [inst-info (instrument-name all-instruments)]
    (when (nil? inst-info)
      (throw (Throwable. (str "Invalid :instrument-name "
                              instrument-name
                              " in loop"))))
    (create-instrument-info
     :instrument (:instrument inst-info)
     :envelope-type (:envelope-type inst-info)
     :range-hi (:range-hi inst-info)
     :range-lo (:range-lo inst-info)
     :note-off (get inst-info :note-off false)
     )
    )
 )

(defn get-gate-dur
  "player - player map
   note-duration - note duration in milliseconds"
  [player note-duration]
  (if (> note-duration 110)
    (/ (- note-duration 110) 1000.0) ; currently assumes an attack of .01 secs and decay of .1 secs
    0.001))

(defn select-random-instrument
  "Selects random instrument-info for player."
  []
  (let [inst-info (rand-nth all-instruments)
        ]
    (create-instrument-info
     :instrument (get-instrument-from-instrument-info inst-info)
     :envelope-type (get-envelope-type-from-instrument-info inst-info)
     :range-hi (get-range-hi-from-instrument-info inst-info)
     :range-lo (get-range-lo-from-instrument-info inst-info)
     :note-off (get-note-off-from-instrument-info inst-info))
    ))

(defn select-instrument
  [ensemble player melody player-id]
  (select-random-instrument)
  )

;; (defn select-instrument
;;   "Selects instrument-info for player.

;;    player - the player to get instrument for"
;;   [player]
;;   (let [instrument-list all-instruments
;;         inst-info (if (empty? instrument-list)
;;                     (do
;;                       (log/warn (log/format-msg "select-instrument" "*** INSTRUMENT LIST EMPTY *************************"))
;;                       (log/warn (log/format-msg "select-instrument"
;;                                                 "player-id: "
;;                                                 (get-player-id player)
;;                                                 ))
;;                       (rand-nth all-instruments)
;;                       )
;;                     (rand-nth instrument-list)
;;                     )
;;         ]

;;     (create-instrument-info
;;      :instrument (get-instrument-for-inst-info inst-info)
;;      :envelope-type (get-envelope-type-for-inst-info inst-info)
;;      :range-hi (get-range-hi-for-inst-info inst-info)
;;      :range-lo (get-range-lo-for-inst-info inst-info)
;;      :note-off (get-note-off-for-inst-info inst-info)
;;      )
;;     )
;;   )

(defn has-release?
  [inst-info]
  (if (contains? non-perc-env (:envelope-type inst-info))
    true
    false)
  )

(defn get-instrument-info-for-name
  [instrument-name]
  (first (filter #(= (:name (:instrument %1)) instrument-name) all-instruments))
  )
