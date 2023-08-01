(ns serum.data-test
  (:require [serum.data :as sut]
            [clojure.test :refer :all]
            [clojure.string :refer [lower-case upper-case]]))

(deftest index-unique-maps
  (is (= {}
         (sut/index-unique-maps
           :duck
           [{:duckling "mallard"} {:duckling "mighty"}])))
  (is (= {"SOLENOID" {:part "SOLENOID"}}
         (sut/index-unique-maps
           :part
           [{:part "SOLENOID"}])))
  (is (= {"head gasket" {:part "HEAD GASKET"}}
         (sut/index-unique-maps
           lower-case
           :part
           [{:part "HEAD GASKET"}])))
  (is (= {"habenero" {:sauce "habenero" :scovilles :moderate}
          "ghost" {:sauce "ghost" :scovilles :high}
          "pequena" {:sauce "pequena" :scovilles :moderate}}
         (sut/index-unique-maps
           :sauce
           [{:sauce "habenero" :scovilles :moderate}
            {:sauce "ghost" :scovilles :high}
            {:sauce "pequena" :scovilles :high}
            {:sauce "pequena" :scovilles :moderate}]))))

(deftest index-maps
  (is (= {"SOLENOID" [{:part "SOLENOID"}]}
         (sut/index-maps
           :part
           [{:part "SOLENOID"}])))
  (is (= {"head gasket" [{:part "HEAD GASKET"}]}
         (sut/index-maps
           lower-case
           :part
           [{:part "HEAD GASKET"}])))
  (is (= {"dirty trogg cloth" [{:part "Dirty Trogg Cloth"
                                :model 2}
                               {:part "dirty trogg cloth"
                                :model 1}]}
         (sut/index-maps
           lower-case
           :part
           [{:part "dirty trogg cloth"
             :model 1}
            {:part "Dirty Trogg Cloth"
             :model 2}])))
  (is (= {"habenero" [{:sauce "habenero" :scovilles :moderate}]
          "ghost" [{:sauce "ghost" :scovilles :high}]
          "pequena" [{:sauce "pequena" :scovilles :moderate}
                     {:sauce "pequena" :scovilles :high}]}
         (sut/index-maps
           :sauce
           [{:sauce "habenero" :scovilles :moderate}
            {:sauce "ghost" :scovilles :high}
            {:sauce "pequena" :scovilles :high}
            {:sauce "pequena" :scovilles :moderate}]))))

(deftest sorted-map-desc
  (is (= (vec
           (array-map
             :pasilla 4
             :ancho 3
             :habenero 2
             :chile-de-arbol 1))
         (vec
           (sut/sorted-map-desc
             (array-map
               :habenero 2
               :chile-de-arbol 1
               :pasilla 4
               :ancho 3))))))

(deftest sorted-map-asc
  (is (= (vec
           (array-map
             :chile-de-arbol 1
             :habenero 2
             :ancho 3
             :pasilla 4))
         (vec
           (sut/sorted-map-asc
             (array-map
               :habenero 2
               :chile-de-arbol 1
               :pasilla 4
               :ancho 3))))))

(deftest proc-keys
      (is (= {"HALL" {"POLITICIANS" {"HANKERCHIEFS" 100}}}
             (sut/proc-keys
               upper-case
               {"hall" {"politicians" {"hankerchiefs" 100}}})))
      (is (= {"TENTS" [{"COLOR" "green"
                        "SIZE" "matchbook"}
                       {"COLOR" "fuschia"
                        "SIZE" "micron"}]}
             (sut/proc-keys
               upper-case
               {"tents" [{"color" "green"
                          "size" "matchbook"}
                         {"color" "fuschia"
                          "size" "micron"}]}))))

(deftest proc-vals
  (is (= {:birds 18
          :types {:swans {:total 4 :kids 3}
                  :geese {:total 6 :kids 4}
                  :ducks {:total 10 :kids 6}}}
         (sut/proc-vals
           (fn [x]
             (if (number? x)
               (inc x)
               x))
           {:birds 17
            :types {:swans {:total 3 :kids 2}
                    :geese {:total 5 :kids 3}
                    :ducks {:total 9 :kids 5}}}))))

