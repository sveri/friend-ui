(ns friendui.models.usertest
  (:require [datomic.api :as d]
            [de.sveri.friendui.models.api.user-api :as user-api]
            [de.sveri.friendui.models.user :as user]
            [de.sveri.friendui.models.db :as db]
            [de.sveri.friendui.service.user :as user-service]
            [cemerick.friend.credentials :as creds]
            [clojure.test :refer [deftest is testing]]
            [clojure.set :as set]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [de.sveri.clojure.commons.tests.util :as commons]
            [de.sveri.friendui.globals :as glob]))

(def gen-quantity 100)
(def gen-quantity-db 5)

(def default-role :free)

(def schema-tx (read-string (slurp "resources/schema/datomic-schema.edn")))

(def uri "datomic:mem://friendui")

(d/create-database uri)

(def conn-datomic (d/connect uri))

@(d/transact conn-datomic schema-tx)

(def base-db (d/db conn-datomic))

(defn catcher [f]
  (try (f)
       (catch Throwable t t)))

(deftest user-can-only-be-inserted-once
  (let [email "sv@sv.de" pw "sv"
        user (user-api/create-user-map email pw default-role)
        _ (:db-after (db/insert-entity conn-datomic (db/create-entity user)))]
      (is (thrown? java.lang.AssertionError (user-api/insert-user conn-datomic email (db/create-entity user))))))

(defspec insert-one-user-should-let-me-retrieve-that-one-user
         gen-quantity-db
         (prop/for-all [email commons/email-gen pw gen/string]
                       (let [user (user-api/create-user-map email pw default-role)
                              db_temp (:db-after (d/with base-db (db/create-entity user)))
                              found-vec (db/find-all-from-column db_temp db/find-by-username-query)
                              found-entity (db/get-entity-from-double-vec db_temp found-vec)]
                         (and (= (db/username-kw found-entity) email)
                              (= (db/activated-kw found-entity) false)
                              (= (db/role-kw found-entity) default-role)
                              (= (count found-vec) 1)
                              (= (not-empty (db/pw-kw found-entity)))
                              (= (user-api/username-exists? db_temp email))
                              (= (user-api/username-exists? db_temp (str "foo" email)) false)))))

(defspec generate-correct-activation-links
         gen-quantity
         (prop/for-all [activationid (gen/not-empty gen/string-alpha-numeric)]
                       (= (user-service/generate-activation-link activationid)
                          (str db/hostname "user/activate/" activationid))))

(defspec activate-user
         gen-quantity-db
         (prop/for-all [email commons/email-gen pw gen/string]
                       (let [user-map (user-api/create-user-map email pw default-role)
                             _ (:db-after (user-api/insert-user conn-datomic email (db/create-entity user-map)))
                             db-temp (:db-after (user/set-user-activated conn-datomic email))
                             user (user-api/get-user-by-username db-temp email)]
                         (and (= (db/username-kw user) email)
                              (= (db/activated-kw user) true)))))

(defspec get-username-should-respect-pw-parameter
         gen-quantity-db
         (prop/for-all [email commons/email-gen pw gen/string]
                       (let [user-map (user-api/create-user-map email pw default-role)
                             db-temp (:db-after (user-api/insert-user conn-datomic email (db/create-entity user-map)))
                             user-wo-pw (user-api/get-user-by-username db-temp email)
                             user-with-pw (user-api/get-user-by-username db-temp email true)]
                         (and (= (get user-wo-pw :password) nil)
                              (not-empty (get user-with-pw :password))))))

(defspec retrieve-roles-for-a-user
         gen-quantity-db
         (prop/for-all [email commons/email-gen pw gen/string]
                       (let [user-map (user-api/create-user-map email pw default-role)
                             db-temp (:db-after (user-api/insert-user conn-datomic email (db/create-entity user-map)))
                             user (user-api/get-user-by-username db-temp email)]
                         (= (get user db/role-kw) #{default-role}))))

;(fact "user update with a supplied map"
;      (let [email "sv@sv.de" pw "sv" role default-role jirauser "jirauser" jirapw "jirapw" jiraurl "url"
;            old-user (user-api/create-user email pw role)
;            testmap {:user/jirausername jirauser :user-api/jirapassword jirapw :user-api/jiraurl jiraurl}
;            _ (user-api/update-user email testmap)
;            updated-user (user-api/get-user-by-username email)
;            ]
;        (set/subset? (set testmap) (set updated-user)) => true
;        ))
