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
  (fact "chan->seq happy path"
    (doall (chan->seq channel)) => ["progress" "ridge" "ottoman"]))

(let [channel (a/chan)]
  (a/onto-chan
   channel
   ["progress" "ridge" (Exception. "not quite sure what went wrong") "ottoman"])
  (fact "chan->seq throws any exception object encounter on input channel"
    (doall (chan->seq channel)) => (throws Exception)))
