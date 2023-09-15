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

(ns splice.player.loops.base-loop
  (:require
   [splice.melody.dur-info :refer [create-dur-info get-dur-millis-from-dur-info]]
   [splice.melody.volume :refer [select-random-volume]]
   [splice.melody.rhythm :refer [select-random-dur-info]]
   [splice.music.music :refer [midi->hz]]
   [splice.player.loops.looptype :refer [LoopType
                                         get-name
                                         get-loop-repetition
                                         set-loop-repetition
                                         ]]
   [splice.util.log :as log]
   [splice.util.random :refer [random-probability-result]]
   )
  )

(def ^:private  global-dur-multiplier (atom nil))

(defrecord BaseLoop [name
                     next-melody-fn
                     loop-repetition   ;; number of this repetition (0 - n) nil for copy
                     ]
  LoopType
  (get-name [loop] (:name loop))
  (get-loop-repetition [loop] (:loop-repetition loop))
  (set-loop-repetition [loop loop-rep] (assoc loop :loop-repetition loop-rep))
  )

(defn init-base-loop
  []
  (reset! global-dur-multiplier nil))

(defn create-base-loop
  [& {:keys [name next-melody-fn]}]
  (BaseLoop. name
             next-melody-fn
             0
             )
  )

(defn get-base-loop-name
  [loop-structr]
  (:name (:base-loop loop-structr))
  )

(defn get-loop-param
  [loop-structr param]
  (cond
    (contains? loop-structr param) (param loop-structr)

    ;; If the loop-structr implements the LoopType protocol search for :next-melody-fn else
    ;; recurse through it
    ;; TODO this will not necessarily work if any it the loop-structr contains more
    ;;      than 1 LoopType
    (satisfies? LoopType loop-structr)
    (some #(get-loop-param % param) (filter record? (vals loop-structr)))

    :else nil)
  )

(defn get-melody-fn
  [loop-structr]
  (get-loop-param loop-structr :next-melody-fn)
  )

(defn get-global-dur-multiplier
  []
  @global-dur-multiplier
  )

(defn set-global-dur-multiplier
  [mult]
  (reset! global-dur-multiplier mult)
  )

(defn- add-dur-variation
  [dur-info melody-dur-info]
  (let [var-prob (:dur-var-prob dur-info)]   ;; if var-prob nil defaults to 100%
    (if (or (= nil var-prob) (random-probability-result var-prob))
      (do
        (let [dur-var-pct (- (rand-int (+ (:dur-var-max-pct-inc dur-info)
                                          (:dur-var-max-pct-dec dur-info)))
                             (:dur-var-max-pct-dec dur-info))
              melody-dur-with-var (* (:dur-millis melody-dur-info) (+ 1 (/ dur-var-pct 100)))
              ]
          ;; This creates a new dur-info record that is dur-var-ms different from the
          ;; dur-millis in melody-dur-info
          (create-dur-info :dur-millis melody-dur-with-var
                           :dur-base-millis (:dur-base-millis melody-dur-info))
          )
        )
      melody-dur-info   ;; no dur variation
      ))
  )

(defn- get-base-dur-info
  [dur-info]
  (condp = (:type dur-info)
    :fixed (create-dur-info
            :dur-millis (:dur-millis dur-info)
            :dur-beats (:dur-beats dur-info)
            :dur-base-milis (:dur-millis dur-info)
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

(defn get-loop-dur-info
  [loop-structr dur-info]
  (let [melody-dur-info (get-base-dur-info dur-info)
        melody-dur-with-var (if-let [first-dur-var-rep (:dur-var-first-rep dur-info)]
                              (if (>= (get-loop-repetition loop-structr) first-dur-var-rep)
                                  (add-dur-variation dur-info melody-dur-info)
                                  melody-dur-info
                                  )
                              melody-dur-info
                              )
        ]
    (log/trace "base_loop.clj/get-loop-dur-info - melody-dur-info: " (pr-str melody-dur-info))
    (if-let [mult (get-global-dur-multiplier)]
      (create-dur-info :dur-millis (* (get-dur-millis-from-dur-info melody-dur-with-var) mult))
      melody-dur-with-var)
    )
  )

(defn- add-pitch-variation
  [pitch-info base-frequency]
  (let [var-prob (:pitch-var-prob pitch-info)]   ;; if var-prob nil defaults to 100%
    (if (or (= nil var-prob) (random-probability-result var-prob))
      (let [pitch-var-cents (- (rand-int (+ (:pitch-var-max-inc pitch-info)
                                            (:pitch-var-max-dec pitch-info)))
                               (:pitch-var-max-dec pitch-info))
            ]
        ;; This adds pitch-var-sents to base-frequency
        (* base-frequency (Math/pow 2.0 (/ pitch-var-cents 1200.0)))
        )
      base-frequency   ;; no pitch variation
      ))
  )

(defn- get-base-pitch
  [pitch-info]
  ;; Pitches that are originally specified as :pitch-midi-note are now converted to
  ;; :pitch-freq when the player(s) are created. At this point it should NEVER be
  ;; necessary to convert midi-note-num to freq.

  (condp = (:type pitch-info)
    :fixed (:pitch-freq pitch-info)

    :variable (rand-nth (:pitches pitch-info))

    :rest nil
    )
  )

(defn get-loop-pitch
  [loop-structr pitch-info]
  ;; Throw an error if the pitch-freq or pitch-midi-note is nil or missing when pitch :type
  ;; is not :rest
  ;; TODO This validation should occur when the loop file is loaded possibly in control/validate-player-settings
  (when (= (:type pitch-info) nil)
    (throw (Throwable. (str "Missing :pitch-type")))
    )
  (if (and (not= (:type pitch-info) :rest)
           (nil? (:pitch-freq pitch-info))
           (nil? (:pitch-midi-note pitch-info))
           (nil? (:pitches pitch-info)))
    (throw (Throwable. (str "Missing :pitch-freq, :pitch-midi-note or :pitches when "
                            ":pitch-type is :fixed or :variable")))
    )
  (let [base-pitch (get-base-pitch pitch-info)]
    (if-let [first-pitch-var-rep (:pitch-var-first-rep pitch-info)]
      (if (>= (get-loop-repetition loop-structr) first-pitch-var-rep)
        (add-pitch-variation pitch-info base-pitch)
        base-pitch)
      base-pitch
      )
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
