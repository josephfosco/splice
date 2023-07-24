{
 ;; :min-start-offset 1
 ;; :max-start-offset 24

 ;; :main-bus-effects
 ;; {:reverb (:wet-dry 0.9 :room-size 1.0)}

 :loops
 [

  {:name "strings-1"
   :loop-type :multiplying-loop
   :max-num-mult-loops 4
   :instrument-name :string-sect
   :melody-info
   [{:pitch {:type :fixed
             :pitch-midi-note 69}
     :dur {:type :variable-millis
           :min-millis 2000
           :max-millis 3000
           }
     :volume {:type :fixed
              :level 0.2}
     :instrument-settings ("attack" 0.5 "release" 0.5)
     }
    {:pitch {:type :rest}
     :dur {:type :fixed
           :dur-millis 4000}
     }
    ]
   }

  ]
 }
