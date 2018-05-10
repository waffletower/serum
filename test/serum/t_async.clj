(ns serum.t-async
  (:require [clojure.string :refer [lower-case]]
            [clojure.core.async :as a]
            [serum.async :refer :all]
            [midje.sweet :refer :all]))

(let [side-effect (atom false)]
  (fire! (do
           (Thread/sleep 40)
           (reset! side-effect true)))
  (Thread/sleep 100)
  (fact "fire!"
    @side-effect => truthy)
  (reset! side-effect false)
  (fire! (do
           (Thread/sleep 100)
           (reset! side-effect true)))
  (fact "fire!"
    @side-effect => falsey))

(let [channel (a/chan)]
  (a/onto-chan
   channel
   ["progress" "ridge" "ottoman"])
  (fact "chan->seq"
    (doall (chan->seq channel)) => ["progress" "ridge" "ottoman"]))

(fact "map-async"
  (doall (map-async lower-case ["Cubby" "Slime" "Cache"])) => ["cubby" "slime" "cache"])
