;    Copyright (C) 2014-2019, 2023  Joseph Fosco. All Rights Reserved
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

(ns splice.instr.sc-instrument
  (:require
   [sc-osc.sc :refer [sc-deref! sc-now sc-on-sync-event sc-send-bundle sc-send-msg sc-uuid]]
   ))

(defn sched-gate-off
  ([sc-synth-id] (sched-gate-off sc-synth-id (sc-now)))
  ([sc-synth-id time]
   (sc-send-bundle time
                   (sc-send-msg "/n_set" sc-synth-id "gate" 0.0)))
  )

(defn sched-control-val
  ([sc-synth-id time & ctl-vals]
   (sc-send-bundle time
                   (apply sc-send-msg "/n_set" sc-synth-id ctl-vals)))
  )

(defn get-release-millis-from-instrument
  ""
  ;; (*  (node-get-control sc-synth-id :release) 1000)
  ([sc-synth-id] (get-release-millis-from-instrument sc-synth-id nil))
  ([sc-synth-id matcher-fn]
   (let [p     (promise)
         key   (sc-uuid)
         res   (do (sc-on-sync-event "/n_set"
                                     (fn [info]
                                       (when (or (nil? matcher-fn)
                                                 (matcher-fn info))
                                         (deliver p info)
                                         :sc-osc/remove-handler))
                                     key)
                   p)
         cvals (do (sc-send-msg "/s_get" sc-synth-id "release")
                   (:args (sc-deref! res
                                     (str "attempting to get control value release for sc-synth-id "
                                          (with-out-str (pr sc-synth-id))))))]
     ;; cvals should be a list something like this
     ;; (7 release 3.0) sc-synth-id contro-param ("release") param-value
     (* (last cvals) 1000)
     )))
