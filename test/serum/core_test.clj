(ns serum.core-test
  (:require [serum.core :as sut]
            [clojure.test :refer :all]))

(deftest and-let
  (is (nil? (sut/and-let [a nil] 4)))
  (is (nil? (sut/and-let [a false] 7)))
  (is (nil? (sut/and-let [a 1 b false] b)))
  (is (nil? (sut/and-let [a 82 b (inc a) c (inc b) d (inc c) e false] e)))
  (is (= 11 (sut/and-let [a true] 11)))
  (is (= 50 (sut/and-let [a 49 b (inc a)] b)))
  (is (= 21 (sut/and-let [a 17 b (inc a) c (inc b) d (inc c) e (inc d)] e)))
  (is (= 32 (sut/and-let [a (+ (* 2 3) 9)
                          b (* (- a 2) 2)
                          c (- (+ b 2) 1)
                          d (mod (inc c) 10)
                          e (* d 4)] e false)))
  (is (false? (sut/and-let [a 643 b (inc a) c (inc b) d (inc c) e false] e false))))

(testing "sut/and-let bindings are available in expr"
  (is (= 55
         (sut/and-let [a (+ 4 7)
                       b (+ 3 8)
                       c (+ 2 9)
                       d (+ 1 10)
                       e 11]
                      (+ a b c d e)))))

;; tunneled iteratively a little bit on this one, but it was late and I was still having fun
(testing "explicit binding execution checks for and-let"
  (let [bark (atom 0)
        curd (atom 0)
        dank (atom 0)
        eels (atom 0)
        result (sut/and-let [a false
                             b (reset! bark 1)
                             c (reset! curd 1)]
                            (reset! dank (+ b c))
                            (reset! eels -1))]
    (is (= -1 result))
    (is (= -1 @eels))
    (is (= 0 @bark))
    (is (= 0 @curd))
    (is (= 0 @dank)))

  (let [atop (atom 0)
        curd (atom 0)
        dank (atom 0)
        eels (atom 0)
        result (sut/and-let [a (reset! atop 1)
                             b false
                             c (reset! curd 1)]
                            (reset! dank -1)
                            (reset! eels -1))]
    (is (= -1 result))
    (is (= -1 @eels))
    (is (= 1 @atop))
    (is (= 0 @curd))
    (is (= 0 @dank)))

  (let [atop (atom 0)
        bark (atom 0)
        dank (atom 0)
        eels (atom 0)
        result (sut/and-let [a (reset! atop 1)
                             b (reset! bark 1)
                             c false]
                            (reset! dank -1)
                            (reset! eels -1))]
    (is (= -1 result))
    (is (= -1 @eels))
    (is (= 1 @atop))
    (is (= 1 @bark))
    (is (= 0 @dank)))

  (let [atop (atom 0)
        bark (atom 0)
        curd (atom 0)
        eels (atom 0)
        foes (atom 0)
        result (sut/and-let [a (reset! atop 1)
                             b (reset! bark 1)
                             c (reset! curd 1)
                             d false]
                            (reset! eels -1)
                            (reset! foes -1))]
    (is (= -1 result))
    (is (= -1 @foes))
    (is (= 1 @atop))
    (is (= 1 @bark))
    (is (= 1 @curd))
    (is (= 0 @eels)))

  (let [atop (atom 0)
        bark (atom 0)
        curd (atom 0)
        dank (atom 0)
        eels (atom 0)
        foes (atom 0)
        result (sut/and-let [a (reset! atop 1)
                             b (reset! bark 1)
                             c (reset! curd 1)
                             d (reset! curd 1)]
                            (reset! eels -1)
                            (reset! foes -1))]
    (is (= -1 result))
    (is (= -1 @eels))
    (is (= 1 @atop))
    (is (= 1 @bark))
    (is (= 1 @curd))
    (is (= 0 @foes))))

