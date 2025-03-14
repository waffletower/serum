(ns serum.async-test
  (:require [serum.async :as sut]
            [clojure.core.async :as a]
            [clojure.test :refer :all]
            [clojure.string :refer [lower-case]]))

(deftest fire!
  (let [side-effect (atom false)]
    (sut/fire! (do
             (Thread/sleep 40)
             (reset! side-effect true)))
    (Thread/sleep 100)
    (is (true? @side-effect))

    (reset! side-effect false)
    (sut/fire! (do
             (Thread/sleep 100)
             (reset! side-effect true)))
    (is (false? @side-effect))))

(deftest onto-chan
  (let [channel (a/chan)]
    (a/onto-chan
      channel
      ["progress" "ridge" "ottoman"])
    (testing "chan->seq happy path"
      (is
        (= ["progress" "ridge" "ottoman"]
           (doall (sut/chan->seq channel))))))

  (let [channel (a/chan)]
    (a/onto-chan
      channel
      ["progress" "ridge" (Exception. "not quite sure what went wrong") "ottoman"])
    (testing "chan->seq throws any exception object encounter on input channel"
      (is (thrown? Exception (doall (sut/chan->seq channel)))))))

(deftest map-async
  (is (= ["cubby" "slime" "cache"]
         (doall (sut/map-async lower-case ["Cubby" "Slime" "Cache"]))))
  (is (thrown?
        Exception
        (doall (sut/map-async (partial / 1) [1 0 2])))))

(deftest map-exec
  (is (= ["cubby" "slime" "cache"]
         (sut/map-exec lower-case ["Cubby" "Slime" "Cache"])))
  (is (thrown? Exception
        (sut/map-exec (partial / 1) [1 0 2]))))

(deftest disorder-exec
  (is (= #{"cubby" "slime" "cache"}
         (-> (sut/disorder-exec lower-case ["Cubby" "Slime" "Cache"])
             set)))
  (is (thrown? Exception
        (sut/disorder-exec (partial / 1) [1 0 2]))))

(deftest side-exec
  (let [st (atom [])]
    (is (= #{"cubby" "slime" "cache"}
           (do (sut/side-exec
                 (comp
                   #(swap! st conj %)
                   lower-case)
                 ["Cubby" "Slime" "Cache"])
               (set @st)))))
  (is (thrown? Exception
        (sut/side-exec (partial / 1) [1 0 2]))))
