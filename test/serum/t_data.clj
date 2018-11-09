(ns serum.t-data
  (:require [serum.data :refer :all]
            [midje.sweet :refer :all]
            [clojure.string :refer [lower-case upper-case]]))

(fact "index-unique-maps"
  (index-unique-maps
   :duck
   [{:duckling "mallard"} {:duckling "mighty"}]) => {}
  (index-unique-maps
   :part
   [{:part "SOLENOID"}]) => {"SOLENOID" {:part "SOLENOID"}}
  (index-unique-maps
   lower-case
   :part
   [{:part "HEAD GASKET"}]) => {"head gasket" {:part "HEAD GASKET"}}
  (index-unique-maps
   :sauce
   [{:sauce "habenero" :scovilles :moderate}
    {:sauce "ghost" :scovilles :high}
    {:sauce "pequena" :scovilles :high}
    {:sauce "pequena" :scovilles :moderate}]) => {"habenero" {:sauce "habenero" :scovilles :moderate}
               "ghost" {:sauce "ghost" :scovilles :high}
               "pequena" {:sauce "pequena" :scovilles :moderate}})

(facts "index-maps"
  (index-maps
   :part
   [{:part "SOLENOID"}]) => {"SOLENOID" [{:part "SOLENOID"}]}
  (index-maps
   lower-case
   :part
   [{:part "HEAD GASKET"}]) => {"head gasket" [{:part "HEAD GASKET"}]}
  (index-maps
   lower-case
   :part
   [{:part "dirty trogg cloth"
     :model 1}
    {:part "Dirty Trogg Cloth"
     :model 2}]) => {"dirty trogg cloth" [{:part "Dirty Trogg Cloth"
                                                   :model 2}
                                                  {:part "dirty trogg cloth"
                                                   :model 1}]}
  (index-maps
   :sauce
   [{:sauce "habenero" :scovilles :moderate}
    {:sauce "ghost" :scovilles :high}
    {:sauce "pequena" :scovilles :high}
    {:sauce "pequena" :scovilles :moderate}]) => {"habenero" [{:sauce "habenero" :scovilles :moderate}]
                                                  "ghost" [{:sauce "ghost" :scovilles :high}]
                                                  "pequena" [{:sauce "pequena" :scovilles :moderate}
                                                             {:sauce "pequena" :scovilles :high}]})

(fact "proc-keys"
  (proc-keys
   upper-case
   {"hall" {"politicians" {"hankerchiefs" 100}}}) => {"HALL" {"POLITICIANS" {"HANKERCHIEFS" 100}}}
  (proc-keys
   upper-case
   {"tents" [{"color" "green"
              "size" "matchbook"}
             {"color" "fuschia"
              "size" "micron"}]}) => {"TENTS" [{"COLOR" "green"
                                       "SIZE" "matchbook"}
                                      {"COLOR" "fuschia"
                                       "SIZE" "micron"}]})

(fact "proc-vals"
  (proc-vals
   (fn [x] (if (number? x)
             (inc x)
             x))
   {:birds 17
    :types {:swans {:total 3 :kids 2}
            :geese {:total 5 :kids 3}
            :ducks {:total 9 :kids 5}}})  => {:birds 18
                                              :types {:swans {:total 4 :kids 3}
                                                      :geese {:total 6 :kids 4}
                                                      :ducks {:total 10 :kids 6}}})
(fact "proc-val"
  (proc-val upper-case
            {:candy "lik-m-aid"
             :drink "icee"
             :store {:name "dunagrees"
                     :candy "freshen up"}}
            :candy) => {:candy "LIK-M-AID"
            :drink "icee"
            :store {:name "dunagrees"
                    :candy "FRESHEN UP"}}
  (proc-val upper-case
            {:inventory {:animal "orca"
                         :paint "rodda"
                         :cutlery "fork"}}
            :cutlery) => {:inventory {:animal "orca"
                                      :paint "rodda"
                                      :cutlery "FORK"}})
(fact "proc-with-map"
  (let [key-fn-map {:theremin inc
                    :ondes-martenot dec}]
    (proc-with-map key-fn-map
                   {:theremin 1
                    :ondes-martenot 1}) => {:theremin 2
                                            :ondes-martenot 0}
    (proc-with-map key-fn-map
                   [{:basement [{:theremin 1
                                 :ondes-martenot 1}
                                {:echoplex 1}]}]) => [{:basement [{:theremin 2
                                                                   :ondes-martenot 0}
                                                                  {:echoplex 1}]}]))
(fact "proc-top-keys"
  (proc-top-keys
   upper-case
   {"hall" {"politicians" {"hankerchiefs" 100}}}) => {"HALL" {"politicians" {"hankerchiefs" 100}}})

(fact "proc-top-vals"
  (proc-top-vals
   (fn [x] (if (number? x)
             (inc x)
             x))
   {:birds 17
    :types {:swans {:total 3 :kids 2}
            :geese {:total 5 :kids 3}
            :ducks {:total 9 :kids 5}}})  => {:birds 18
                                              :types {:swans {:total 3 :kids 2}
                                                      :geese {:total 5 :kids 3}
                                                      :ducks {:total 9 :kids 5}}})

