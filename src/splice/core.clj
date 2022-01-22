;    Copyright (C) 2019 Joseph Fosco. All Rights Reserved
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

(ns splice.core
  (:gen-class)
  (:require
   [overtone.live :as overtone]
   [splice.control :refer [clear-splice pause-splice quit-splice
                           start-splice ]]
   [splice.util.log :as log]
   [splice.util.settings :refer [get-setting load-settings set-setting!]]
   [splice.version :refer [SPLICE-VERSION-STR]]
   ))

 (defn -main
   [& args]
   (println "command line args:" args)
  )

(defn get-settings
  [filename]
  (let [settings (load-settings filename)]
    (doseq [[k v] (seq settings)]
      (set-setting! k v)
      )
    )
  )

(defn splice-start
  "Start playing.
  "
  [& {:keys [] :as args} ]
  (get-settings "src/splice/settings.clj")
  (log/set-print-log-level! true)
  (log/set-log-level! (get-setting :log-level))
  (start-splice args)
)

(defn splice-quit
  "Quit splice and exit Clojure"
  []
  (quit-splice)
  )

(defn splice-exit
  "same as splice-quit"
  []
  (quit-splice))

(defn splice-stop
  "same as splice-quit"
  []
  (quit-splice))

(defn splice-pause
  "Stop playing after players finish what they have scheduled"
  []
  (pause-splice)
)

(defn splice-clear
  "Clears the scheduler, message-processor, and players"
  []
  (clear-splice)
  )

(defn splice-help
  []
  (println)
  (println "SPLICE version" SPLICE-VERSION-STR)
  (print
   "
   Functions to run splice

     (splice-start)        Start playing
                             optional key :num-players
     (splice-pause)        Pause after playing current notes

     (splice-help)         Print this message


   ")
  (println "")
  )

(defn stop
  []
  (overtone/stop))

(splice-help)

(+ 1 2)
