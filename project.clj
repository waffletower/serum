(defproject waffletower/serum "0.8.1"
  :description "Clojure library of utility functions and macros"
  :url "https://github.com/waffletower/serum"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/core.async "1.5.648"]
                 [org.clojure/tools.logging "1.2.4"]
                 [camel-snake-kebab "0.4.3"]]
  :deploy-repositories [["clojars-https" {:url "https://clojars.org/repo"
                                          :username :env/clojars_user
                                          :password :env/clojars_password}]]
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.10.5"]]
                   :plugins [[lein-midje "3.2.1"]]}})
