{
  :min-start-offset - minimum amount of time to wait before starting a loop (defaults to 0)
  :max-start-offset - maximum amount of time to wait before starting a loop (defaults to 0)
    The start of each loop is computed independently using these two values

  :main-bus-effects - a map of effects to add to the main stereo output bus
  {:reverb adds reverb uses the params in the map that follows 
    (:wet-dry - wet/dry ratio (defaults to 0.5)
     :room-size - room size (0-1 defaults to 0.5)
     :dampening - high freq dampening (0 - 1 defaults to 0.2)
    )
  }
 
 :loops - array of 1 or more maps of loops to play 
 [
  {:name - loop name 
   :loop-type Required options for type :loop. Plays :melody-info in a loop 
   :instrument-name an instrument name from src/splice/instr/instrument.clj - all-instruments
   :melody-info - an array of melody elemants to play
   [
    {:pitch - Required - Map containing pitch information for this note
     :dur - Required. Map containing duration info for this event.
     :volume - Required if pitch is not nil. volume of melody element 0 - 1.
     :instrument-settings - Optional A list of keys and values of settings for this instrument. Example: (:attack 4.0 :release 3.0)
    }
   ]
  }
 ]
}

:pitch
[
  {
    :type - :fixed | :variable | :rest
    :pitch-midi-note - This or :pitch-freq are required if :type == :fixed
    :pitch-freq - This or :pitch-midi-note are required if :type == :fixed
    :pitch-type - Required if type == :variable.
    :pitches - Required if :type == :variable.
  }

  :pitch-midi-note - The midi note number of the note to play
  :pitch-freq - The frequency (Hz) of the note to play
  :pitch-type - :midi-note | :freq
  :pitches - An array of integers representing midi-notes if :pitch-type == :midi-note or
             frequencies (Hz.) if :pitch-type ==:freq.
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
  }

  :dur-millis - if type == :fixed, the duration of this loop event
              if type == :variable-inc-millis, the base duration of this event
  :min-millis - if :type == :variable-millis, the minimum duration of this event
  :max-millis - if :type == :variable-millis, the maximum duration of this event
  :inc-millis - if :type == :variable-inc-millis, the maximum amount dur-millis can be incremented
  :dec-millis - if :type == :variable-inc-millis, the maximum amount dur-millis can be decremented
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

:range-lo and :range-hi
[
  minimum and maximum midi note numbers for the instrument's pitch
]