(deftest proc-val
  (is (= {:candy "LIK-M-AID"
          :drink "icee"
          :store {:name "dunagrees"
                  :candy "FRESHEN UP"}}
         (sut/proc-val upper-case
                       {:candy "lik-m-aid"
                        :drink "icee"
                        :store {:name "dunagrees"
                                :candy "freshen up"}}
                       :candy)))
  (is (= {:inventory {:animal "orca"
                      :paint "rodda"
                      :cutlery "FORK"}}
         (sut/proc-val upper-case
                       {:inventory {:animal "orca"
                                    :paint "rodda"
                                    :cutlery "fork"}}
                       :cutlery))))

(deftest proc-with-map
      (let [key-fn-map {:theremin inc
                        :ondes-martenot dec}]
        (is (= {:theremin 2
                :ondes-martenot 0}
               (sut/proc-with-map key-fn-map
                                  {:theremin 1
                                   :ondes-martenot 1})))
        (is (= [{:basement [{:theremin 2
                             :ondes-martenot 0}
                            {:echoplex 1}]}]
               (sut/proc-with-map key-fn-map
                                  [{:basement [{:theremin 1
                                                :ondes-martenot 1}
                                               {:echoplex 1}]}])))))

(deftest proc-top-keys
  (is (= {"HALL" {"politicians" {"hankerchiefs" 100}}}
         (sut/proc-top-keys
           upper-case
           {"hall" {"politicians" {"hankerchiefs" 100}}}))))

(deftest proc-top-vals
  (is (= {:birds 18
          :types {:swans {:total 3 :kids 2}
                  :geese {:total 5 :kids 3}
                  :ducks {:total 9 :kids 5}}}
         (sut/proc-top-vals
           (fn [x]
             (if (number? x)
               (inc x)
               x))
           {:birds 17
            :types {:swans {:total 3 :kids 2}
                    :geese {:total 5 :kids 3}
                    :ducks {:total 9 :kids 5}}}))))

(deftest remap-keys
  (is (= {:president "shambolic"}
         (sut/remap-keys
           {:prime-minister "shambolic"}
           {:prime-minister :president})))
  (is (= {}
         (sut/remap-keys
           {:prime-minister "shambolic"}
           {:dentine :trident})))
  (is (= {:post-kardashian "devo"
          :single-payer "sound-of-silence"}
         (sut/remap-keys
           {:pre-cambrian "devo"
            :psionic-fiend "mind-flayer"
            :garfunkle-says "sound-of-silence"}
           {:pre-cambrian :post-kardashian
            :garfunkle-says :single-payer})))
  (is (= {}
         (sut/remap-keys
           {:blodgett "staff-of-life"}
           {})))
  (is (= {}
         (sut/remap-keys
           {:blodgett "staff-of-life"}
           nil)))
  (is (= {}
         (sut/remap-keys
           nil
           {:your-comfort "second-concern"})))
  (is (= {}
         (sut/remap-keys
           {}
           {})))
  (is (= {}
         (sut/remap-keys
           {}
           nil)))
  (is (= {}
         (sut/remap-keys
           nil
           {})))
  (is (= {}
         (sut/remap-keys
           nil
           nil))))

