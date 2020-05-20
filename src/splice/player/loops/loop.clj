;    Copyright (C) 2018-2019  Joseph Fosco. All Rights Reserved
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
   [splice.player.loops.base-loop :refer [create-base-loop
                                          get-loop-dur-info
                                          get-next-melody
                                          ]]
   )
  )

(defrecord Loop [melody-info
                 next-melody-event-ndx
                 base-loop
                 ])

(defn get-loop-pitch
  [pitch-info]
  (condp = (:type pitch-info)
    :fixed (or (:pitch-freq pitch-info)
               (if-let [note-no (:pitch-midi-note pitch-info)]
                 (midi->hz note-no)
                 ))
    :variable (let [pitch (rand-nth (:pitches pitch-info))]
                (if (and pitch (= :midi-note (:pitch-type pitch-info)))
                  (midi->hz pitch)
                  pitch) ;; :pitch-type = :freq or pitch is nil (rest)
                )
    )
  )

(defn create-loop
  [& {:keys [name melody-info next-melody-event-ndx]}]
  (Loop. melody-info
         next-melody-event-ndx
         (create-base-loop :name name
                           :next-melody-fn get-next-melody
                           :pitch-fn get-loop-pitch)
         )
  )
