;    Copyright (C) 2017-2018, 2023  Joseph Fosco. All Rights Reserved
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

(ns splice.util.settings
  (:require
   [clojure.java.io :as io]
   [splice.util.log :as log]
   )
  )

(defn load-settings
  "Reads a settings file and returns the contents. For splice settings,
   this file should be a map"
  [filename]
  (try
    (with-open [r (io/reader filename)]
      (binding [*read-eval* false]
        (read (java.io.PushbackReader. r))))
    (catch java.io.FileNotFoundException e
      (log/warn (str "File: " filename " is missing...."))
      {})
    )
  )

(declare settings)
(defn get-setting
  [key]
  (key @settings)
  )

(defn set-setting!
  [key val]
  ;; TODO look into this. It is not entirely thread safe. If val is computed based on the
  ;;      current or prior value of key, it is possible that another thread ahs changed
  ;;      the value of the key before this changes it. In ths case it is possible that that
  ;;      val may no longer be correct.
  (swap! settings assoc key val)
  val
  )

(defn update-settings!
  [fn]
  (swap! settings fn)
  (log/data "settings.clj update-settings! settings: " @settings)
  )

(def settings (atom (load-settings "src/splice/config/init_settings.clj")))
