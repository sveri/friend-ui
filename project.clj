(defproject de.sveri/friendui "0.2.3"
  :description "A frontend for friend library using selmer and storing data in datomic"
  :url "https://github.com/sveri/friend-ui/"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.datomic/datomic-pro "0.9.4556"]
                 [midje "1.6.3" :scope "test"]
                 [org.clojure/core.cache "0.6.3"]
                 [com.cemerick/friend "0.2.1"]
                 [clojurewerkz/mailer "1.0.0"]
                 [com.taoensso/timbre "3.2.1"]
                 [compojure "1.1.8"]
                 [lib-noir "0.8.3"]
                 [enlive "1.1.5"]
                 [org.clojure/clojurescript "0.0-2227"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [fogus/ring-edn "0.2.0"]
                 [om "0.6.4"]
                 [om-sync "0.1.1"]
                 ;[kioo "0.4.0"]
                 ]
  :cljsbuild {:builds
               [{:id           "dev"
                 :source-paths ["src/cljs"]
                 :compiler     {
                                 :output-to     "resources/public/js/friendui/friendui.js"
                                 :output-dir    "resources/public/js/friendui/out"
                                 :optimizations :none
                                 :source-map    "resources/public/js/friendui/friendui.js.map"}}]}
  :source-paths ["src/clj"]
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-midje "3.1.1"]]
  :test-paths ["test" "test/resources"]
  :deploy-repositories [["clojars-self" {:url           "https://clojars.org/repo"
                                         :sign-releases false}]]
  )
