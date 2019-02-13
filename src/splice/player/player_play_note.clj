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

(ns splice.player.player-play-note
  (:require
   [clojure.core.async :refer [>!!]]
   [overtone.live :refer [apply-at ctl midi->hz]]
   [splice.instr.instrumentinfo :refer [get-release-millis-from-instrument-info
                                         get-instrument-from-instrument-info]]
   [splice.instr.sc-instrument :refer [stop-instrument]]
   [splice.config.constants :refer [SAVED-MELODY-LEN]]
   [splice.ensemble.ensemble :refer [get-ensemble-clear-msg-for-player-id
                                      get-melody
                                      get-player
                                      update-player-and-melody]]
   [splice.melody.dur-info :refer [get-dur-millis-from-dur-info]]
   [splice.melody.melody-event :refer [get-dur-info-from-melody-event
                                       get-dur-millis-from-melody-event
                                       get-event-time-from-melody-event
                                       get-freq-from-melody-event
                                       get-instrument-info-from-melody-event
                                       get-instrument-settings-from-melody-event
                                       get-note-off-from-melody-event
                                       get-player-id-from-melody-event
                                       get-release-millis-from-melody-event
                                       get-sc-instrument-id-from-melody-event
                                       get-volume-from-melody-event
                                       set-play-info]]
   [splice.player.player-utils :refer [get-next-melody-event
                                       NEXT-METHOD]]
   [splice.util.settings :refer [get-setting]]
   [splice.util.util :refer [get-msg-channel]]
   )
  )

(def NEXT-NOTE-PROCESS-MILLIS 2)

(defn is-playing?
 "Returns:
   true - if player is playing now
   false - if player is not playing now
 "
 [player]
  )

(defn update-melody-with-event
  [melody melody-event]
  ;; Adds melody-event to the end of the melody vector
  ;; Does not allow melody-vector to have more than SAVED-MELODY-LEN
  ;; elements in it
  (if (= (count melody) SAVED-MELODY-LEN)
    (assoc (subvec melody 1) (dec SAVED-MELODY-LEN) melody-event)
    (assoc melody (count melody) melody-event)
    )
  )

(defn check-prior-event-note-off
   " if the prior note was not turned off and
       either this note is a rest or
         this note has a different instrument than the prior note
     then
       turn off the prior note"
  [prior-melody-event cur-melody-event]
  (when (and (false? (get-note-off-from-melody-event prior-melody-event))
             (or (not (nil? (get-freq-from-melody-event cur-melody-event)))
                 (not=
                  (get-sc-instrument-id-from-melody-event prior-melody-event)
                  (get-sc-instrument-id-from-melody-event cur-melody-event)
                  )
                 )
             )
    (stop-instrument (get-sc-instrument-id-from-melody-event prior-melody-event))
    )
  )

(defn play-note-prior-instrument
  [prior-melody-event melody-event]
  (let [inst-id (get-sc-instrument-id-from-melody-event prior-melody-event)]
    ;; apply not tested???
    (apply ctl inst-id
         :freq (get-freq-from-melody-event melody-event)
         :vol (* (get-volume-from-melody-event melody-event)
                 (get-setting :volume-adjust))
         (get-instrument-settings-from-melody-event melody-event)
         )
    inst-id
    )
  )

(defn play-note-new-instrument
  [melody-event]
  (apply (get-instrument-from-instrument-info
    (get-instrument-info-from-melody-event melody-event)
    )
   (get-freq-from-melody-event melody-event)
   (* (get-volume-from-melody-event melody-event) (get-setting :volume-adjust))
   (get-instrument-settings-from-melody-event melody-event))
  )

(declare play-next-note)
(defn sched-next-note
  [melody-event]
  (when (get-dur-info-from-melody-event melody-event)
    (let [next-time (- (+ (get-event-time-from-melody-event melody-event)
                          (get-dur-millis-from-melody-event melody-event)
                          )
                       NEXT-NOTE-PROCESS-MILLIS)]
      (apply-at next-time
                play-next-note
                [(get-player-id-from-melody-event melody-event) next-time]
                )))
  )

(defn play-melody-event
  [prior-melody-event melody-event event-time]
  (let [cur-inst-id
        (cond (nil? (get-freq-from-melody-event melody-event))
              nil
              (not (false? (get-note-off-from-melody-event prior-melody-event)))
              (play-note-new-instrument melody-event)
              :else
              (play-note-prior-instrument prior-melody-event melody-event)
              )
        full-melody-event (set-play-info melody-event
                                         cur-inst-id
                                         event-time
                                         (System/currentTimeMillis)
                                         )
        ]
    ;; schedule note-off for melody-event
    (when (get-note-off-from-melody-event full-melody-event)
      (apply-at (+ event-time
                   (- (get-dur-millis-from-dur-info
                       (get-dur-info-from-melody-event melody-event))
                      (get-release-millis-from-melody-event full-melody-event)
                      ))
                stop-instrument
                [cur-inst-id])
      )
    full-melody-event
    )
  )

(defn play-next-note
  [player-id sched-time]
  (println "-")
  (println "start -" player-id)
  (let [event-time (+ sched-time NEXT-NOTE-PROCESS-MILLIS)
        [ensemble player-msgs] (get-ensemble-clear-msg-for-player-id player-id)
        player (get-player ensemble player-id)
        melody (get-melody ensemble player-id)
        [upd-player next-melody-event] (get-next-melody-event
                                        ensemble
                                        player
                                        melody
                                        player-id)
        upd-melody-event (play-melody-event (last melody)
                                            next-melody-event
                                            event-time)
        upd-melody (update-melody-with-event melody upd-melody-event)
        ]
    (check-prior-event-note-off (last melody) upd-melody-event)
    (update-player-and-melody upd-player upd-melody player-id)
    (sched-next-note upd-melody-event)
    (>!! (get-msg-channel) {:msg :melody-event
                             :data upd-melody-event
                             :time (System/currentTimeMillis)})
    (println "end:   " player-id " time: " (- (System/currentTimeMillis) event-time) "melody-event: " (:melody-event-id upd-melody-event))
    )
 )
