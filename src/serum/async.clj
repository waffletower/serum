(ns serum.async
  (:require
    [clojure.core.async :as a])
  (:import (java.util.concurrent Executors)))

(defmacro fire!
  "execute 'body' on an asynchronous worker thread.
  (fire! and forget)"
  [body]
  `(a/go
     (let [channel# (a/chan)]
       (a/>!
         channel#
         (do
           (try
             (~@body)
             (catch
               Throwable
               exception#
               (do
                 (println (str "fire! caught exception on worker thread: " (Thread/currentThread)))
                 (println exception#))))
           (a/close! channel#)
           true)))))

;; `throw-exceptions`, `<?` and `<??` adapted from:
;; http://martintrojer.github.io/clojure/2014/03/09/working-with-coreasync-exceptions-in-go-blocks

(defn throw-exceptions
  [x]
  (when (instance? Throwable x)
    (throw x))
  x)


(defmacro <?
  [channel]
  `(throw-exceptions (a/<! ~channel)))


(defmacro <??
  [channel]
  `(throw-exceptions (a/<!! ~channel)))


(defn chan->seq
  "recursive function which derives a lazy sequence from a core.async channel, `channel`.
  Intended for use on main thread."
  [channel]
  (lazy-seq
     ;; the reflection within `<??` shouldn't be too slow for many use cases
    (when-let [took (<?? channel)]
      (cons took (chan->seq channel)))))


(defn map-async
  "an asynchronous implementation of `map`
  returns a lazy sequence of the asynchronous evaluation of `f` applied to members of the collection `coll`
  will block until all asynchronous evaluation completes.
  Intended for use on main thread.
  `f` single-argument function
  `n` parallelization parameter
  `coll` collection applied to the function `f`"
  ([f coll]
   (map-async
     f
     (.. Runtime getRuntime availableProcessors)
     coll))
  ([f n coll]
   (let [input-channel (a/chan n)
         output-channel (a/chan n)]
     (a/onto-chan input-channel coll)
     (a/pipeline-blocking
       n
       output-channel
       (map
         (fn [x]
           (try
             (f x)
             (catch Throwable e
               (do
                 (a/close! input-channel)
                 e)))))
       input-channel)
     (chan->seq output-channel))))


;; TODO consider making a variant that externalizes the threadpool to attain full laziness.
(defn map-exec
  "an asynchronous implementation of `map`
  returns a vector of the asynchronous evaluation of `f` applied to members of the collection `coll`
  will block until all asynchronous evaluation completes.
  Intended for use on main thread. More efficient utilization of thread pool than `map-async`.
  Not fully lazy, however.
  `f` single-argument function
  `n` parallelization parameter
  `coll` collection applied to the function `f`"
  ([f coll]
   (map-exec
     f
     (.. Runtime getRuntime availableProcessors)
     coll))
  ([f n coll]
   (let [execs (Executors/newFixedThreadPool n)
         rs (->> coll
                 (map #(.submit execs (partial f %)))
                 (mapv deref))]
     (.shutdown execs)
     rs)))

;; TODO could create a version where order could be maintained by indexing and sorting results
(defn disorder-exec
  "asynchronously executes `f` upon members of `coll`.
  does not serially block on task (`java.util.concurrent.FutureTask`) de-referencing.
  only de-references tasks which are complete.
  order of output collection does not correspond to order of input.
  Imperative, not lazy.
  `f` single-argument function
  `opts` clojure hashmap containing the following key/value pairs:
    `:pool-count` thread pool thread count parameter
    `:poll-millis` main thread polling wait in milliseconds between completion checks (`.isDone`)
                   `nil` skips wait (with stack overflow risk) - `10 ms` default
  `coll` collection applied to the function `f`"

  ([f coll]
   (disorder-exec
     f
     {:pool-count (.. Runtime getRuntime availableProcessors)
      :poll-millis 10}
     coll))

  ([f opts coll]
   (let [{:keys [pool-count poll-millis]} opts
         execs (Executors/newFixedThreadPool pool-count)
         ts (map #(.submit execs (partial f %)) coll)
         outs (loop [curs ts
                     sts {:outs []}]
                (let [{:keys [tasks outs]} (reduce
                                             (fn [acc cur]
                                               (if (.isDone cur)
                                                 (update acc :outs conj @cur)
                                                 (update acc :tasks conj cur)))
                                             sts
                                             curs)]
                  (if (not-empty tasks)
                    (do
                      (when poll-millis
                        (Thread/sleep poll-millis))
                      (recur tasks {:outs outs}))
                    outs)))]
     (.shutdown execs)
     outs)))
