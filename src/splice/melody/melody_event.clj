;    Copyright (C) 2017-2019, 2023  Joseph Fosco. All Rights Reserved
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

(ns splice.melody.melody-event
  (:require
   [splice.melody.dur-info :refer [get-dur-millis-from-dur-info
                                    get-dur-beats-from-dur-info]]
   )
  )

(defrecord MelodyEvent [melody-event-id
                        freq
                        dur-info
                        volume
                        instrument-info
                        instrument-settings
                        player-id
                        event-time  ; this is the time the event will start peocessing
                        play-time   ; this is the time the note will start playing
                        sc-synth-id
                        note-off
                        ])

;; MelodyEvent fields
;;  id   - id sequence of melody event - 0 is initial blank event
;;  freq - pitch frequency of event - nil for rest
;;  event-time - time (in millis) event was ssheduled
;;  play-time  - time (in millis) event was scheduled to be actually played
;;  note-off - true if a note-off was scheduled for this event note or not needed
;;               (instrument-info note-off is false)
;;             false if note-off event was not scheduled for this note and is needed
;;             nil if this event is a rest (freq = nil) or it is not known if note-off is needed

(defn create-melody-event
  [& {:keys [melody-event-id
             freq
             dur-info
             volume
             instrument-info
             instrument-settings
             player-id
             event-time
             play-time
             sc-synth-id
             note-off
             ]
      :or {instrument-settings nil
           play-time nil
           sc-synth-id nil}}]
  (MelodyEvent. melody-event-id
                freq
                dur-info
                volume
                instrument-info
                instrument-settings
                player-id
                event-time
                play-time
                sc-synth-id
                note-off
                )
  )

(defn create-rest-event
  [player-id event-id event-time]
  (create-melody-event :melody-event-id event-id
                         :freq nil
                         :dur-info nil
                         :volume nil
                         :instrument-info nil
                         :player-id player-id
                         :event-time event-time
                         :note-off nil
                         )
  )

(defn set-play-info
  [melody-event sc-synth-id play-time note-off-val]
  (let [constant-params {:play-time play-time :sc-synth-id sc-synth-id}
        update-params (if (= note-off-val :none)
                        constant-params
                        (assoc constant-params :note-off note-off-val)
                        )
        ]
    (merge melody-event update-params))
    )

(defn set-melody-event-note-off
  [melody-event val]
  (assoc melody-event
         :note-off val
         ))

(defn get-melody-event-id-from-melody-event
  [melody-event]
  (:melody-event-id melody-event)
  )

(defn get-dur-info-from-melody-event
  [melody-event]
  (:dur-info melody-event)
  )

(defn get-dur-beats-from-melody-event
  [melody-event]
  (get-dur-beats-from-dur-info (get-dur-info-from-melody-event melody-event))
  )

(defn get-dur-millis-from-melody-event
  [melody-event]
  (get-dur-millis-from-dur-info (get-dur-info-from-melody-event melody-event))
  )

(defn get-event-time-from-melody-event
  [melody-event]
  (:event-time melody-event)
  )

(defn get-play-time-from-melody-event
  [melody-event]
  (:play-time melody-event)
  )

(defn get-instrument-info-from-melody-event
  [melody-event]
  (:instrument-info melody-event)
  )

(defn get-instrument-settings-from-melody-event
  [melody-event]
  (:instrument-settings melody-event)
  )

(defn get-freq-from-melody-event
  [melody-event]
  (:freq melody-event)
  )

(defn get-note-off-from-melody-event
  [melody-event]
  (:note-off melody-event)
  )

(defn get-player-id-from-melody-event
  [melody-event]
  (:player-id melody-event)
  )

(defn get-sc-synth-id-from-melody-event
  [melody-event]
  (:sc-synth-id melody-event)
  )

(defn get-volume-from-melody-event
  [melody-event]
  (:volume melody-event)
  )

(defn print-melody-event
  [melody-event]
  (let [inst-inf (:instrument-info melody-event)
        melody-map (into (sorted-map)
                         (assoc
                          melody-event
                          :instrument-info
                          {:name (:name (:instrument inst-inf))
                           :range-lo (:range-lo inst-inf)
                           :range-hi (:range-hi inst-inf)}
                          :dur-info
                          (into {} (:dur-info melody-event))
                          ))
        ]
    (println melody-map)
    )
  )
