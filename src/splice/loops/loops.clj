;    Copyright (C) 2023  Joseph Fosco. All Rights Reserved
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

(ns splice.loops.loops
  (:require
   [splice.music.music :refer [midi->hz]]
   [splice.util.log :as log]
   [splice.util.settings :refer [get-setting set-setting! update-settings!]]
   )
  )

(def valid-loop-keys (set '(:instrument-name
                            :loop-type
                            :melody-info
                            :name
                            :min-new-loop-delay-ms
                            :max-new-loop-delay-ms
                            :max-num-mult-loops
                            :reps-before-multing
                            :num-mult-loops-started
                            :loop-mult-probability
                            )))

(defn- validate-loop-keys
  [loop-settings]
  ;; TODO validate loop keys based on type of loop
  (flatten
   (for [loop loop-settings]
     (let [loop-keys (keys loop)]
       (for [loop-key loop-keys
             :when (not (contains? valid-loop-keys loop-key))]
         (str "control.clj - validate-loop-keysInvalid loop key " loop-key " in player-settings")
         )
       )
     ))
  )

(defn- validate-player-settings
  [player-settings]
  (let [loop-key-msgs (validate-loop-keys (:loops player-settings))]
    (cond-> '()
      (< (count (:loops player-settings)) 1)
      (conj "loops.clj/validate-player-settings - :loops not found in player-settings file")
      (not= (count loop-key-msgs) 0)
      ((partial reduce conj) loop-key-msgs)
      )
    ))

(defn- convert-midi-to-hz
  [loop]
  (let [new-melody-info
        (vec
         (for [melody-event (:melody-info loop)]
           (if (or (contains? (:pitch melody-event) :pitch-midi-note)
                   (contains? (:pitch melody-event) :pitch-type))
             ;; a single pitch
             (if (= (:type (:pitch melody-event)) :fixed)
               (let [new-pitch-map (assoc (dissoc (:pitch melody-event) :pitch-midi-note)
                                          :pitch-freq
                                          (midi->hz (:pitch-midi-note (:pitch melody-event))))
                     ]
                 (assoc melody-event :pitch new-pitch-map)
                 )
               ;; a vector of pitches
               (let [new-pitch-map (assoc (dissoc (:pitch melody-event) :pitches)
                                          :pitch-type
                                          :freq
                                          :pitches
                                          (vec
                                           (for [midi-note (:pitches (:pitch melody-event))]
                                             (if (= midi-note nil)
                                               midi-note
                                               (midi->hz midi-note))
                                             )))
                     ]
                 (assoc melody-event :pitch new-pitch-map)
                 )
               )
             melody-event
             )))]
    (assoc loop :melody-info new-melody-info)
    )
  )

(defn validate-and-adjust-loops
  [loops]
  ;; Validate loops then convert any loop events that specify pitch by midi-not-num to
  ;; specify pitch by frequency
  (let [errors (validate-player-settings loops)]
    (if (not= 0 (count errors))
      (do
        (doseq [error-msg errors]
          (log/error error-msg))
        (throw (Throwable. "Validation error(s) in player loops"))
        )
      (doall (map convert-midi-to-hz (:loops loops)))
      ))
    )
