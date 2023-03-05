;    Copyright (C) 2023 Joseph Fosco. All Rights Reserved
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

(ns sc-osc.sc
  (:require [overtone.osc :refer [in-osc-bundle]]
            [sc-osc.lib.connection :refer [connect connection-status*]]
            [sc-osc.lib.counters :refer [next-id]]
            [sc-osc.lib.event :refer [on-sync-event]]
            [sc-osc.lib.lib :refer [deref! uuid]]
            [sc-osc.lib.server-comms :refer [server-osc-peer* server-snd with-server-sync]]
            ))

(defn sc-connect
  []
  (connect))

(defn sc-send-msg
  [& args]
  (apply server-snd args))

(defn sc-send-bundle
  [time & msgs]
  (in-osc-bundle @server-osc-peer* time msgs))

(defn sc-now
  "Return the current time in milliseconds"
  []
  (System/currentTimeMillis))

(defn sc-connection-status
  []
  @connection-status*)

(defn sc-with-server-sync
  [& args]
  (apply with-server-sync args))

(defn sc-on-sync-event
  [& args]
  (apply on-sync-event args))

(defn sc-deref!
  [& args]
  (apply deref! args)
  )

(defn sc-next-id
  [& args]
  (apply next-id args))

(defn sc-uuid
  []
  (uuid))

(defn sc-debug
  [val]
  (if val
    (do
      (println "Activating detailed OSC debug logs....")
      (sc-osc.lib.event/event-debug-on)
      (overtone.osc/osc-debug true)
      (sc-send-msg "/dumpOSC" 1))
    (do
      (sc-osc.lib.event/event-debug-off)
      (overtone.osc/osc-debug false)
      (sc-send-msg "/dumpOSC" 0))
    ))
