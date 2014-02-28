(defproject friendui "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.datomic/datomic-pro "0.9.4556"]
                 [midje "1.6.2" :scope "test"]
                 [org.clojure/core.cache "0.6.3"]
                 [com.cemerick/friend "0.2.0"]
                 [clojurewerkz/mailer "1.0.0"]
                 [com.taoensso/timbre "3.0.0"]
                 [compojure "1.1.6"]
                 [selmer "0.5.9"]]
  :plugins [[lein-midje "3.1.1"]]
  :test-paths ["test" "test/resources"]
  )
