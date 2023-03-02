;    Copyright (C) 2023  Joseph Fosco. All Rights Reserved
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

(ns splice.sc.groups
  (:require
   [sc-osc.sc :refer [sc-next-id sc-send-msg sc-with-server-sync]]
   ))

;; The root group is allocated by supercollider
(defonce ^:private _root-group_ (sc-next-id :node))
(defonce ^:private head 0)
(defonce ^:private tail 1)
(defonce ^:private before 2)
(defonce ^:private after 3)
(defonce ^:private replce 4)

(def empty-initial-group-ids {:splice-group-id        nil
                              :instrument-group-i     nil
                              :effect-group-id        nil
                           })

(defonce initial-group-ids* (atom empty-initial-group-ids))

(defn setup-initial-groups
  []
  (let [splice-group-id (sc-next-id :node)
        instrument-group-id (sc-next-id :node)
        effect-group-id (sc-next-id :node)
        ]
    (sc-with-server-sync #(sc-send-msg "/g_new" splice-group-id head _root-group_)
      "whilst creating the main Splice group")
    (sc-with-server-sync #(sc-send-msg "/g_new" instrument-group-id head splice-group-id)
      "whilst creating the Splice instrument group")
    (sc-with-server-sync #(sc-send-msg "/g_new" effect-group-id after instrument-group-id)
      "whilst creating the Splice effect group")
     (swap! initial-group-ids* assoc
           :splice-group-id         splice-group-id
           :instrument-group-id     instrument-group-id
           :effect-group-id         effect-group-id
           )
     )
  ;; (satisfy-deps :initial-groups-created)
  )
