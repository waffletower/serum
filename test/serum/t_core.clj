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

(fact "and-let bindings are available in expr"
  (and-let [a (+ 4 7)
            b (+ 3 8)
            c (+ 2 9)
            d (+ 1 10)
            e 11]
           (+ a b c d e)) => 55)

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

(fact "shift"
  ((shift -) 3 2) => 1
  ((shift - 1) 2) => 1
  ((shift - 1 2) 3) => 0
  ((shift - 1 2 3) 4) => -2
  ((shift - 1 2 3 4) 5) => -5
  ((shift - 1 2 3 4 5) 6) => -9
  ((shift - 1 2 3 4 5 6) 7) => -14)

(fact "success-let"
  (success-let [peachy nil]
               (assoc peachy :success true)
               "wyatt earp") => (throws Exception)
  (success-let [slackful {:success true}]
               (assoc slackful :advert "test it.")) => {:success true
                                                        :advert "test it."}
  (success-let [slackful {:success true}]
               (assoc slackful :path "life balance")
               (assoc slackful :path "sisyphus")) => {:success true
                                                       :path "life balance"}
  (success-let [encumbered {:success false}]
               (assoc encumbered :success true)) => nil
  (success-let [encumbered {:success false}]
               (assoc encumbered :success true)
               "I got a rock") => "I got a rock")

(fact "fail-let"
  (fail-let [peachy nil]
            "wyatt earp") => (throws Exception)
  (fail-let [slackful {:success true}]
            (assoc slackful :advert "test it.")) => nil
  (fail-let [slackful {:success true}]
            (assoc slackful :path "sisyphus")) => nil
  (fail-let [encumbered {:success false}]
            (assoc encumbered :tuber "rutabaga")) => {:success false
                                                      :tuber "rutabaga"}
  (fail-let [encumbered {:success false}]
            "I got a rock") => "I got a rock")

(letfn [(colorizer [m]
          (assoc m
                 :color "blue"
                 :success true))
        (weaponizer [m]
          (assoc m
                 :weapon "doughnut"
                 :success true))
        (hipifier [m]
          (assoc m
                 :outfit "beanie"
                 :success true))
        (love-infuser [m]
          (assoc m :success false))
        (humidifier [m & args]
          (assoc m
                 :somesum (reduce + args)
                 :success true))]

  (fact "success-> all succeeding list-free"
               (success-> {}
                          colorizer
                          weaponizer
                          hipifier) => {:success true
                                        :color "blue"
                                        :weapon "doughnut"
                                        :outfit "beanie"})

  (fact "success-> all succeeding list-full"
               (success-> {}
                          (colorizer)
                          (weaponizer)
                          (hipifier)) => {:success true
                                          :color "blue"
                                          :weapon "doughnut"
                                          :outfit "beanie"})

  (fact "success-> all succeeding list-full redux"
               (success-> (colorizer {})
                          (weaponizer)
                          (hipifier)) => {:success true
                                          :color "blue"
                                          :weapon "doughnut"
                                          :outfit "beanie"})

  (fact "success-> success with multiple arguments"
               (success-> (colorizer {})
                          (weaponizer)
                          (hipifier)
                          (humidifier 123 74 82)) => {:success true
                                                      :color "blue"
                                                      :weapon "doughnut"
                                                      :outfit "beanie"
                                                      :somesum 279})

  (fact "success-> first failure list-free"
               (success-> {}
                          love-infuser
                          colorizer
                          weaponizer
                          hipifier) => {:success false})

  (fact "success-> first failure list-full"
               (success-> {}
                          (love-infuser)
                          (colorizer)
                          (weaponizer)
                          (hipifier)) => {:success false})

  (fact "success-> final failure mixed listness"
               (success-> (colorizer {})
                          weaponizer
                          hipifier
                          love-infuser) => {:success false
                                            :color "blue"
                                            :weapon "doughnut"
                                            :outfit "beanie"})

  (fact "success-> final failure list-full"
               (success-> {}
                          (colorizer)
                          (weaponizer)
                          (hipifier)
                          (love-infuser)) => {:success false
                                              :color "blue"
                                              :weapon "doughnut"
                                              :outfit "beanie"})

  (fact "success-> another failure possibility"
               (success-> {}
                          colorizer
                          weaponizer
                          love-infuser
                          hipifier) => {:success false
                                        :color "blue"
                                        :weapon "doughnut"}))

(facts "try-true?"
  (try-true? (throw Exception)) => false
  (try-true? true) => true
  (try-true? false) => false
  (try-true? nil) => false
  (try-true? (when true true)) => true
  (try-true? (when true false)) => false
  (try-true? (when true nil)) => false)

(fact "attempt"
  (attempt true (fn [e] e)) => true
  (attempt false (fn [e] e)) => false
  (attempt nil (fn [e] e)) => nil
  (attempt [] (fn [e] e)) => []
  (attempt (/ 1 0) (fn [e] (.getMessage e))) => "Divide by zero"
  (attempt (/ 1 0) (fn [e] nil)) => nil
  (attempt (/ 1 2) (fn [e] e)) => 1/2)

(fact "divert"
  (divert
   (do
     (print "escape-pod")
     7)) => [7 "escape-pod"])

;; find-key-val test exercises `find-pred` at least
(future-fact "find-pred")

(fact "find-key-val"
  (find-key-val :a 33 [{:a 11 :b 22} {:a 33 :b 44} {:a 33 :b 55}]) => {:a 33 :b 44}
  (find-key-val :a 22 [{:a 11 :b 22} {:a 33 :b 44} {:a 33 :b 55}]) => nil
  (find-key-val :a 22 [{:a 11 :b 22} {:a 33 :b 44} {:a 33 :b 55}]) => nil
  (find-key-val :a nil [{:a 11 :b 22} {:a 33 :b 44} {:a 33 :b 55}]) => nil
  (find-key-val nil 2 [{:a 11 :b 22} {:a 33 :b 44} {:a 33 :b 55}]) => nil
  (find-key-val :a 2 nil) => nil
  (find-key-val :a nil nil) => nil
  (find-key-val nil 2 nil) => nil
  (find-key-val nil nil nil) => nil
  (find-key-val nil nil [{:a 11 :b 22} {:a 33 :b 44} {:a 33 :b 55}]) => {:a 11 :b 22})

(fact "within?"
  (within? nil 1) => falsey
  (within? [4 5 6] nil) => falsey
  (within? nil nil) => falsey
  (within? [4 5 6] 1) => falsey
  (within? [4 5 6] 4) => truthy
  (within? [4 5 6] 6) => truthy
  (within? '(4 5 6) 1) => falsey
  (within? '(4 5 6) 4) => truthy
  (within? '(4 5 6) 6) => truthy
  (within?
   ["duck" "spork"]
   (wrap-within-fn clojure.string/lower-case)
   "SPORK") => truthy
  (within?
   ["duck" "spork"]
   (wrap-within-fn = clojure.string/lower-case)
   "BARN") => falsey)
