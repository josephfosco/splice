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
   [splice.sc.sc-constants :refer [head after]]
   ))

;; The root group is allocated by supercollider
(defonce ^:private _root-group_ (sc-next-id :node))

(def empty-base-group-ids {:splice-group-id        nil
                           :instrument-group-id    nil
                           :effect-group-id        nil
                           :pre-fx-group-id        nil
                           :main-fx-group-id       nil
                           :post-fx-group-id       nil
                           })

(defonce base-group-ids* (atom empty-base-group-ids))

(defn setup-base-groups
  []
  (let [splice-group-id (sc-next-id :node)
        instrument-group-id (sc-next-id :node)
        effect-group-id (sc-next-id :node)
        pre-fx-group-id (sc-next-id :node)
        main-fx-group-id (sc-next-id :node)
        post-fx-group-id (sc-next-id :node)
        ]
    (sc-with-server-sync #(sc-send-msg "/g_new" splice-group-id head _root-group_)
      "whilst creating the main Splice group")
    (sc-with-server-sync #(sc-send-msg "/g_new" instrument-group-id head splice-group-id)
      "whilst creating the Splice instrument group")
    (sc-with-server-sync #(sc-send-msg "/g_new" effect-group-id after instrument-group-id)
      "whilst creating the Splice effect group")
    (sc-with-server-sync #(sc-send-msg "/g_new" pre-fx-group-id head effect-group-id)
      "whilst creating the pre fx group")
    (sc-with-server-sync #(sc-send-msg "/g_new" main-fx-group-id after pre-fx-group-id)
      "whilst creating the main fx group")
    (sc-with-server-sync #(sc-send-msg "/g_new" post-fx-group-id after main-fx-group-id)
      "whilst creating the main post group")
     (swap! base-group-ids* assoc
           :splice-group-id         splice-group-id
           :instrument-group-id     instrument-group-id
           :effect-group-id         effect-group-id
           :pre-fx-group-id         pre-fx-group-id
           :main-fx-group-id        main-fx-group-id
           :post-fx-group-id        post-fx-group-id
           )
     )
  ;; (satisfy-deps :base-groups-created)
  )
