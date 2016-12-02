(ns serum.t-core
  (:require [midje.sweet :refer :all]
            [serum.core :refer :all]))

(fact "and-let"
  (and-let [a nil] 4) => nil
  (and-let [a false] 7) => nil
  (and-let [a true] 11) => 11
  (and-let [a 1 b false] b) => nil
  (and-let [a 49 b (inc a)] b) => 50
  (and-let [a 17 b (inc a) c (inc b) d (inc c) e (inc d)] e) => 21
  (and-let [a 82 b (inc a) c (inc b) d (inc c) e false] e) => nil
  (and-let [a 643 b (inc a) c (inc b) d (inc c) e false] e false) => false
  (and-let [a (+ (* 2 3) 9)
            b (* (- a 2) 2)
            c (- (+ b 2) 1)
            d (mod (inc c) 10)
            e (* d 4)] e false) => 32)

;; tunneled iteratively a little bit on this one, but it was late and I was still having fun
(fact "explicit binding execution checks for and-let"
  (let [bark (atom 0)
        curd (atom 0)
        dank (atom 0)
        eels (atom 0)
        result (and-let [a false
                         b (reset! bark 1)
                         c (reset! curd 1)]
                        (reset! dank (+ b c))
                        (reset! eels -1))]
    result => -1
    @bark => 0
    @curd => 0
    @dank => 0
    @eels => -1)

  (let [atop (atom 0)
        curd (atom 0)
        dank (atom 0)
        eels (atom 0)
        result (and-let [a (reset! atop 1)
                         b false
                         c (reset! curd 1)]
                        (reset! dank -1)
                        (reset! eels -1))]
    result => -1
    @atop => 1
    @curd => 0
    @dank => 0
    @eels => -1)

  (let [atop (atom 0)
        bark (atom 0)
        dank (atom 0)
        eels (atom 0)
        result (and-let [a (reset! atop 1)
                         b (reset! bark 1)
                         c false]
                        (reset! dank -1)
                        (reset! eels -1))]
    result => -1
    @atop => 1
    @bark => 1
    @dank => 0
    @eels => -1)

  (let [atop (atom 0)
        bark (atom 0)
        curd (atom 0)
        eels (atom 0)
        foes (atom 0)
        result (and-let [a (reset! atop 1)
                         b (reset! bark 1)
                         c (reset! curd 1)
                         d false]
                        (reset! eels -1)
                        (reset! foes -1))]
    result => -1
    @atop => 1
    @bark => 1
    @curd => 1
    @eels => 0
    @foes => -1)

  (let [atop (atom 0)
        bark (atom 0)
        curd (atom 0)
        dank (atom 0)
        eels (atom 0)
        foes (atom 0)
        result (and-let [a (reset! atop 1)
                         b (reset! bark 1)
                         c (reset! curd 1)
                         d (reset! curd 1)]
                        (reset! eels -1)
                        (reset! foes -1))]
    result => -1
    @atop => 1
    @bark => 1
    @curd => 1
    @eels => -1
    @foes => 0))