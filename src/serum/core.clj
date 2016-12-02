(ns serum.core)

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
  "Derived from Scheme's and-let, this macro will evaluate 'expr' should all of its bindings evaluate truthy
  Evaluates 'else-expr' if any of the bindings evaluate falsey.  Returns nil if 'else-expr' is not provided.
  Evaluation of bindings ceases upon a falsey result
  'bindings' expects a binding-forms vector as in (let) and (loop) etc.
  'expr' expression to be evaluated if bindings evaluate truthy
  'else-expr' expression to be evaluated if any bindings evaluate falsey"
  ([bindings expr] `(and-let-core ~bindings ~expr))
  ([bindings expr else-expr] `(and-let-else-core ~bindings ~expr ~else-expr)))
