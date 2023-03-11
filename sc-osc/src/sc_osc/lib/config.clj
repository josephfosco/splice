(ns sc-osc.lib.config)

(def CONFIG {:log-level               :debug
             :sc-default-input-buses  6
             :sc-default-output-buses 6
             :sc-max-audio-buses      1024
             :sc-max-control-buses    4096
             }
  )

(def LOG-FILE  "./osc.log")

(defn get-config
  [key]
  (get CONFIG key))