(deftest shift
  (is (= 1 ((sut/shift -) 3 2)))
  (is (= 1 ((sut/shift - 1) 2)))
  (is (= 0 ((sut/shift - 1 2) 3)))
  (is (= -2 ((sut/shift - 1 2 3) 4)))
  (is (= -5 ((sut/shift - 1 2 3 4) 5)))
  (is (= -9 ((sut/shift - 1 2 3 4 5) 6)))
  (is (= -14 ((sut/shift - 1 2 3 4 5 6) 7))))

(deftest success-let
  (is (thrown? Exception (sut/success-let [peachy nil]
                                          (assoc peachy :success true)
                                          "wyatt earp")))
  (is (= {:success true
          :advert "test it."}
         (sut/success-let [slackful {:success true}]
                          (assoc slackful :advert "test it."))))
  (is (= {:success true
          :path "life balance"}
         (sut/success-let [slackful {:success true}]
                          (assoc slackful :path "life balance")
                          (assoc slackful :path "sisyphus"))))
  (is (nil? (sut/success-let [encumbered {:success false}]
                             (assoc encumbered :success true))))
  (is (= "I got a rock"
         (sut/success-let [encumbered {:success false}]
                          (assoc encumbered :success true)
                          "I got a rock"))))

(deftest fail-let
  (is (thrown? Exception (sut/fail-let [peachy nil] "wyatt earp")))
  (is (nil? (sut/fail-let [slackful {:success true}]
                          (assoc slackful :advert "test it."))))
  (is (nil? (sut/fail-let [slackful {:success true}]
                          (assoc slackful :path "sisyphus"))))
  (is (= {:success false
          :tuber "rutabaga"}
         (sut/fail-let [encumbered {:success false}]
                       (assoc encumbered :tuber "rutabaga"))))
  (is (= "I got a rock"
         (sut/fail-let [encumbered {:success false}]
                       "I got a rock"))))

(letfn [(colorizer
          [m]
          (assoc m
                 :color "blue"
                 :success true))
        (weaponizer
          [m]
          (assoc m
                 :weapon "doughnut"
                 :success true))
        (hipifier
          [m]
          (assoc m
                 :outfit "beanie"
                 :success true))
        (love-infuser
          [m]
          (assoc m :success false))
        (humidifier
          [m & args]
          (assoc m
                 :somesum (reduce + args)
                 :success true))]

  (testing "success-> all succeeding list-free"
    (is (= {:success true
            :color "blue"
            :weapon "doughnut"
            :outfit "beanie"}
           (sut/success-> {}
                          colorizer
                          weaponizer
                          hipifier))))

  (testing "success-> all succeeding list-full"
    (is (= {:success true
            :color "blue"
            :weapon "doughnut"
            :outfit "beanie"}
           (sut/success-> {}
                          (colorizer)
                          (weaponizer)
                          (hipifier)))))

  (testing "success-> all succeeding list-full redux"
    (is (= {:success true
            :color "blue"
            :weapon "doughnut"
            :outfit "beanie"}
           (sut/success-> (colorizer {})
                          (weaponizer)
                          (hipifier)))))

  (testing "success-> success with multiple arguments"
    (is (= {:success true
            :color "blue"
            :weapon "doughnut"
            :outfit "beanie"
            :somesum 279}
           (sut/success-> (colorizer {})
                          (weaponizer)
                          (hipifier)
                          (humidifier 123 74 82)))))

  (testing "success-> first failure list-free"
    (is (= {:success false}
           (sut/success-> {}
                          love-infuser
                          colorizer
                          weaponizer
                          hipifier))))

  (testing "success-> first failure list-full"
    (is (= {:success false}
           (sut/success-> {}
                          (love-infuser)
                          (colorizer)
                          (weaponizer)
                          (hipifier)))))

  (testing "success-> final failure mixed listness"
    (is (= {:success false
            :color "blue"
            :weapon "doughnut"
            :outfit "beanie"}
           (sut/success-> (colorizer {})
                          weaponizer
                          hipifier
                          love-infuser))))

  (testing "success-> final failure list-full"
    (is (= {:success false
            :color "blue"
            :weapon "doughnut"
            :outfit "beanie"}
           (sut/success-> {}
                          (colorizer)
                          (weaponizer)
                          (hipifier)
                          (love-infuser)))))

  (testing "success-> another failure possibility"
    (is (= {:success false
            :color "blue"
            :weapon "doughnut"}
           (sut/success-> {}
                          colorizer
                          weaponizer
                          love-infuser
                          hipifier)))))

