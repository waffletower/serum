(ns serum.data
  (:require
   [camel-snake-kebab.core :refer [->kebab-case
                                   ->HTTP-Header-Case
                                   ->camelCase
                                   ->PascalCase
                                   ->snake_case
                                   ->SCREAMING_SNAKE_CASE]]
   [clojure.walk :refer [postwalk]]
   [serum.string :refer [casefree]]))


(defn index-unique-maps
  "derives a map from a mapseq (sequence of maps).
  utilizes an index specified by key, `k`, as keys for the resulting map.
  assumes that each indexed map value is unique.
  when duplicate keys are present, last duplicate keyed map will be present in result.
  the optional `key-proc` parameter allows key processing of each indexing key
  (such as forcing the case of each index via clojure.string/lower-case, for example)."
  ([k ms] (index-unique-maps identity k ms))
  ([key-proc k ms]
   (reduce
     (fn [acc m]
       (if-let [pk (-> (get m k)
                       key-proc)]
         (assoc acc pk m)
         acc))
     {}
     ms)))


(defn index-maps
  "derives a map from a mapseq (sequence of maps).
  utilizes an index specified by key, `k`, as keys for the resulting map.
  indexed maps need not be unique.  returned maps are grouped in a list for each unique index.
  an optional `key-proc` parameter is provide to allow key processing of each indexing key
  (such as forcing the case of each index via clojure.string/lower-case, for example)."
  ([k ms] (index-maps identity k ms))
  ([key-proc k ms]
   (reduce
     (fn [acc m]
       (if-let [pk (-> (get m k)
                       key-proc)]
         (update acc pk conj m)
         acc))
     {}
     ms)))


(defn sorted-map-desc
  "create a sorted-map sorting by descending value from an pre-existing map `m`."
  [m]
  (apply
    sorted-map-by
    (fn [k1 k2]
      (compare [(get m k2) k2]
               [(get m k1) k1]))
    (apply concat m)))


(defn sorted-map-asc
  "create a sorted-map sorting by ascending value from a pre-existing map `m`."
  [m]
  (apply
    sorted-map-by
    (fn [k1 k2]
      (compare [(get m k1) k1]
               [(get m k2) k2]))
    (apply concat m)))


(defn proc-map
  "deep recursive walk of `form` via postwalk.
  each mapentry within `form` will be processed via the function `f`."
  [f form]
  (postwalk
    (fn [cur]
      (if (map? cur)
        (into {} (map f cur))
        cur))
    form))


(defn proc-keys
  "deep recursive walk of `form` via postwalk.  applies function, `f` to hashmap keys nested within `form`.
  `form` - input data structure, presumably containing one or more hashmaps
  `f` - a function of one variable, `k`, corresponding to the current hashmap key"
  [f form]
  (proc-map
    (fn [[k v]] [(f k) v])
    form))


(defn proc-vals
  "deep recursive walk of `form` via postwalk.  applies function, `f` to hashmap values nested within `form`.
  `form` - input data structure, presumably containing one or more hashmaps
  `f` - a function of one variable, `v`, corresponding to the current hashmap value"
  [f form]
  (proc-map
    (fn [[k v]] [k (f v)])
    form))


(defn proc-select-vals
  "deep recursive walk of `form` via postwalk.  applies function, `f` to hashmap values nested within `form`.
  `form` - input data structure, presumably containing one or more hashmaps
  `f` - a function of two variables, `k` and `v`, corresponding to the individual values of the current map entry being processed"
  [f form]
  (proc-map
    (fn [[k v]]
      [k (f k v)])
    form))


(defn- select-key-proc
  [f ck]
  (fn [k v]
    (if (= k ck)
      (f v)
      v)))


(defn- select-key-fn-proc
  [m]
  (fn [k v]
    (if-let [f (get m k)]
      (f v)
      v)))


(defn proc-val
  "deep recursive walk of `form`.
  applies function, `f` to process any values for the key `k` that are nested within `form`.
  `form` - input data structure, presumably containing one or more hashmaps
  `f` - a function of one variable, `v`, corresponding to the current hashmap value"
  [f form k]
  (proc-select-vals (select-key-proc f k) form))


