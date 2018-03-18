(ns serum.data
  (:require [clojure.walk :refer [postwalk]]
            [clojure.string :refer [lower-case]]
            [camel-snake-kebab.core :refer [->kebab-case
                                            ->HTTP-Header-Case
                                            ->camelCase
                                            ->PascalCase
                                            ->snake_case
                                            ->SCREAMING_SNAKE_CASE]]))

(defn index-unique-maps
  "derives a map from a mapseq (sequence of maps).
  utilizes an index specified by keypath, 'ks', as keys for the resulting map
  assumes that each indexed map value is unique.
  when duplicate keys are present, last duplicate keyed map will be present in result
  an optional 'key-proc' parameter is provide to allow key processing of each indexing key
  (such as forcing the case of each index via clojure.string/lower-case, for example)"
  [ms ks & {:keys [key-proc]
            :or {key-proc identity}}]
  (reduce
   (fn [acc m]
     (if-let [k (-> (get-in m ks)
                    key-proc)]
       (assoc acc k m)
       acc))
   {}
   ms))

(defn index-maps
  "derives a map from a mapseq (sequence of maps).
  utilizes an index specified by keypath, 'ks', as keys for the resulting map
  indexed maps need not be unique.  returned maps are grouped in a list for each unique index.
  an optional 'key-proc' parameter is provide to allow key processing of each indexing key
  (such as forcing the case of each index via clojure.string/lower-case, for example)"
  [ms ks & {:keys [key-proc]
            :or {key-proc identity}}]
  (reduce
   (fn [acc m]
     (if-let [k (-> (get-in m ks)
                    key-proc)]
       (update acc k conj m)
       acc))
   {}
   ms))

(defn proc-map
  "deep recursive walk of 'form' via postwalk.
  each mapentry within 'form' will be processed via the function 'f'"
  [f form]
  (postwalk
   (fn [cur]
     (if (map? cur)
       (into {} (map f cur))
       cur))
   form))

(defn proc-keys
  "deep recursive walk of 'form' via postwalk.  applies function, 'f' to hashmap keys nested within 'form'
  'form' - input data structure, presumably containing one or more hashmaps
  'f' - a function of one variable, 'k', corresponding to the currently hashmap key"
  [f form]
  (proc-map
   (fn [[k v]] [(f k) v])
   form))

(defn proc-vals
  "deep recursive walk of 'form' via postwalk.  applies function, 'f' to hashmap values nested within 'form'
  'form' - input data structure, presumably containing one or more hashmaps
  'f' - a function of one variable, 'v', corresponding to the current hashmap value"
  [f form]
  (proc-map
   (fn [[k v]] [k (f v)])
   form))

(defn proc-top-keys
  "shallow hashmap key processor.  applies function, 'f' to hashmap keys  within 'form'
  'form' - input hashmap
  'f' - a function of one variable, 'k', corresponding to the current hashmap key"
  [f form]
  (into
   {}
   (for [[k v] form]
     [(f k) v])))

(defn proc-top-vals
  "shallow hashmap key processor.  applies function, 'f' to hashmap values  within 'form'
  'form' - input hashmap
  'f' - a function of one variable, 'v', corresponding to the current hashmap value"
  [f form]
  (into
   {}
   (for [[k v] form]
     [k (f v)])))

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

(defn proc-ns-key
  "use a string processing function, f, to process a key, k
  also preserves namespace if the k is already a keyword
  result will be a keyword"
  [f k]
  (if (keyword? k)
    (let [ns (namespace k)
          nm (name k)]
      (keyword ns (f nm)))
    (keyword (f k))))

(defn keys->kebabs
  [form]
  (proc-keys (partial proc-ns-key ->kebab-case) form))

(defn keys->HTTP-Header-Case
  [form]
  (proc-keys (partial proc-ns-key ->HTTP-Header-Case) form))

(defn keys->camelCase
  [form]
  (proc-keys (partial proc-ns-key ->camelCase) form))

(defn keys->PascalCase
  [form]
  (proc-keys (partial proc-ns-key ->PascalCase) form))

(defn keys->snake_case
  [form]
  (proc-keys (partial proc-ns-key ->snake_case) form))

(defn keys->SCREAMING_SNAKE_CASE
  [form]
  (proc-keys (partial proc-ns-key ->SCREAMING_SNAKE_CASE) form))

(defn keys->strings
  [form]
  (proc-keys name form))

(defn keys->keywords
  [form]
  (proc-keys keyword form))

;; aliases
(defn keys->kebab-case
  [form]
  (keys->kebabs form))

(defn keys->spinal-case
  [form]
  (keys->kebabs form))
