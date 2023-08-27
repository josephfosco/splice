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
   [splice.util.settings :refer [get-setting set-setting! update-settings!]]
   )
  )

(defn- convert-midi-to-hz
  [loop-event]
  (if (contains? (:pitch loop-event) :pitch-midi-note )
    (let [new-pitch-map (assoc (dissoc (:pitch loop-event) :pitch-midi-note)
                               :pitch-freq
                               (midi->hz (:pitch-midi-note (:pitch loop-event))))
          ]
      (assoc loop-event :pitch new-pitch-map)
      )
    loop-event
    )
  )

(defn validate-and-adjust-loop
  [loop]
  (let [new-melody-info (doall (map convert-midi-to-hz loop))
        ]
    new-melody-info
    )
  )
