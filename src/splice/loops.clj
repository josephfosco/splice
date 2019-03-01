{:min-start-offset 1
 :max-start-offset 24

 :main-bus-effects
 {:reverb (:wet-dry 0.9 :room-size 1.0)}

 :loops
 [
  {:name "plink-loop"
   :loop-type :loop
   :instrument-name :plink-m1
   :melody-info
   [
    {:pitch-freq nil
     :dur {:type :fixed
           :dur-millis 20000}
     }
    {:pitch-midi-note 105
     :dur {:type :fixed
           :dur-millis 200}
     :volume 0.5
     }
    {:pitch-midi-note 112
     :dur {:type :fixed
           :dur-millis 200}
     :volume 0.5
     }
    {:pitch-midi-note 110
     :dur {:type :fixed
           :dur-millis 200}
     :volume 0.5
     }
    {:pitch-midi-note 109
     :dur {:type :fixed
           :dur-millis 200}
     :volume 0.5
     }
    {:pitch-freq nil
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
    {:pitch-freq nil
     :dur {:type :fixed
           :dur-millis 28000}
     }
    {:pitch-midi-note 38
     :dur {:type :fixed
           :dur-millis 10000}
     :volume 0.45
     }
    {:pitch-freq nil
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
    {:pitch-freq nil
     :dur {:type :fixed
           :dur-millis 40000}
     }
    {:pitch-midi-note 38
     :dur {:type :fixed
           :dur-millis 10000}
     :volume 0.5
     }
    {:pitch-freq nil
     :dur {:type :fixed
           :dur-millis 38000}
     }
    ]
   }

  {:name "loop-1"
   :loop-type :loop
   :instrument-name :string-sect
   :melody-info
   [{:pitch-midi-note 69
     :dur {:type :fixed
           :dur-millis 11000}
     :volume 0.2
     :instrument-settings (:attack 4.0 :release 3.0)
     }
    {:pitch-freq nil
     :dur {:type :fixed
           :dur-millis 14000}
     }
    ]
   }

  {:name "loop-2"
   :loop-type :loop
   :instrument-name :string-sect
   :melody-info
   [{:pitch-midi-note 74
     :dur {:type :fixed
           :dur-millis 8500}
     :volume 0.2
     :instrument-settings (:attack 2.5 :release 3.0)
     }
    {:pitch-freq nil
     :dur {:type :fixed
           :dur-millis 22000}
     }
    ]
   }

  {:name "loop-3"
   :loop-type :loop
   :instrument-name :string-sect
   :melody-info
   [{:pitch-midi-note 76
     :dur {:type :fixed
           :dur-millis 1200}
     :volume 0.2
     :instrument-settings (:attack 5.5 :release 4.7)
     }
    {:pitch-freq nil
     :dur {:type :fixed
           :dur-millis 28000}
     }
    ]
   }

  {:name "loop-4"
   :loop-type :loop
   :instrument-name :string-sect
   :melody-info
   [{:pitch-midi-note 81
     :dur {:type :fixed
           :dur-millis 9000}
     :volume 0.2
     :instrument-settings (:attack 7.0 :release 4.0)
     }
    {:pitch-freq nil
     :dur {:type :fixed
           :dur-millis 26000}
     }
    ]
   }

  ]
 }
