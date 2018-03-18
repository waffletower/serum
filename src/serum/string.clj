(ns serum.string
  (:require [clojure.string :refer [lower-case]]))

(defn casefree
  "case insensitive comparison.
  (= nil nil) evaluates truthy"
  [s1 s2]
  (if (or
       (not (string? s1))
       (not (string? s2)))
    (= s1 s2)
    (= (lower-case s1)
       (lower-case s2))))
