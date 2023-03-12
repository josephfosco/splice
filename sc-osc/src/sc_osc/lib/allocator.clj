;; Copied from the Overtone project overtone.sc.machinery.allocator

(ns sc-osc.lib.allocator
  "ID allocator system. Used to return new unique integer IDs
   in a threadsafe manner. IDs may be freed and therefore
   reused. Allows action fns to be executed in synchronisation
   with allocation and deallocation.
   Author: Sam Aaron"
  (:use [sc-osc.lib.defaults])
  (:require [sc-osc.lib.config :refer [get-config]]
            [sc-osc.lib.deps :refer [on-deps satisfy-deps]]
            [sc-osc.lib.log :as log]))

;; This file is used to track the use of buses (audio buses and control buses)
;; There are no osc commands to use a bus, you can just send output to any bus
;; supercollider has access to and the signal will be palced there. It is, however,
;; necessary to track which buses are being used so that you can know what bus numbers
;; are available when you wish to use a new bus. The use of buses is tracked in the
;; dictionary "allocator-bits". Each dictioary contains a list of entries for the number of
;; control or audio buses that are available. Entries for buses that are unused buses
;; contain a value of false and buses that are being used are set to true. The number of
;; available buses is set in the config variable

;; ## Allocators
;;
;; We use bit sets to store the allocation state of resources
;; on the audio server.  These typically get allocated on usage by the client,
;; and then freed either by client request or automatically by receiving
;; notifications from the server.  (e.g. When an envelope trigger fires to
;; free a synth.)

(defn mk-bitset
  "Create a vector representation of a bitset"
  [size]
  (vec (repeat size false)))

;; This finction is changed from the corresponding function in overtone
;; Since this lib is not getting arg default from the supercollidre derver,
;; the required values are coming from config.clj. If in the future functionality
;; is added to get values directly from supercollider, this can be updated.
;; Overtone get values from supercollider in the file
;; overtone/src/overtone/sc/machinery/server/args.clj
(defonce allocator-bits
  {:audio-bus    (ref (mk-bitset (get-config :sc-max-audio-buses)))
   :control-bus  (ref (mk-bitset (get-config :sc-max-control-buses)))})

(defn- fill-gaps
  "Returns a new vector similar to bs except filled with with size
   consecutive vals from idx

   example: (fill with 3 from idx 1 for 2 vals)
   (fill-gaps [1 0 0 0 1] 1 2 3) ;=> [1 3 3 0 1]"
  [bs idx size val]
  (loop [bs bs
         idx idx
         size size]
    (if (> size 0)
      (recur (assoc bs idx val) (inc idx) (dec size))
      bs)))

(defn- find-gap
  "Returns index of the first gap in vector bs with specified size where
   gap is defined as a falsey value.

  example:
  (find-gap [true false true false false false true] 3) ;=> 3"
  ([bs size] (find-gap bs size 0 0))
  ([bs size idx gap-found]
     (let [limit (count bs)]
       (when (> idx limit)
         (throw (Exception. (str "No more ids! Unable to allocate a sequence of ids of length: " size))))
       (if (= gap-found size)
         (- idx gap-found)
         (let [gap-found (if (not (get bs idx)) (inc gap-found) 0)]
           (find-gap bs size (inc idx) gap-found))))))

(defonce action-fn-executor* (agent nil))

(defn- execute-action-fn
  "Execute action-fn and catch all exceptions - outputting them to the
   error log. All actions are executed in sequence."
  [action-fn caller-name]
  (send action-fn-executor* (fn [_]
                              (try
                                (action-fn)
                                (catch Exception e
                                  (log/error "Exception in " caller-name " action-fn: " e "\nstacktrace: " (.printStackTrace e)))
                                (finally nil)))))

(defn alloc-id
  "Allocate a new ID for the type corresponding to key. Takes an
   optional action-fn which it will evaluate in transaction with the
   allocation of the id.  Therefore there is no possibility of
   interleaving concurrent allocation of ids and the execution of
   associated action-fns. Execution of action-fn is also synchronised
   with the execution of free-id action-fns. Action-fn takes one param -
   the newly allocated id.

   Returns newly allocated id."
  ([k] (alloc-id k 1 nil))
  ([k size] (alloc-id k size nil))
  ([k size action-fn]
     (let [bits  (get allocator-bits k)]
       (when-not bits
         (throw (Exception. (str "Unable to get allocator bits for keyword " k))))
       (dosync
        (let [id (find-gap @bits size)]
          (alter bits fill-gaps id size true)
          (when action-fn (execute-action-fn #(action-fn id) "alloc-id"))
          id)))))

(defn free-id
  "Free the ID of type key. Takes an optional action-fn which it will
   evaluate in transaction with the freeing of the id. (Evaluation
   happens within an agent so there's no need to worry about the
   transaction retrying multiple times).  Therefore there is no
   possibility of interleaving concurrent freeing of ids and execution
   of associated action-fns. Execution of action-fn is also synchronised
   with the execution of alloc-id action-fns (they all use the same
   agent)."
  ([k id] (free-id k id 1 nil))
  ([k id size] (free-id k id size nil))
  ([k id size action-fn]
     (let [bits (get allocator-bits k)]
       (dosync
        (alter bits fill-gaps id size false)
        (when action-fn (execute-action-fn action-fn "free-id"))))))

(defn clear-ids
  "Clear all ids allocated for key."
  [k]
  (let [bits (get allocator-bits k)]
    (dosync
     (let [new-bitset (mk-bitset (count @bits))]
       (ref-set bits new-bitset)))
    :cleared))

;; -----------------------------------------------------------------------------
;; In overtone the following function and on-deps call are defined in
;; overtone/src/overtone/sc/bus.clj but since this file has not been copied
;; over, the functionality has been implemented here in the allocator (which
;; deals with buses) Since this code functions differently than the
;; corresponding code in overtone, If the overtone bus file is ever brought
;; into this repo, either this code or the code in bus.clj will need to be
;; modified and/or deleted.
;; -----------------------------------------------------------------------------

(defn allocate-hw-audio-buses
  []
  (let [n-buses (+ (get-config :sc-default-input-buses)
                   (get-config :sc-default-output-buses))]
    (alloc-id :audio-bus n-buses)
    (satisfy-deps :hw-audio-buses-reserved)))

;; overtone does this on-deps :synthdefs-loaded but there is no need to
;; wait and :synthdefs-loaded is not being used within this lib. Since this
;; is important to reserve the hw bus ids, it is important to keep this
;; local to this lib and not require the app this lib is included in to
;; satisfy a specific deps to reserve the bus ids
(on-deps :server-connected ::allocate-hw-audio-busses allocate-hw-audio-buses)