(deftest case-conversion
  (let [kebab {:dental-floss {:proctor-gamble false :ashley-madison true :sample-hold {:meal-worm true}}}
        header {:Dental-Floss {:Proctor-Gamble false :Ashley-Madison true :Sample-Hold {:Meal-Worm true}}}
        camel {:dentalFloss {:proctorGamble false :ashleyMadison true :sampleHold {:mealWorm true}}}
        pascal {:DentalFloss {:ProctorGamble false :AshleyMadison true :SampleHold {:MealWorm true}}}
        snake {:dental_floss {:proctor_gamble false :ashley_madison true :sample_hold {:meal_worm true}}}
        scream {:DENTAL_FLOSS {:PROCTOR_GAMBLE false :ASHLEY_MADISON true :SAMPLE_HOLD {:MEAL_WORM true}}}]

    (testing "keys->kebabs"
      (is (= kebab (sut/keys->kebabs header)))
      (is (= kebab (sut/keys->kebabs camel)))
      (is (= kebab (sut/keys->kebabs pascal)))
      (is (= kebab (sut/keys->kebabs snake)))
      (is (= kebab (sut/keys->kebabs scream))))

    (testing "keys->HTTP-Header-Case"
      (is (= header (sut/keys->HTTP-Header-Case kebab)))
      (is (= header (sut/keys->HTTP-Header-Case camel)))
      (is (= header (sut/keys->HTTP-Header-Case pascal)))
      (is (= header (sut/keys->HTTP-Header-Case snake)))
      (is (= header (sut/keys->HTTP-Header-Case scream))))

    (testing "keys->camelCase"
      (is (= camel (sut/keys->camelCase kebab)))
      (is (= camel (sut/keys->camelCase header)))
      (is (= camel (sut/keys->camelCase pascal)))
      (is (= camel (sut/keys->camelCase snake)))
      (is (= camel (sut/keys->camelCase scream))))

    (testing "keys->PascalCase"
      (is (= pascal (sut/keys->PascalCase kebab)))
      (is (= pascal (sut/keys->PascalCase header)))
      (is (= pascal (sut/keys->PascalCase camel)))
      (is (= pascal (sut/keys->PascalCase snake)))
      (is (= pascal (sut/keys->PascalCase scream))))

    (testing "keys->snake_case"
      (is (= snake (sut/keys->snake_case kebab)))
      (is (= snake (sut/keys->snake_case header)))
      (is (= snake (sut/keys->snake_case camel)))
      (is (= snake (sut/keys->snake_case pascal)))
      (is (= snake (sut/keys->snake_case scream))))

    (testing "keys->SCREAMING_SNAKE_CASE"
      (is (= scream (sut/keys->SCREAMING_SNAKE_CASE kebab)))
      (is (= scream (sut/keys->SCREAMING_SNAKE_CASE header)))
      (is (= scream (sut/keys->SCREAMING_SNAKE_CASE camel)))
      (is (= scream (sut/keys->SCREAMING_SNAKE_CASE pascal)))
      (is (= scream (sut/keys->SCREAMING_SNAKE_CASE snake))))))

(deftest keys-symmetry
  (let [stringy {"motto" "thank heaven" "conbini" "7/11"}
        keyly {:motto "thank heaven" :conbini "7/11"}]
    (is (= keyly
           (-> stringy
               sut/keys->keywords
               sut/keys->strings
               sut/keys->keywords)))))

(deftest namespace-preservation
  (let [k :there.is.water/flowing-down
        m {k true}]
    (is (= k (sut/proc-ns-key identity k)))
    (is (= m (sut/keys->kebabs m)))
    (is (= m (sut/keys->keywords m)))
    (is (= {:there.is.water/Flowing-Down true} (sut/keys->HTTP-Header-Case m)))
    (is (= {:there.is.water/flowingDown true} (sut/keys->camelCase m)))
    (is (= {:there.is.water/FlowingDown true} (sut/keys->PascalCase m)))
    (is (= {:there.is.water/flowing_down true} (sut/keys->snake_case m)))
    (is (= {:there.is.water/FLOWING_DOWN true} (sut/keys->SCREAMING_SNAKE_CASE m)))))

(deftest rotate
  (is (nil? (sut/rotate nil)))
  (is (= [] (sut/rotate [])))
  (is (= [] (sut/rotate [[]])))
  (is (= [[1]] (sut/rotate [[1]])))
  (is (= '((1 4 7 10 13)
           (2 5 8 11 14)
           (3 6 9 12 15))
         (sut/rotate [[1 2 3]
                      [4 5 6]
                      [7 8 9]
                      [10 11 12]
                      [13 14 15]])))
  ;; simply demonstrate behavior for unequal length collections
  (is (= [[1 3 6] [2 4 7]]
         (sut/rotate [[1 2]
                      [3 4 5]
                      [6 7 8]])))
  (is (= [[1 4 6] [2 5 7]]
         (sut/rotate [[1 2 3]
                      [4 5]
                      [6 7 8]])))
  (is (= [[1 4 7] [2 5 8]]
         (sut/rotate [[1 2 3]
                      [4 5 6]
                      [7 8]]))))
