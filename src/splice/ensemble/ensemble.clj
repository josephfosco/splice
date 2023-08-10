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

(ns splice.ensemble.ensemble
  (:require
   [sc-osc.sc :refer [sc-oneshot-sync-event sc-uuid]]
   [splice.melody.melody-event :refer [print-melody-event set-melody-event-note-off]]
   [splice.player.player-utils :refer [print-player]]
   [splice.util.log :as log]
   )
  )

(def ^:private ensemble (ref nil))
;; TODO document what player-msgs is doing.
;; I am not sure anything is ever being put in here
(def ^:private player-msgs (ref nil))

(defn get-ensemble
  []
  @ensemble)

(defn get-melodies-from-ensemble
  [ens]
  (:melodies ens))

(defn get-melody-for-player-id-from-ensemble
  [ensemble player-id]
  ((:melodies ensemble) player-id)
  )

(defn get-melody-for-player-id
  [player-id]
  (get-melody-for-player-id-from-ensemble (get-ensemble) player-id)
  )

(defn get-player
  [ensemble player-id]
  ((:players ensemble) player-id)
  )

(defn add-msgs-for-new-player
  [player-msgs]
  (conj player-msgs [])
  )

(defn- player-and-melody-update
  [ens player melody player-id]
  ;; adds new player and melody it player-id is = number of players otherwise
  ;; it replaces the player and melody at the index of player-id

  ;; This function can only be called if a transaction has already been
  ;; established using dosync.  A transaction could be added here to make certain,
  ;; but at this point it is not necessary

  ;; TODO might want to make this multi variant and not pass in player-id when adding a player
  (if (= player-id (count (:players ens)))
    (do
      (alter player-msgs add-msgs-for-new-player)
      (assoc ens
             :players (conj (:players ens) player)
             :melodies (conj (:melodies ens) melody)
             )
      )
    (assoc ens
           :players (assoc (:players ens) player-id player)
           :melodies (assoc (:melodies ens) player-id melody)
           ))
  )

(defn update-player-and-melody
  [player melody player-id]
  (dosync
   (alter ensemble player-and-melody-update player melody player-id))
  )

(defn replace-melody-event-note-off
  [ens player-id melody-event melody-event-id melody-event-ndx note-off-val]
  (let [
        melodies (get-melodies-from-ensemble ens)
        upd-melody-event (set-melody-event-note-off melody-event note-off-val)
        upd-player-melody (assoc (nth melodies player-id)
                                 melody-event-ndx
                                 upd-melody-event)
        upd-melodies (assoc melodies player-id upd-player-melody)
        ]
    (assoc ens :melodies upd-melodies)
    ))

(defn update-melody-note-off-for-player-id
  [player-id melody-event-id note-off-val]
  (let [melody (get-melody-for-player-id player-id)
        ; get the index of and melody-event-id of the event to be to replaced
        ndx-and-id (->> melody
                        (map :melody-event-id)
                        (map-indexed vector)
                        (filter #(= (second %) melody-event-id))
                        (map first))
        ]
    (if (= [] ndx-and-id)
      ;; This will occur if for some reason the synth has started before the event
      ;; has been added to the players melody. In that case wait a bit for the melody
      ;; to be updated.
      (do
        (Thread/sleep 100)
        (log/warn "%%%%%%%% ABOUT TO RECUR update-melody-note-off-for-player-id player-id: " player-id " %%%%%%%%")
        (recur player-id melody-event-id note-off-val))
      (do
        (let [melody-event-ndx (first ndx-and-id)
              melody-event (nth melody melody-event-ndx)
              ]
          (dosync
           (alter ensemble replace-melody-event-note-off player-id
                                                         melody-event
                                                         melody-event-id
                                                         melody-event-ndx
                                                         note-off-val))
        )))
  ))

(defn reset-msgs-for-player-id
  [msgs player-id]
  ;; sets the msgs for player-id to [] only if player-msgs hasn't changed and returns the reset
  ;;  value of player-msgs
  ;; if it has changed, player-msgs is not modified and this fn returns false
  (dosync
   (if (= msgs @player-msgs)
     (do
       (ref-set player-msgs (assoc msgs player-id []))
       @player-msgs
       )
     false
     ))
  )

(defn try-to-clear-msgs-for-player-id
  [player-id]
  (let [msgs @player-msgs]
    (if (reset-msgs-for-player-id msgs player-id)
      msgs
      (do
        (log/warn "*** Couldn't clear msgs for player " player-id " - Retrying .... ***")
        nil
        )
      ))
  )

(defn get-ensemble-clear-msg-for-player-id
  [player-id]
  (let [cur-msgs
        (first
         (remove nil?
                 (repeatedly (fn [] (try-to-clear-msgs-for-player-id player-id)))))]
    [@ensemble (get cur-msgs player-id)]
    )
  )

(defn clear-ensemble
  [event]
  (log/info "clearing ensemble....")
  (dosync
   (ref-set ensemble nil))
  )

(defn init-ensemble
  [init-players init-melodies init-msgs]
  (sc-oneshot-sync-event :reset clear-ensemble (sc-uuid))
  (dosync
   (ref-set
    ensemble
    {:players
     (into [] init-players)
     :melodies
     (into [] init-melodies)
     }
    )
   (ref-set player-msgs (into [] init-msgs))
   )
  @ensemble
  )

(defn print-ensemble
  ([] (print-ensemble @ensemble))
  ([ensemble]
   (doseq [player (:players ensemble)]
     (print-player player)
     )
   )
  )

(defn print-player-id
 [player-id]
 (print-player (get-player @ensemble player-id))
 )

(defn print-melody-for-player-id
  [player-id]
  (let [plyr-melody ((:melodies @ensemble) player-id)]
    (doseq [melody-event plyr-melody]
      (print-melody-event melody-event))
    )
  )
