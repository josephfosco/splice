;    Copyright (C) 2017  Joseph Fosco. All Rights Reserved
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

(ns transit.player.structures.base-structure)

(def MAX_STRUCTURE_STRENGTH 100.0)

(defrecord BaseStructure [structr-id
                          internal-strength
                          external-strength
                          orig-internal-strength
                          strength-fn
                          melody-fn
                          cleanup-fn
                          post-play-fn
                          created-at
                          updated-at
                          last-played
                          remove-structr])

(defn get-base-strength
  [structr]
  (let [base-structr (:base structr)]
    (min 100 (+ (:internal-strength base-structr)
                (:external-strength base-structr)))
    )
  )

(defn get-cleanup-fn
  [structr]
  (:cleanup-fn (:base structr))
  )

(defn get-strength-fn
  [structr]
  (:strength-fn (:base structr))
  )

(defn get-structr-id
  [structr]
  (:structr-id (:base structr))
  )

(defn get-melody-fn
  [structr]
  (:melody-fn (:base structr))
  )

(defn get-post-play-fn
  [structr]
  (:post-play-fn (:base structr))
  )

(defn get-internal-strength
  [structr]
  (:internal-strength (:base structr))
  )

(defn structure-updated
  [structr]
  (assoc structr :base (assoc (:base structr) :updated-at (System/currentTimeMillis)))
  )

(defn set-internal-strength
  [structr new-int-strength]
  (structure-updated (assoc structr :base (assoc (:base structr)
                                             :internal-strength
                                             (min 100 new-int-strength))))
  )

(defn set-cleanup-fn
  [structr cleanup-fn]
  (structure-updated (assoc structr :base (assoc (:base structr)
                                             :cleanup-fn
                                             cleanup-fn)))
  )

(defn reset-internal-strength-to-orig
  [structr]
  (let [base-structr (:base structr)]
    (structure-updated (assoc structr
                           :base
                           (assoc base-structr
                                  :internal-strength
                                  (:orig-internal-strength base-structr)))
                    )
    )
  )

(defn set-remove-structr
  [structr new-remove-structr]
  (assoc structr :base (assoc (:base structr)
                              :remove-structr new-remove-structr))
  )

(defn set-structr-id
  [structr new-structr-id]
  (assoc structr :base (assoc (:base structr) :structr-id new-structr-id))
  )

(defn remove-structr?
  [structr]
  (:remove-structr (:base structr))
  )

(defn create-base-structure
  [& {:keys [structr-id
             internal-strength
             external-strength
             strength-fn
             melody-fn
             post-play-fn
             cleanup-fn] :or
      {structr-id "none"
       internal-strength 0
       external-strength 0
       strength-fn get-base-strength
       cleanup-fn nil
       post-play-fn nil
       }}]
  (BaseStructure. structr-id
                  internal-strength
                  external-strength
                  internal-strength  ;; orig-internal-strength
                  strength-fn
                  melody-fn
                  cleanup-fn
                  post-play-fn
                  (System/currentTimeMillis)
                  (System/currentTimeMillis)
                  nil  ;; last-played
                  false  ;; remove-structr
                  )
  )
