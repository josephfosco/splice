;    Copyright (C) 2018-2019, 2023  Joseph Fosco. All Rights Reserved
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
   [splice.instr.instrumentinfo :refer [get-note-off-from-instrument-info]]
   [splice.melody.melody-event :refer [create-melody-event]]
   [splice.player.loops.base-loop :refer [create-base-loop
                                          get-loop-dur-info
                                          get-loop-pitch
                                          get-loop-volume
                                          ]]
   [splice.player.loops.looptype :refer [LoopType
                                         get-name
                                         get-loop-repetition
                                         set-loop-repetition]]
   [splice.player.player-utils :refer [get-player-id
                                       get-player-instrument-info]]
   )
  )

(defrecord Loop [melody-info
                 next-melody-event-ndx
                 core-loop
                 ]
  LoopType
  (get-name [loop] (get-name (:core-loop loop)))
  (get-loop-repetition [loop] (get-loop-repetition (:core-loop loop)))
  (set-loop-repetition
    [loop loop-rep]
    (assoc loop :core-loop (set-loop-repetition (:core-loop loop) loop-rep))
    )
  )

(defn get-melody-info
  [loop]
  (:melody-info loop)
  )

(defn play-event?
  [play-prob]
  (if (< (rand-int 100) play-prob)
    true
    false)
  )

(defn get-next-melody-event-ndx
  [loop-structr]
  (:next-melody-event-ndx loop-structr)
  )

(defn- compute-next-melody-event-ndx
  "Returns the next loop-event index to use starting at start-ndx
   checks each loop-events :play-prob (play probability) if it exists
  "
  [loop-structr start-ndx]
  ;; ndx in the for will range between 0 and start-ndx, but will start at start-ndx
  ;; If there is a play-prob, for the next melody-info event it will check if it
  ;; should play the event based on the play-prob. If it should it will return that
  ;; ndx, otherwise it will continue trying with the next ndx until it finds the event
  ;; ndx it should play. If there is no play-prob for the event, it will ruturn the next
  ;; event ndx.

  (first
   (for [ndx (iterate
              #(mod (inc %1)
                    (count (:melody-info loop-structr)))
              start-ndx)
         :when (let [play-prob (:play-prob ((:melody-info loop-structr) ndx))]
                 (if play-prob
                   (play-event? play-prob)
                   ndx
                   ))
         ]
     ndx)
         )
  )

(defn- update-loop-structr
  [loop-structr melody-ndx loop-rep]
  (let [new-loop-struct
        (if loop-rep
          (assoc (set-loop-repetition loop-structr loop-rep)
                 :next-melody-event-ndx (mod (inc melody-ndx)
                                             (count (:melody-info
                                                     loop-structr))))
          (assoc loop-structr :next-melody-event-ndx (mod (inc melody-ndx)
                                                          (count (:melody-info
                                                                  loop-structr))))
          )
        ]
    new-loop-struct
    )
    )

(defn get-next-melody
  "Returns an updated loop structure with the :next-melody-event-ndx updated and
  a new melody-event. loop-structr must be a Loop record."
  [& {:keys [player melody loop-structr next-melody-event-id event-time inc-reps?]
      :or {inc-reps? true}
      }]
  (let [melody-ndx (compute-next-melody-event-ndx loop-structr
                                                  (:next-melody-event-ndx loop-structr))
        melody-info ((:melody-info loop-structr) melody-ndx)
        instrument-info (get-player-instrument-info player)
        ;; frequency can be nil when pitch-type is variable and one or more entries in
        ;; the pitch vector is nil, and the nil value is chosen
        frequency (get-loop-pitch loop-structr (:pitch melody-info))
        event-note-off (if (false? (get-note-off-from-instrument-info instrument-info))
                         true
                         nil)
        melody-event (create-melody-event
                      :melody-event-id next-melody-event-id
                      :freq frequency
                      :dur-info (get-loop-dur-info loop-structr (:dur melody-info))
                      :volume (get-loop-volume (:volume melody-info))
                      :instrument-info instrument-info
                      :instrument-settings (:instrument-settings melody-info)
                      :player-id (get-player-id player)
                      :event-time event-time
                      :note-off (if (or (= :rest (:type (:pitch melody-info)))
                                        (nil? frequency))
                                  nil
                                  event-note-off)
                      )
        begining-of-loop? (= (get-next-melody-event-ndx loop-structr) 0)
        loop-rep (if (and inc-reps? begining-of-loop?)
                   (inc (get-loop-repetition loop-structr))
                   nil
                   )
        ]
    [
     ;; TODO Will probably need to figure out how to place this in the correct place
     ;;      Also look for any other assoc in the loop files
     (update-loop-structr loop-structr melody-ndx loop-rep)
     melody-event
     ]

    )
  )

(defn create-loop
  [& {:keys [name melody-info next-melody-event-ndx next-melody-fn]
      :or {next-melody-fn get-next-melody}}]
  (Loop. melody-info
         next-melody-event-ndx
         (create-base-loop :name name
                           :next-melody-fn next-melody-fn)
         )
  )
