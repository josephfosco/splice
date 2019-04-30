{:min-start-offset 1
 :max-start-offset 24

 :main-bus-effects
 {:reverb (:wet-dry 0.5 :room-size 1.0)}

 :loops
 [
  {:name "plink-loop"
   :loop-type :loop
   :instrument-name :plink-m1
   :melody-info
   [
    {:pitch {:type :fixed
             :pitch-freq nil}
     :dur {:type :fixed
           :dur-millis 20000}
     }
    {:pitch {:type :variable
             :pitch-type :midi-note
             :pitches [105 109 110 112 nil]}
     :dur {:type :fixed
           :dur-millis 200}
     :volume 0.5}
    {:pitch {:type :variable
             :pitch-type :midi-note
             :pitches [105 109 110 112 nil]}
     :dur {:type :fixed
           :dur-millis 200}
     :volume 0.5
     }
    {:pitch {:type :variable
             :pitch-type :midi-note
             :pitches [105 109 110 112 nil]}
     :dur {:type :variable-inc-millis
           :dur-millis 200
           :inc-millis 50
           :dec-millis 50}
     :volume 0.5
     }
    {:pitch {:type :variable
             :pitch-type :midi-note
             :pitches [105 109 110 112 nil]}
     :dur {:type :fixed
           :dur-millis 200}
     :volume 0.5
     }
    {:pitch {:type :fixed
             :pitch-freq nil}
     :dur {:type :fixed
           :dur-millis 23000}
     }
    ]
   }

  {:name "gong-loop"
   :loop-type :loop
   :instrument-name :gong
   :melody-info
   [
    {:pitch {:type :fixed
             :pitch-freq nil}
     :dur {:type :fixed
           :dur-millis 28000}
     }
    {:pitch {:type :fixed
             :pitch-midi-note 38}
     :dur {:type :fixed
           :dur-millis 10000}
     :volume 0.45
     }
    {:pitch {:type :fixed
             :pitch-freq nil}
     :dur {:type :fixed
           :dur-millis 43000}
     }
    ]
   }

  {:name "woosh-loop"
   :loop-type :loop
   :instrument-name :woosh
   :melody-info
   [
    {:pitch {:type :fixed
             :pitch-freq nil}
     :dur {:type :fixed
           :dur-millis 40000}
     }
    {:pitch {:type :fixed
             :pitch-midi-note 38}
     :dur {:type :fixed
           :dur-millis 10000}
     :volume 0.5
     }
    {:pitch {:type :fixed
             :pitch-freq nil}
     :dur {:type :fixed
           :dur-millis 38000}
     }
    ]
   }

  {:name "strings-1"
   :loop-type :loop
   :instrument-name :string-sect
   :melody-info
   [{:pitch {:type :fixed
             :pitch-midi-note 69
             }
             ;; :pitch-freq 440
     :dur {:type :variable-millis
           :min-millis 7000
           :max-millis 13500
           }
     :volume 0.2
     :instrument-settings (:attack 4.0 :release 3.0)
     }
    {:pitch {:type :fixed
             :pitch-freq nil}
     :dur {:type :fixed
           :dur-millis 14000}
     }
    ]
   }

  {:name "strings-2"
   :loop-type :loop
   :instrument-name :string-sect
   :melody-info
   [{:pitch {:type :fixed
             :pitch-midi-note 74
             }
             ;; :pitch-freq
     :dur {:type :fixed
           :dur-millis 8500}
     :volume 0.2
     :instrument-settings (:attack 2.5 :release 3.0)
     }
    {:pitch {:type :fixed
             :pitch-freq nil}
     :dur {:type :fixed
           :dur-millis 22000}
     }
    ]
   }

  {:name "strings-3"
   :loop-type :loop
   :instrument-name :string-sect
   :melody-info
   [{:pitch {:type :fixed
             :pitch-midi-note 76
             }
             ;; :pitch-freq 733.333348
     :dur {:type :fixed
           :dur-millis 1200}
     :volume 0.2
     :instrument-settings (:attack 5.5 :release 4.7)
     }
    {:pitch {:type :fixed
             :pitch-freq nil}
     :dur {:type :fixed
           :dur-millis 28000}
     }
    ]
   }

  {:name "strings-4"
   :loop-type :loop
   :instrument-name :string-sect
   :melody-info
   [{:pitch {:type :fixed
             :pitch-midi-note 81
             }
             ;; :pitch-freq 880
     :dur {:type :fixed
           :dur-millis 9000}
     :volume 0.2
     :instrument-settings (:attack 7.0 :release 4.0)
     }
    {:pitch {:type :fixed
             :pitch-freq nil}
     :dur {:type :fixed
           :dur-millis 26000}
     }
    ]
   }

  {:name "marimaba-loop"
   :loop-type :loop
   :instrument-name :marimba
   :melody-info
   [
    {:name e1
     :pitch {:type :fixed
             :pitch-freq nil}
     :dur {:type :fixed
           :dur-millis 35000}
     }
    {:name e2
     :play-prob 70
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [81 83 86 88 91 93]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e3
     :play-prob 60
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [81 83 86 88 91 93]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e4
     :play-prob 80
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [83 86 88 91 93 nil]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e5
     :play-prob 80
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [81 83 86 88 91 93 nil]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e6
     :play-prob 80
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [81 86 88 91 93 nil]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e7
     :play-prob 80
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [81 83 86 88 91 93 nil]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e8
     :play-prob 80
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [81 83 88 91 93 nil]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e9
     :play-prob 20
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [91 93]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e10
     :play-prob 40
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [91 93]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e11
     :play-prob 80
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [81 83 86 88 93 nil]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e12
     :play-prob 80
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [81 83 86 88 91 93 nil]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e313
     :play-prob 80
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [81 83 86 88 91 nil]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e14
     :play-prob 80
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [81 83 86 88 91 93 nil]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e15
     :play-prob 80
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [81 83 86 88 91 93 nil]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e16
     :play-prob 80
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [81 83 86 88 91 93]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e17
     :play-prob 80
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [81 83 86 91 93 nil]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e18
     :play-prob 80
     :pitch {:type :variable
             :pitch-type :midi-note
             :pitches [81 83 86 88 91 93 nil]}
     :dur {:type :fixed
           :dur-millis 175}
     :volume 0.18}
    {:name e19
     :pitch {:type :fixed
             :pitch-freq nil}
     :dur {:type :fixed
           :dur-millis 20000}
     }
    ]
   }

  ]}
