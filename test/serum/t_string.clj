(ns serum.t-string
  (:require [serum.string :refer :all]
            [midje.sweet :refer :all]))

(fact "casefree"
  (casefree "" "") => true
  (casefree "DUCK" "duck") => true
  (casefree "GOOSE" "GOOSE") => true
  (casefree "gainly" "gAiNlY") => true
  (casefree nil "dead disco") => false
  (casefree "dead funk" nil) => false
  (casefree "dead rock & roll" "dead verismo") => false
  (casefree nil nil) => true
  (casefree 1 1) => true
  (casefree [] []) => true
  (casefree 2.71828 2.71828) => true)