(deftest try-true?
  (is (true? (sut/try-true? true)))
  (is (true? (sut/try-true? (when true true))))
  (is (false? (sut/try-true? nil)))
  (is (false? (sut/try-true? false)))
  (is (false? (sut/try-true? (when true false))))
  (is (false? (sut/try-true? (when true nil))))
  (is (false? (sut/try-true? (throw Exception)))))

(deftest attempt
  (is (true? (sut/attempt true (fn [e] e))))
  (is (false? (sut/attempt false (fn [e] e))))
  (is (nil? (sut/attempt nil (fn [e] e))))
  (is (= [] (sut/attempt [] (fn [e] e))))
  (is (=  "Divide by zero" (sut/attempt (/ 1 0) (fn [e] (.getMessage e)))))
  (is (nil? (sut/attempt (/ 1 0) (fn [e] nil))))
  (is (= 1/2 (sut/attempt (/ 1 2) (fn [e] e)))))

(deftest divert
  (is (= [7 "escape-pod"]
         (sut/divert
           (do
             (print "escape-pod")
             7)))))

;; find-key-val test exercises `find-pred` at least
(deftest find-key-val
  (is (= {:a 33 :b 44} (sut/find-key-val :a 33 [{:a 11 :b 22} {:a 33 :b 44} {:a 33 :b 55}])))
  (is (= {:a 11 :b 22} (sut/find-key-val nil nil [{:a 11 :b 22} {:a 33 :b 44} {:a 33 :b 55}])))
  (is (nil? (sut/find-key-val :a 22 [{:a 11 :b 22} {:a 33 :b 44} {:a 33 :b 55}])))
  (is (nil? (sut/find-key-val :a 22 [{:a 11 :b 22} {:a 33 :b 44} {:a 33 :b 55}])))
  (is (nil? (sut/find-key-val :a nil [{:a 11 :b 22} {:a 33 :b 44} {:a 33 :b 55}])))
  (is (nil? (sut/find-key-val nil 2 [{:a 11 :b 22} {:a 33 :b 44} {:a 33 :b 55}])))
  (is (nil? (sut/find-key-val :a 2 nil)))
  (is (nil? (sut/find-key-val :a nil nil)))
  (is (nil? (sut/find-key-val nil 2 nil)))
  (is (nil? (sut/find-key-val nil nil nil))))

(deftest within?
  (is (nil? (sut/within? nil 1)))
  (is (nil? (sut/within? [4 5 6] nil)))
  (is (nil? (sut/within? nil nil)))
  (is (nil? (sut/within? [4 5 6] 1)))
  (is (nil? (sut/within? '(4 5 6) 1)))
  (is (true? (sut/within? [4 5 6] 4)))
  (is (true? (sut/within? [4 5 6] 6)))
  (is (true? (sut/within? '(4 5 6) 4)))
  (is (true? (sut/within? '(4 5 6) 6)))
  (is (true?
         (sut/within?
           ["duck" "spork"]
           (sut/wrap-within-fn clojure.string/lower-case)
           "SPORK")))
  (is (nil? (sut/within?
              ["duck" "spork"]
              (sut/wrap-within-fn clojure.string/lower-case)
              "BARN"))))


;; TODO write tests demonstrating thread-safety for `do-once`
(deftest do-once
  (let [ca (atom 0)
        cl (sut/do-once (fn [] (swap! ca inc) (+ 4 @ca)))]
    (is (= 5 (cl)))
    (is (= 1 @ca))
    (is (= 5 (cl)))
    (is (= 1 @ca))))
