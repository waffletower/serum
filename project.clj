(defproject waffletower/serum "0.7.0"
  :description "Clojure library of utility functions and macros"
  :url "https://github.com/waffletower/serum"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [camel-snake-kebab "0.4.0"]
                 [slingshot "0.12.2"]
                 [com.taoensso/timbre "4.10.0"]]
  :deploy-repositories [["clojars-https" {:url "https://clojars.org/repo"
                                          :username :env/clojars_user
                                          :password :env/clojars_password}]]
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.9.1"]]
                   :plugins [[lein-midje "3.2.1"]]}})
