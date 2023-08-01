(ns serum.string-test
  (:require [serum.string :as sut]
            [clojure.test :refer :all]))

(deftest casefree
  (is (true? (sut/casefree "" "")))
  (is (true? (sut/casefree "DUCK" "duck")))
  (is (true? (sut/casefree "GOOSE" "GOOSE")))
  (is (true? (sut/casefree "gainly" "gAiNlY")))
  (is (false? (sut/casefree nil "dead disco")))
  (is (false? (sut/casefree "dead funk" nil)))
  (is (false? (sut/casefree "dead rock & roll" "dead verismo")))
  (is (true? (sut/casefree nil nil)))
  (is (true? (sut/casefree 1 1)))
  (is (true? (sut/casefree [] [])))
  (is (true? (sut/casefree 2.71828 2.71828))))
