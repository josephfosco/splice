{
  :min-start-offset-ms - minimum amount of time in milliseconds to wait before starting 
                           a loop (defaults to 0)
  :max-start-offset-ms - maximum amount of time in milliseconds to wait before starting 
                           a loop (defaults to 0)
    The start of each loop is computed independently using these two values

  :main-bus-effects - a map of effects to add to the main stereo output bus
  {:reverb adds reverb uses the params in the map that follows 
    (:wet-dry - wet/dry ratio (defaults to 0.5)
     :room-size - room size (0-1 defaults to 0.5)
     :dampening - high freq dampening (0 - 1 defaults to 0.2)
    )
  }
}
 
 :loops - [array of 1 or more maps of loops to play]
 
:loop-type loop
  {:name - loop name 
  :loop-type Required options for type :loop. Plays :melody-info in a loop 
  :instrument-name an instrument name from src/splice/instr/instrument.clj - all-instruments
  :melody-info - an array of melody elemants to play
  [
    {:pitch - Required - Map containing pitch information for this note
     :dur - Required. Map containing duration info for this event.
     :volume - Required if pitch is not nil. volume of melody element 0 - 1.
     :instrument-settings - Optional - a list of keys and values of settings for this instrument. Example: ("attack" 4.0 "release" 3.0)
     :play-prob - the probability that this event will play
    }
  ]
}

:loop-type multiplying-loop
  This loop will create a new loop (based on the paramaters when created) on every repetition 
    of the loop (when the loop starts from the begining)
    
  everything from :loop-type loop plus
  {:max-num-mult-loops - after creating this many loops, no new loops will be creted - 
                           defaults to 1
   :reps-before-multing - will not start creating new loops until the original loop has played
                            this many times - 
   :num-mult-loops-started - the number of new loops created. This is managed internally
   :loop-mult-probability - percent probability that a new loop will will be created when 
                              possible - defaults to 100
   :original-loop? - indicates that this is the original loop, not a copy - THIS PARAM IS NOT 
                       used yet
   :create-player-fn - the function to create a new player for a new loop that is being
                         created 
   :min-new-mult-loop-delay-ms - the minimum number of millis to wait before creating a new loop. 
   :max-new-mult-loop-delay-ms - the maximum number of millis to wait before creating a new loop. 
   :core-loop - the core loop that this loop is based on, currently is a loop
}

:pitch
[
  {
    :type - :fixed | :variable | :rest
    :pitch-midi-note - This or :pitch-freq are required if :type == :fixed
    :pitch-freq - This or :pitch-midi-note are required if :type == :fixed
    :pitch-type - Required if type == :variable
    :pitches - Required if :type == :variable
    :pitch-var-first-rep - the first repitition that patch variation will possibly take place on
    :pitch-var-prob - Optional, defaults to 100
    :pitch-var-max-inc - The maximum number of cents the pitch can be adjusted up
    :pitch-var-max-dec - The maximum number of cents the pitch can be adjusted down
  }

  :pitch-midi-note - The midi note number of the note to play
  :pitch-freq - The frequency (Hz) of the note to play
  :pitch-type - :midi-note | :freq
  :pitches - An array of integers representing midi-notes if :pitch-type == :midi-note or
             frequencies (Hz.) if :pitch-type ==:freq.
  :pitch-var-first-rep - the first repitition that patch variation will possibly take place on
  :pitch-var-prob - percent that the pitch will be adjusted defaults to 100
  :pitch-var-max-inc - The maximum number of cents the pitch can be adjusted up
  :pitch-var-max-dec - The maximum number of cents the pitch can be adjusted down
]

:dur
[
  {
    :type  - Required :fixed | :variable-millis | :variable-inc-millis
    :dur-millis - Required if :type == :fixed or :variable-inc-millis
    :min-millis - Required if :type == :fixed or :variable-millis
    :max-millis - Required if :type == :fixed or :variable-millis
    :inc-millis - Required if :type == :variable-inc-millis
    :dec-millis - Required if :type == :variable-inc-millis
    :dur-var-first-rep - the first repitition that dur variation will possibly take place on
                         the presence of this variable indicates that dur-var processing will
                         occur
    :dur-var-prob - Optional, percent that the dur will be adjusted. Defaults to 100
    :dur-var-max-pct-inc - Optional, The maximum percent the duration can be increased default 0
    :dur-var-max-pct-dec - Optional, The maximum percent the duration can be decreased default 0
    :dur-var-ignore-for-nxt-note - Optional true | false | nil
    :dur-beats - NOT IMPLEMENTED
  }

  :dur-millis - if type == :fixed, the duration of this loop event
              if type == :variable-inc-millis, the base duration of this event
  :min-millis - if :type == :variable-millis, the minimum duration of this event
  :max-millis - if :type == :variable-millis, the maximum duration of this event
  :inc-millis - if :type == :variable-inc-millis, the maximum amount dur-millis can be incremented
  :dec-millis - if :type == :variable-inc-millis, the maximum amount dur-millis can be decremented
  :dur-var-first-rep - the first repitition that dur variation will possibly take place on
  :dur-var-prob - Optional, percent that the dur will be adjusted. Defaults to 100
  :dur-var-max-pct-inc - Optional, The maximum percent the duration can be increased default 0
  :dur-var-max-pct-dec - Optional, The maximum percent the duration can be decreased default 0
  :dur-var-ignore-for-nxt-note - schedule the not after this as if this note had a duration
                                 that did not include a dur-var (use base-dur only for next note)
  :dur-beats - NOT IMPLEMENTED
]

:volume
[
  {
    :type - Required :fixed | random | variable
    :level - Required if type == :fixed, not used if :type == random or variable
    :min-volume - Required if :type == :variable, not used if :type == :fixed or :random
    :max-volume - Required if :type == :variable, not used if :type == :fixed or :random
  }

  :level - if :type == :fixed the volume level of this event. Must be between 0 and 1 inclusive
  :min-volume - if :type == :variable the minimum volume for this event
  :max-volume - if :type == :variable the maximum volume for this event, for :variable type
]
