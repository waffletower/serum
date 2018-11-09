(ns serum.core
  (:require [slingshot.slingshot :refer [throw+]]))

(defmacro and-let-core [bindings expr]
  (if (seq bindings)
    `(if-let [~(first bindings) ~(second bindings)]
       (and-let-core ~(drop 2 bindings) ~expr))
    expr))

(defmacro and-let-else-core [bindings expr else-expr]
  (if (seq bindings)
    `(if-let
         [~(first bindings) ~(second bindings)]
       (and-let-else-core ~(drop 2 bindings) ~expr ~else-expr)
       ~else-expr)
    expr))

;; use nested macro for now until I can figure out multi-arity macro/function interaction here
;; small divergence from Scheme's and-let as it supports an else expression
(defmacro and-let
  "Derived from Scheme's and-let, this macro will evaluate `expr` should all of its bindings evaluate truthy.
  Evaluates `else-expr` if any of the bindings evaluate falsey or returns nil if `else-expr` is not provided.
  Evaluation of `bindings` ceases upon a falsey result.
  `bindings` expects a binding-forms vector as in (let) and (loop) etc.
  `expr` expression to be evaluated if bindings evaluate truthy
  `else-expr` expression to be evaluated if any bindings evaluate falsey"
  ([bindings expr] `(and-let-core ~bindings ~expr))
  ([bindings expr else-expr] `(and-let-else-core ~bindings ~expr ~else-expr)))

(defn shift
  "similar to partial, provides a modified interface to function `f`.
  returns a function which accepts a single argument, always passing it as the first argument of `f`.
  useful with (comp) and threading macros."
  ([f] f)
  ([f arg2] (fn [arg1] (f arg1 arg2)))
  ([f arg2 arg3] (fn [arg1] (f arg1 arg2 arg3)))
  ([f arg2 arg3 arg4 & args] (fn [arg1] (apply f arg1 arg2 arg3 arg4 args))))

(defmacro success-let
  "conditional let form which expects binding expression to return a hashmap with {:success boolean} content.
  `success-let` supports a single binding pair: `binding`.
  will evaluate `expr` if (:success y#) is truthy.
  will evaluate `else` if (:success y#) is falsey.
  passes binding symbol/value to both `expr` and `else-expr` expressions."
  ([binding expr]
   `(success-let ~binding ~expr nil))
  ([binding expr else-expr]
   (let [bsym (binding 0)
         bexpr (binding 1)]
     `(let [y# ~bexpr
            ~bsym y#]
        (when (not (contains? y# :success))
          (throw+ {:message "form evaluation result did not contain :success key"}))
        (if (:success y#) ~expr ~else-expr)))))

;; TODO improve to allow multiple forms as in when-let
(defmacro fail-let
  "conditional let form which expects binding expression to return a hashmap with {:success boolean} content.
  `fail-let` supports a single binding pair: `binding`.
  will evaluate 'expr' if (:success y#) is falsey.
  passes binding symbol/value to `expr`."
  [binding expr]
  (let [bsym (binding 0)
        bexpr (binding 1)]
    `(let [y# ~bexpr
           ~bsym y#]
       (when (not (contains? y# :success))
         (throw+ {:message "form evaluation result did not contain :success key"}))
       (when (not (:success y#)) ~expr))))

(defmacro try-true?
  "the expression `expr` is expected to be a validation form which returns booleanness.
  `expr` will be executed in a try/catch form and a boolean returned.
  caught exceptions will return false."
  [expr]
  `(try
     (if ~expr
       true
       false)
     (catch Exception e# false)))

(defmacro attempt
  "the expression `expr` will be executed in a try/catch form.
  if an exception is caught, a handler function of one argument, `f`, will be executed
  and will be passed the caught java exception object."
  [expr f]
  `(try
     ~expr
     (catch Exception e# (~f e#))))

(defmacro success->
  [x & forms]
  (if forms
    (let [form (first forms)
          cur (if (seq? form)
                `(~(first form) ~x ~@(next form))
                (list form x))]
      `(success-let
        [y# ~cur]
        (success-> y# ~@(next forms))
        y#))
    x))

(defmacro divert
  "derived from (with-out-str).  Evaluates body, captures any output destined to *out*, and returns result
  of `body` and text output as a vector tuple, [~@body captured-output-string]"
  [& body]
  `(let [s# (new java.io.StringWriter)]
     (binding [*out* s#]
       [~@body (str s#)])))

(defn find-pred
  "applies `pred` to each element of a collection, `coll`, until it returns a truthy value,
  then returns the matching element (*not* the individual value).  Compare with `clojure.core/some`"
  [pred coll]
  (reduce
   (fn [_ cur]
     (when (pred cur)
       (reduced cur)))
   nil
   coll))

(defn find-key-val
  "searches `mapseq` for an element with a matching key/value pair.
  The key is specified by `k`, and the value is specified by `v`."
  [k v mapseq]
  (find-pred
   (comp
    (shift = v)
    (shift get k))
   mapseq))

(defn within?
  "is \"x\" in \"coll\"?
  has signature and semantics of `contains?`.
  matches elements rather than indices of indexed collections.
  `coll` collection which is source of comparison.
  `x` target comparison value.
  `fun` comparison function (defaults to `=`).
  `within?` has arguably more consistent semantics for collections than `contains?`:
  `(contains? [4 5 6] 4) => falsey`
  `(contains? '(4 5 6) 4) => truthy`
  `(within? [4 5 6] 4) => truthy`
  `(within? '(4 5 6) 4) => truthy`"
  ([coll x] (within? coll = x))
  ([coll fun x]
   (some #(fun x %) coll)))

(defn wrap-within-fn
  "convenience function which returns a lambda intended for `within?` comparisons.
  `fun` to process all comparison arguments.
  `compare-fn` function to perform comparison"
  ([fun]
   (wrap-within-fn = fun))
  ([compare-fn fun]
   (fn [& args]
     (->> args
          (map fun)
          (apply compare-fn)))))
