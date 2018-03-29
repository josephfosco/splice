;    Copyright (C) 2018  Joseph Fosco. All Rights Reserved
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

(ns splice.player.loops.loop
  (:require
   [overtone.live :refer [midi->hz]]
   [splice.melody.melody-event :refer [create-melody-event]]
   [splice.melody.dur-info :refer [create-dur-info]]
   [splice.player.loops.base-loop :refer [create-base-loop]]
   [splice.player.player-utils :refer [get-player-id
                                       get-player-instrument-info]]
   )
  )

(defrecord Loop [melody-info
                 next-melody-event-ndx
                 base-loop
                 ])

(defn get-next-melody
  "Returns an updated loop structure and a melody-event"
  [player melody loop-structr next-id]
  (let [melody-ndx (:next-melody-event-ndx loop-structr)
        melody-info ((:melody-info loop-structr) melody-ndx)
        melody-event (create-melody-event
                      :melody-event-id next-id
                      :freq (or (:pitch-freq melody-info)
                                (if-let [note-no (:pitch-midi-note melody-info)]
                                  (midi->hz note-no)
                                  ))
                      :dur-info (create-dur-info
                                 :dur-millis (:dur-millis melody-info)
                                 :dur-beats (:dur-beats melody-info)
                                 )
                      :volume 1.0
                      :instrument-info (get-player-instrument-info player)
                      :player-id (get-player-id player)
                      :event-time nil
                      )
        ]
    [
     (assoc loop-structr :next-melody-event-ndx (mod (inc melody-ndx)
                                                     (count (:melody-info
                                                             loop-structr))))
     melody-event
     ]

    )
 )

(defn create-loop
  [& {:keys [melody-info next-melody-event-ndx]}]
  (Loop. melody-info
         next-melody-event-ndx
         (create-base-loop :next-melody-fn get-next-melody)
         )
  )
