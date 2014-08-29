(defproject de.sveri/friendui "0.4.0"
  :description "A frontend for friend library using enlive."
  :url "https://github.com/sveri/friend-ui/"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/core.cache "0.6.3"]
                 [com.cemerick/friend "0.2.1"]
                 [clojurewerkz/mailer "1.0.0"]
                 [com.taoensso/timbre "3.2.1"]
                 [compojure "1.1.8"]
                 [lib-noir "0.8.3"]
                 [enlive "1.1.5"]
                 [de.sveri/clojure-commons "0.1.3"]]
  :source-paths ["src/clj"]
  :test-paths ["test" "test/resources"]
  :deploy-repositories [["clojars-self" {:url           "https://clojars.org/repo"
                                         :sign-releases false}]])