(defn proc-with-map
  "deep recursive walk of `form`.
  processes map entries by applying a function indexed by key from the given `key-fn-map`
  to the current map entry value.
  `key-fn-map` should be a hashmap which pairs keys and processing functions.
  `form` - input data structure, presumably containing one or more hashmaps"
  [key-fn-map form]
  (proc-select-vals (select-key-fn-proc key-fn-map) form))


(defn proc-top-keys
  "shallow hashmap key processor.  applies function, 'f' to hashmap keys within 'form'.
  'form' - input hashmap
  'f' - a function of one variable, 'k', corresponding to the current hashmap key"
  [f form]
  (into
    {}
    (for [[k v] form]
      [(f k) v])))


(defn proc-top-vals
  "shallow hashmap key processor.  applies function, 'f' to hashmap values within 'form'.
  'form' - input hashmap
  'f' - a function of one variable, 'v', corresponding to the current hashmap value"
  [f form]
  (into
    {}
    (for [[k v] form]
      [k (f v)])))


(defn remap-keys
  "similar to select-keys, remap-keys requires a hashmap, key-map, instead of a key-seq.
   The keys of keymap correspond to keys in 'm', while the values
   of keymap correspond to keys in the resulting hashmap."
  [m key-map]
  (reduce
    (fn [acc [k v]]
      (if (contains? key-map k)
        (assoc acc (get key-map k) v)
        acc))
    {}
    m))


(defn proc-ns-key
  "use a string processing function, 'f', to process a key, 'k'.
  also preserves namespace if the k is already a keyword.
  result will be a keyword."
  [f k]
  (if (keyword? k)
    (let [ns (namespace k)
          nm (name k)]
      (keyword ns (f nm)))
    (keyword (f k))))


(defn keys->kebabs
  "deep recursive walk of 'form' via postwalk.
  converts all hashmap keys into kebab-case style keywords.
  preserves any namespaces in existing keyword keys."
  [form]
  (proc-keys (partial proc-ns-key ->kebab-case) form))


(defn keys->HTTP-Header-Case
  "deep recursive walk of 'form' via postwalk.
  converts all hashmap keys into HTTP-Header-Case style keywords.
  preserves any namespaces in existing keyword keys."
  [form]
  (proc-keys (partial proc-ns-key ->HTTP-Header-Case) form))


(defn keys->camelCase
  "deep recursive walk of 'form' via postwalk.
  converts all hashmap keys into camelCase style keywords.
  preserves any namespaces in existing keyword keys."
  [form]
  (proc-keys (partial proc-ns-key ->camelCase) form))


(defn keys->PascalCase
  "deep recursive walk of 'form' via postwalk.
  converts all hashmap keys into PascalCase style keywords.
  preserves any namespaces in existing keyword keys."
  [form]
  (proc-keys (partial proc-ns-key ->PascalCase) form))


(defn keys->snake_case
  "deep recursive walk of 'form' via postwalk.
  converts all hashmap keys into snake_case style keywords.
  preserves any namespaces in existing keyword keys."
  [form]
  (proc-keys (partial proc-ns-key ->snake_case) form))


(defn keys->SCREAMING_SNAKE_CASE
  "deep recursive walk of 'form' via postwalk.
  converts all hashmap keys into SCREAMING_SNAKE_CASE style keywords.
  preserves any namespaces in existing keyword keys."
  [form]
  (proc-keys (partial proc-ns-key ->SCREAMING_SNAKE_CASE) form))


(defn keys->strings
  "deep recursive walk of 'form' via postwalk.
  converts all hashmap keys into strings.
  pre-existing namespaces in keyword keys will be lost."
  [form]
  (proc-keys name form))


(defn keys->keywords
  "deep recursive walk of 'form' via postwalk.
  converts all hashmap keys into keywords.
  preserves any namespaces in existing keyword keys."
  [form]
  (proc-keys (partial proc-ns-key identity) form))

;; aliases
(defn ^{:doc (:doc (meta #'keys->kebabs))}
  keys->kebab-case
  [form]
  (keys->kebabs form))


(defn ^{:doc (:doc (meta #'keys->kebabs))}
  keys->spinal-case
  [form]
  (keys->kebabs form))

;; collection processing
(defn rotate
  "\"rotates\" a multi-dimensional collection (lazily).
  stable behavior for equal-length collections."
  [coll]
  (if (empty? coll)
    coll
    (apply map list coll)))
