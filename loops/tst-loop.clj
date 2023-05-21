{:min-start-offset 0
 :max-start-offset 0

 :loops
 [
  {:name "strings-1"
   :loop-type :loop
   :instrument-name :string-sect
   :melody-info
   [{:pitch {:type :fixed
             :pitch-midi-note 69}
     :dur {:type :fixed
           :dur-millis 1000
           }
     :volume {:type :fixed
              :level 0.7}
     :instrument-settings ("attack" 0.10 "release" 3.0)
     }
    {:pitch {:type :fixed
             :pitch-midi-note 76}
     :dur {:type :fixed
           :dur-millis 1000
           }
     :volume {:type :fixed
              :level 0.2}
     :instrument-settings ("attack" 0.10 "release" 3.0)
     }
    {:pitch {:type :rest}
     :dur {:type :fixed
           :dur-millis 5000}
     }
    ]
   }

  ]}
