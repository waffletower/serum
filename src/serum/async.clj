(ns serum.async
  (:require [clojure.core.async :as a]
            [taoensso.timbre :as log]))

(defmacro fire!
  "execute 'body' on an asynchronous worker thread via a core.async channel.
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
                (log/error "fire! caught exception on worker thread:")
                (log/error exception#))))
          (a/close! channel#)
          true)))))

(defn chan->seq
  "recursive function which derives a lazy sequence from a core.async channel, 'channel'"
  [channel]
  (lazy-seq
   (when-let [took (a/<!! channel)]
     (cons took (chan->seq channel)))))

(defn map-async
  "an asynchronous implementation of 'map'
  returns a lazy sequence of the asynchronous evaluation of 'f' applied to members of the collection 'coll'
  will block until all asynchronous evaluation completes
  'f' single-argument function
  'n' parallelization parameter
  'coll' collection applied to the function 'f'"
  ([f coll]
   (map-async
    f
    (.. Runtime getRuntime availableProcessors)
    coll))
  ([f n coll]
   (let [input-channel (a/chan n)
         output-channel (a/chan n)]
     (a/onto-chan input-channel coll)
     (a/pipeline-async
      n
      output-channel
      (fn [x c]
        (a/>!! c (f x))
        (a/close! c))
      input-channel)
     (chan->seq output-channel))))