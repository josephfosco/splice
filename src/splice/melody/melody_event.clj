;    Copyright (C) 2017-2018  Joseph Fosco. All Rights Reserved
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
   [splice.instr.instrumentinfo :refer [get-release-millis-from-instrument-info]]
   [splice.instr.sc-instrument :refer [get-release-millis-from-instrument]]
   [overtone.live :refer [node-get-control]]
   )
  )

(defrecord MelodyEvent [melody-event-id
                        freq
                        dur-info
                        volume
                        instrument-info
                        instrument-settings
                        player-id
                        event-time
                        play-time
                        sc-instrument-id
                        note-off
                        ])

;; MelodyEvent fields
;;  id   - id sequence of melody event - 0 is initial blank event
;;  freq - pitch frequency of event - nil for rest
;;  event-time - time (in millis) event was supposed to be played
;;  play-time  - time (in millis) event was actually played
;;  note-off - true if a note-off was scheduled for this event note
;;             false if note-off event was not scheduled for this note
;;             nil if this event is a rest (freq = nil)

(defn sched-note-off?
  "If this is not a rest and
    this note has a release
    the note length > release
  then return true
  else return false"
  [freq dur-info instrument-info]

  (if (and (not (nil? freq))
           (> (get-dur-millis-from-dur-info dur-info)
              (get-release-millis-from-instrument-info instrument-info)
              )
           )
    true
    false)
  )

(defn create-melody-event
  [& {:keys [:melody-event-id
             :freq
             :dur-info
             :volume
             :instrument-info
             :instrument-settings
             :player-id
             :event-time
             :play-time
             :sc-instrument-id
             ]
      :or {:instrument-settings nil
           :play-time nil
           :sc-instrument-id nil}}]
  (MelodyEvent. melody-event-id
                freq
                dur-info
                volume
                instrument-info
                instrument-settings
                player-id
                event-time
                play-time
                sc-instrument-id
                nil ;; note-off is nil if this is a rest or we don't know yet
                )
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

(defn get-release-millis-from-melody-event
  [melody-event]
  (get-release-millis-from-instrument (:sc-instrument-id melody-event))
  )

(defn get-sc-instrument-id-from-melody-event
  [melody-event]
  (:sc-instrument-id melody-event)
  )

(defn get-volume-from-melody-event
  [melody-event]
  (:volume melody-event)
  )

(defn set-play-info
  [melody-event sc-instrument-id event-time play-time]
  (assoc melody-event
         :event-time event-time
         :play-time play-time
         :sc-instrument-id sc-instrument-id
         :note-off (if sc-instrument-id
                       (> (get-dur-millis-from-melody-event melody-event)
                          (get-release-millis-from-instrument sc-instrument-id)
                          ))

         ))

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
