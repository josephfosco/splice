;    Copyright (C) 2017  Joseph Fosco. All Rights Reserved
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

(ns transit.player.structures.random-event
  (:require
   [transit.melody.melody-event :refer [create-melody-event]]
   [transit.melody.pitch :refer [select-random-pitch]]
   [transit.melody.rhythm :refer [select-random-rhythm]]
   [transit.melody.volume :refer [select-random-volume]]
   [transit.player.structures.base-structure :refer [create-base-structure
                                                     get-structr-id]]
   )
)

(defrecord RandomEvent [base rest?])

(defn get-random-note-event
  [player next-id rnd-evnt]
  (create-melody-event
   :melody-event-id next-id
   :note (select-random-pitch (:range-lo (:instrument-info player))
                              (:range-hi (:instrument-info player)))
   :dur-info (select-random-rhythm)
   :volume (select-random-volume)
   :instrument-info (:instrument-info player)
   :player-id (:id player)
   :event-time nil
   :structr-id (get-structr-id rnd-evnt)
   )
  )

(defn get-random-rest-event
  [player next-id rnd-evnt]
  (create-melody-event
   :melody-event-id next-id
   :note nil
   :dur-info (select-random-rhythm)
   :volume nil
   :instrument-info nil
   :player-id (:id player)
   :event-time nil
   :structr-id (get-structr-id rnd-evnt)
   )
  )

(defn get-random-melody-event
  [ensemble player melody player-id rnd-evnt next-id]
  (if (or (:rest? rnd-evnt) (= (rand-int 2) 0))
    [rnd-evnt (get-random-rest-event player next-id rnd-evnt)]
    [rnd-evnt (get-random-note-event player next-id rnd-evnt)]
    )
  )

(defn create-random-structr
  [& {:keys [structr-id
             internal-strength
             external-strength
             rest?] :or
      {internal-strength 0
       external-strength 0
       rest? nil}}]
  (RandomEvent. (create-base-structure :structr-id structr-id
                                       :internal-strength internal-strength
                                       :external-strength external-strength
                                       :melody-fn get-random-melody-event)
                 rest?
                 )
  )
