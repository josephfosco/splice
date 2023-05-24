{:min-start-offset 0
 :max-start-offset 0

 :loops
 [
  {:name "plink-loop"
   :loop-type :loop
   :instrument-name :plink-m1
   :melody-info
   [
    {:pitch {:type :rest}
     :dur {:type :fixed
           :dur-millis 1000}
     }
    {:pitch {:type :variable
             :pitch-type :midi-note
             :pitches [105 105 105 105 109 110 112 nil]}
     :dur {:type :fixed
           :dur-millis 200}
     :volume {:type :variable
              :min-volume 0.3
              :max-volume 0.5}
     }
    {:pitch {:type :variable
             :pitch-type :midi-note
             :pitches [105 109 110 110 110 110 112 nil]}
     :dur {:type :fixed
           :dur-millis 200}
     :volume {:type :variable
              :min-volume 0.3
              :max-volume 0.5}
     }
    {:pitch {:type :variable
             :pitch-type :midi-note
             :pitches [105 109 110 112 112 112 112 nil]}
     :dur {:type :variable-inc-millis
           :dur-millis 200
           :inc-millis 50
           :dec-millis 50}
     :volume {:type :variable
              :min-volume 0.3
              :max-volume 0.5}
     }
    {:pitch {:type :variable
             :pitch-type :midi-note
             :pitches [105 109 109 109 109 110 112 nil]}
     :dur {:type :fixed
           :dur-millis 200}
     :volume {:type :variable
              :min-volume 0.3
              :max-volume 0.5}
     }
    {:pitch {:type :rest}
     :dur {:type :fixed
           :dur-millis 2000}
     }
    ]
   }
  ]


 ;; :loops
 ;; [
 ;;  {:name "strings-1"
 ;;   :loop-type :loop
 ;;   :instrument-name :string-sect
 ;;   :melody-info
 ;;   [{:pitch {:type :fixed
 ;;             :pitch-midi-note 69}
 ;;     :dur {:type :fixed
 ;;           :dur-millis 1000
 ;;           }
 ;;     :volume {:type :fixed
 ;;              :level 0.7}
 ;;     :instrument-settings ("attack" 0.10 "release" 3.0)
 ;;     }
 ;;    {:pitch {:type :fixed
 ;;             :pitch-midi-note 76}
 ;;     :dur {:type :fixed
 ;;           :dur-millis 1000
 ;;           }
 ;;     :volume {:type :fixed
 ;;              :level 0.2}
 ;;     :instrument-settings ("attack" 0.10 "release" 3.0)
 ;;     }
 ;;    {:pitch {:type :fixed
 ;;             :pitch-midi-note 69}
 ;;     :dur {:type :fixed
 ;;           :dur-millis 5000
 ;;           }
 ;;     :volume {:type :fixed
 ;;              :level 0.7}
 ;;     :instrument-settings ("attack" 0.10 "release" 0.3)
 ;;     }
 ;;    {:pitch {:type :rest}
 ;;     :dur {:type :fixed
 ;;           :dur-millis 5000}
 ;;     }
 ;;    ]
 ;;   }

 ;;  ]
  }
