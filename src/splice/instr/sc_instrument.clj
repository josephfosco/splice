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
   [sc-osc.sc :refer [sc-deref! sc-on-sync-event sc-send-msg sc-uuid]]
   ))

(defn stop-instrument
  [sc-instrument-id]
  (sc-send-msg "/n_set" sc-instrument-id "gate" 0.0)
  )

(defn get-release-millis-from-instrument
  ""
  ;; (*  (node-get-control sc-instrument-id :release) 1000)
  ([sc-instrument-id] (get-release-millis-from-instrument sc-instrument-id nil))
  ([sc-instrument-id matcher-fn]
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
         cvals (do (sc-send-msg "/s_get" sc-instrument-id "release")
                   (:args (sc-deref! res
                                     (str "attempting to get control value release for sc-instrument-id "
                                          (with-out-str (pr sc-instrument-id))))))]
     ;; cvals should be a list something like this
     ;; (7 release 3.0) sc-instrument-id contro-param ("release") param-value
     (* (last cvals) 1000)
     )))
