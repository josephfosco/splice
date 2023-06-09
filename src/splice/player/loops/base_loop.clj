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

(ns splice.player.loops.base-loop
  (:require
   [splice.melody.dur-info :refer [create-dur-info]]
   [splice.melody.volume :refer [select-random-volume]]
   [splice.melody.rhythm :refer [select-random-dur-info]]
   [splice.music.music :refer [midi->hz]]
   )
  )

(defrecord BaseLoop [name next-melody-fn])

(defn create-base-loop
  [& {:keys [name next-melody-fn]}]
  (BaseLoop. name next-melody-fn
             )
  )

(defn get-base-loop-name
  [loop-structr]
  (:name (:base-loop loop-structr))
  )

(defn get-melody-fn
  [loop-structr]
  (:next-melody-fn (:base-loop loop-structr))
 )

(defn get-loop-dur-info
  [dur-info]
  (condp = (:type dur-info)
    :fixed (create-dur-info
            :dur-millis (:dur-millis dur-info)
            :dur-beats (:dur-beats dur-info)
            )
    :variable-millis (select-random-dur-info
                      (:min-millis dur-info)
                      (:max-millis dur-info)
                      )
    :variable-inc-millis (let [base-dur (:dur-millis dur-info)]
                           (select-random-dur-info
                            (- base-dur (:dec-millis dur-info))
                            (+ base-dur (:inc-millis dur-info))
                            ))
    :variable-pct nil
    )
  )

(defn get-loop-pitch
  [pitch-info]
  ;; Throw an error if the pitch-freq or pitch-midi-note is nil or missing when pitch :type
  ;; is not :rest
  ;; TODO This validation should occur when the loop file is loaded possibly in control/validate-player-settings
  (if (and (not= (:type pitch-info) :rest)
           (nil? (:pitch-freq pitch-info))
           (nil? (:pitch-midi-note pitch-info))
           (nil? (:pitches pitch-info)))
    (throw (Throwable. (str "Missing :pitch-freq, :pitch-midi-note or :pitches when "
                            ":pitch-type is :fixed or :variable")))

    )
  (condp = (:type pitch-info)
    :fixed (or (:pitch-freq pitch-info)
               (if-let [note-no (:pitch-midi-note pitch-info)]
                 (midi->hz note-no)
                 )
               )
    :variable (let [pitch (rand-nth (:pitches pitch-info))]
                (if (and pitch (= :midi-note (:pitch-type pitch-info)))
                  (midi->hz pitch)
                  pitch) ;; :pitch-type = :freq or pitch is nil (rest)
                )
    :rest nil
    )
  )

(defn get-loop-volume
  [volume-info]
  (condp = (:type volume-info)
    :fixed (:level volume-info)
    :random (select-random-volume)
    :variable (select-random-volume
                      (:min-volume volume-info)
                      (:max-volume volume-info)
                      )
    nil ;; default - used when this is a rest and volume-info is nil
    )
  )
