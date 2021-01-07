(ns serum.t-async
  (:require
    [clojure.core.async :as a]
    [clojure.string :refer [lower-case]]
    [midje.sweet :refer :all]
    [serum.async :refer :all]))


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
  (fact "chan->seq happy path"
        (doall (chan->seq channel)) => ["progress" "ridge" "ottoman"]))


(let [channel (a/chan)]
  (a/onto-chan
    channel
    ["progress" "ridge" (Exception. "not quite sure what went wrong") "ottoman"])
  (fact "chan->seq throws any exception object encounter on input channel"
        (doall (chan->seq channel)) => (throws Exception)))


(facts "map-async"
       (doall (map-async lower-case ["Cubby" "Slime" "Cache"])) => ["cubby" "slime" "cache"]
       (doall (map-async (partial / 1) [1 0 2])) => (throws Exception))