(fact "remap-keys"
  (remap-keys
   {:prime-minister "shambolic"}
   {:prime-minister :president}) => {:president "shambolic"}
  (remap-keys
   {:prime-minister "shambolic"}
   {:dentine :trident}) => {}
  (remap-keys
   {:pre-cambrian "devo"
    :psionic-fiend "mind-flayer"
    :garfunkle-says "sound-of-silence"}
   {:pre-cambrian :post-kardashian
    :garfunkle-says :single-payer}) => {:post-kardashian "devo"
                                        :single-payer "sound-of-silence"}
  (remap-keys
   {:blodgett "staff-of-life"}
   {}) => {}
  (remap-keys
   {:blodgett "staff-of-life"}
   nil) => {}
  (remap-keys
   nil
   {:your-comfort "second-concern"}) => {}
  (remap-keys
   {}
   {}) => {}
  (remap-keys
   {}
   nil) => {}
  (remap-keys
   nil
   {}) => {}
  (remap-keys
   nil
   nil) => {})

(facts "camelCase, kebab-case, snake_case, PascalCase conversion"
  (let [kebab {:dental-floss {:proctor-gamble false :ashley-madison true :sample-hold {:meal-worm true}}}
        header {:Dental-Floss {:Proctor-Gamble false :Ashley-Madison true :Sample-Hold {:Meal-Worm true}}}
        camel {:dentalFloss {:proctorGamble false :ashleyMadison true :sampleHold {:mealWorm true}}}
        pascal {:DentalFloss {:ProctorGamble false :AshleyMadison true :SampleHold {:MealWorm true}}}
        snake {:dental_floss {:proctor_gamble false :ashley_madison true :sample_hold {:meal_worm true}}}
        scream {:DENTAL_FLOSS {:PROCTOR_GAMBLE false :ASHLEY_MADISON true :SAMPLE_HOLD {:MEAL_WORM true}}}]

    (fact "keys->kebabs"
      (keys->kebabs header) => kebab
      (keys->kebabs camel) => kebab
      (keys->kebabs pascal) => kebab
      (keys->kebabs snake) => kebab
      (keys->kebabs scream) => kebab)

    (fact "keys->HTTP-Header-Case"
      (keys->HTTP-Header-Case kebab) => header
      (keys->HTTP-Header-Case camel) => header
      (keys->HTTP-Header-Case pascal) => header
      (keys->HTTP-Header-Case snake) => header
      (keys->HTTP-Header-Case scream) => header)

    (fact "keys->camelCase"
      (keys->camelCase kebab) => camel
      (keys->camelCase header) => camel
      (keys->camelCase pascal) => camel
      (keys->camelCase snake) => camel
      (keys->camelCase scream) => camel)

    (fact "keys->PascalCase"
      (keys->PascalCase kebab) => pascal
      (keys->PascalCase header) => pascal
      (keys->PascalCase camel) => pascal
      (keys->PascalCase snake) => pascal
      (keys->PascalCase scream) => pascal)

    (fact "keys->snake_case"
      (keys->snake_case kebab) => snake
      (keys->snake_case header) => snake
      (keys->snake_case camel) => snake
      (keys->snake_case pascal) => snake
      (keys->snake_case scream) => snake)

    (fact "keys->SCREAMING_SNAKE_CASE"
      (keys->SCREAMING_SNAKE_CASE kebab) => scream
      (keys->SCREAMING_SNAKE_CASE header) => scream
      (keys->SCREAMING_SNAKE_CASE camel) => scream
      (keys->SCREAMING_SNAKE_CASE pascal) => scream
      (keys->SCREAMING_SNAKE_CASE snake) => scream)))

(fact "keys->strings and keys->keywords"
  (let [stringy {"motto" "thank heaven" "conbini" "7/11"}
        keyly {:motto "thank heaven" :conbini "7/11"}]
    (-> stringy
        keys->keywords
        keys->strings
        keys->keywords) => keyly))

(fact "keyword namespace preservation tests"
  (let [k :there.is.water/flowing-down
        m {k true}]
    (proc-ns-key identity k) => k
    (keys->kebabs m) => m
    (keys->keywords m) => m
    (keys->HTTP-Header-Case m) => {:there.is.water/Flowing-Down true}
    (keys->camelCase m) => {:there.is.water/flowingDown true}
    (keys->PascalCase m) => {:there.is.water/FlowingDown true}
    (keys->snake_case m) => {:there.is.water/flowing_down true}
    (keys->SCREAMING_SNAKE_CASE m) => {:there.is.water/FLOWING_DOWN true}))

(fact "rotate"
  (rotate nil) => nil
  (rotate []) = []
  (rotate [[]]) => []
  (rotate [[1]]) => [[1]]
  (rotate [[1 2 3]
           [4 5 6]
           [7 8 9]
           [10 11 12]
           [13 14 15]]) => '((1 4 7 10 13)
                             (2 5 8 11 14)
                             (3 6 9 12 15))
  ;; simply demonstrate behavior for unequal length collections
  (rotate [[1 2]
           [3 4 5]
           [6 7 8]]) => [[1 3 6] [2 4 7]]
  (rotate [[1 2 3]
           [4 5]
           [6 7 8]]) => [[1 4 6] [2 5 7]]
  (rotate [[1 2 3]
           [4 5 6]
           [7 8]]) => [[1 4 7] [2 5 8]])
