# serum

Clojure library of utility functions and macros

## Usage

Leiningen:
`[waffletower/serum "0.1.0"]`

`(require [serum.core :refer :all])`

## Macros

#### and-let

Compare to https://clojars.org/egamble/let-else

The `and-let` macro is inspired by the Scheme `and-let*` function.
Being honest to my own developer history, I wanted short-circuiting
behavior for bindings in Clojure somewhat analagous to _optional
chaining_ (https://en.wikipedia.org/wiki/Safe_navigation_operator) in
Object-oriented languages like Swift and Objective-C.  Unlike Scheme I
also wanted to support else conditions.  Evan Gamble's `let-else`
provides the facility for _else_ handling for each individual binding.
I wanted a typical blanket _else_ behavior instead.

``` Clojure
(and-let [a (its)
          b (not*)
          c (my)
          d (disco)]
         (fire-dj)
         (dance))
```

## Tests

`lein midje`

## License

Copyright © 2016 Christopher Penrose

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
