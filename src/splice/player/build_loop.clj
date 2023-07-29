(ns splice.player.build-loop
  (:require
   [splice.player.loops.loop :refer [create-loop]]
   [splice.player.loops.multiplying-loop :refer [create-multiplying-loop]]
   )

  )

(defn build-loop-structr
  [loop-settings]
  (condp = (:loop-type loop-settings)
    :loop (create-loop :name (:name loop-settings)
                       :melody-info (:melody-info loop-settings)
                       :next-melody-event-ndx 0
                       )

    :multiplying-loop (create-multiplying-loop
                       :name (:name loop-settings)
                       :melody-info (:melody-info loop-settings)
                       :next-melody-event-ndx 0
                       :max-num-mult-loops (:max-num-mult-loops loop-settings)
                       )

    nil (throw (Throwable. (str ":loop-type missing")))
    (do
      (throw (Throwable. (str "Invalid :loop-type "
                              (:loop-type loop-settings))))
      )
    )
  )
