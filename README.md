# serum

Clojure library of utility functions and macros

## Artifact

via Clojars:
`[waffletower/serum "0.3.0"]`

## Macros

#### and-let

Compare to: https://github.com/egamble/let-else

The `and-let` macro is quite similar to the Scheme `and-let*` special
form.  Being honest about my own developer history, I wanted
short-circuiting behavior for bindings in Clojure somewhat analagous
to _optional chaining_
(https://en.wikipedia.org/wiki/Safe_navigation_operator) in
object-oriented languages like Swift and Objective-C.  Unlike
`and-let*` in Scheme, I also wanted to support else conditions.  Evan
Gamble's richly featured `let-else` provides the facility for _else_
handling for each individual binding.  I wanted a simple blanket
_else_ behavior instead.

``` Clojure
(and-let [a (its)
          b (not*)
          c (my)
          d (disco)]
         (fire-dj)
         (dance))
```

Evaluation of bindings stops should a binding evaluate
falsey.  In the above example, `(my)` and `(disco)` will not be
evaluated when `(not*)` evaluates falsey.

## Helpers

#### shift

The `shift` function is similar to `clojure.core/partial`. `shift`
accepts a function `f` and returns a function of one argument mapped
to the first argument of the function `f`:

``` Clojure
(map (comp (shift select-keys [:name :hat :shoe-size])
    (partial filter friend?) my-people)
```

## Tests

`lein midje`

## License

Copyright © 2017-2018 Christopher Penrose

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
