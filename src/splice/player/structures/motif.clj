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

(ns transit.player.structures.motif
  (:require
   [transit.melody.melody-event :refer [create-melody-event]]
   [transit.melody.rhythm :refer [select-random-rhythm]]
   [transit.player.structures.base-structure :refer [create-base-structure]]
   )
  )


(def ^:private MOTIF-MIN-LEN 2)
(def ^:private MOTIF-MAX-LEN 12)
(def ^:private MOTIF-MAX-SKIP 12)
(def ^:private MOTIF-MAX-NUM-SKIPS 3)
(def ^:private MOTIF-MIN-MILLIS 200)
(def ^:private MOTIF-MAX-MILLIS 1000)

(defrecord Motif [base
                  motif-events
                  type  ;; FREE or METERED (METERED has mm and rhythmic values)
                  complete? ;; is this a complete motif
                  ])

(defn find-motif
 " Looks through past melody events for material that can be used to
   create a motif"
  [melody]
  (let [possible-motif
        (for [event (reverse melody) :while (not= nil (:note event))]
          {:note (:note event) :dur-info (:dur-info event)})]
    (if (>= (count possible-motif) MOTIF-MIN-LEN)
      possible-motif
      nil
      )
    )
 )

(defn next-melody-event
  [ensemble player melody player-id motif next-id]
  (if-let [motif (find-motif melody)]
    (do
      [motif (create-melody-event :melody-event-id next-id
                                  :note 72
                                  :dur-info (select-random-rhythm)
                                  :volume 0.7
                                  :instrument-info (:instrument-info player)
                                  :player-id (:id player)
                                  :event-time nil
                                  )
       ])
    [motif (create-melody-event :melody-event-id next-id
                                :note 60
                                :dur-info (select-random-rhythm)
                                :volume 0.7
                                :instrument-info (:instrument-info player)
                                :player-id (:id player)
                                :event-time nil
                                )
     ]
    )
  )

(defn get-motif-melody-event
  [ensemble player melody player-id motif next-id]
  [motif
   (next-melody-event ensemble player melody player-id motif next-id)
   ]
  )

(defn create-motif
  [& {:keys [structr-id
             internal-strength
             external-strength
             motif-events
             type
             complete?] :or
      {internal-strength 0
       external-strength 0
       melody-events []
       complete? false
       }}]
  (Motif. (create-base-structure :structr-id structr-id
                                 :internal-strength internal-strength
                                 :external-strength external-strength
                                 :melody-fn get-motif-melody-event)
          motif-events
          type
          complete?
          )
  )
