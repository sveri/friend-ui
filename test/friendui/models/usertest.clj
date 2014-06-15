(ns friendui.models.usertest
  (:require [datomic.api :as d]
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
            [de.sveri.friendui.globals :as glob]
            ))

(def gen-quantity 100)
(def gen-quantity-db 10)

(def default-role :free)

(def schema-tx (read-string (slurp "resources/schema/datomic-schema.edn")))

(def uri "datomic:mem://friendui")

(d/create-database uri)

(def conn-datomic (d/connect uri))


@(d/transact conn-datomic schema-tx)

(def base-db (d/db conn-datomic))

;(background (before :facts (delete-and-create-db) :after ()))

;(println (user/username-exists (:db-after (d/with base-db (db/create-entity {db/username-kw "foo@foo.de"}))) "foo@foo.de"))
;(println (db/find-by-column-and-search-string (:db-after (d/with base-db (db/create-entity {db/username-kw "foo@foo.de"}))) db/username-kw "foo@foo.de"))

;(defn fresh-db-conn! []
;  (let [uri "datomic:mem://test"]
;    (d/delete-database uri)
;    (d/create-database uri)
;    (let [c (d/connect uri)]
;      (d/transact c (load-file "resources/schema/datomic-schema.edn"))
;      c)))

;(background (around :facts (with-redefs [myapp.db/conn (fresh-db-conn!)]
;(background (around :facts (with-redefs [de.sveri.friendui.models.db/conn-datomic (fresh-db-conn!)]
;                                        ?form)))



;(def email-or-nil (gen/one-of [email-gen (gen/return nil)]))



;(def insert-one-user-should-let-me-retrieve-that-one-user
;  (prop/for-all [email email-gen pw gen/string]
;                (let [user (user/create-user-map email pw default-role)
;                      db_temp (:db-after (d/with base-db (db/create-entity user)))
;                      found-vec (db/find-all-from-column db_temp db/username-kw)
;                      found-entity (db/get-entity-from-double-vec db_temp found-vec)]
;                  (and (= (db/username-kw found-entity) email)
;                       (= (db/activated-kw found-entity) false)
;                       (= (db/role-kw found-entity) default-role)
;                       (= (count found-vec) 1)
;                       (= (not-empty (db/pw-kw found-entity)))
;                       (= (user/username-exists db_temp email) true)
;                       (= (user/username-exists db_temp (str "foo" email)) false))))
;                )
;(def test-test
;  (prop/for-all [email email-gen pw gen/string]
;               (= email email)))
;
;(fact "get a list of usernames and password (also test if usernames are unique)"
;      (tc/quick-check 10 insert-one-user-should-let-me-retrieve-that-one-user) => true
;      )

;(deftest generated-instance-tests
;  (is (tc/quick-check 10 insert-one-user-should-let-me-retrieve-that-one-user))
;  (is (tc/quick-check 100 test-test))
;  )

;(deftest user-can-only-be-inserted-once
;  (let [email "sv@sv.de" pw "sv"
;        user (user/create-user-map email pw default-role)
;        _ (:db-after (db/insert-entity conn-datomic (db/create-entity user)))]
;      (is (thrown? java.lang.AssertionError (user/insert-user conn-datomic email (db/create-entity user))))))

;(defspec insert-one-user-should-let-me-retrieve-that-one-user
;         gen-quantity-db
;         (prop/for-all [email commons/email-gen pw gen/string]
;                       (let [user (user/create-user-map email pw default-role)
;                              db_temp (:db-after (d/with base-db (db/create-entity user)))
;                              found-vec (db/find-all-from-column db_temp db/find-by-username-query)
;                              found-entity (db/get-entity-from-double-vec db_temp found-vec)]
;                         (and (= (db/username-kw found-entity) email)
;                              (= (db/activated-kw found-entity) false)
;                              (= (db/role-kw found-entity) default-role)
;                              (= (count found-vec) 1)
;                              ;(is (thrown? java.lang.AssertionError (user/insert-user conn-datomic email (db/create-entity user))))
;                              (= (not-empty (db/pw-kw found-entity)))
;                              (= (user/username-exists db_temp email))
;                              (= (user/username-exists db_temp (str "foo" email)) false)))))
;(defspec generate-correct-activation-links
;         gen-quantity
;         (prop/for-all [activationid (gen/not-empty gen/string-alpha-numeric)]
;                       (= (user-service/generate-activation-link activationid)
;                          (str db/hostname "user/activate/" activationid))))
;
;(defspec activate-user
;         gen-quantity-db
;         (prop/for-all [email commons/email-gen pw gen/string]
;                       (let [user-map (user/create-user-map email pw default-role)
;                             _ (:db-after (user/insert-user conn-datomic email (db/create-entity user-map)))
;                             db-temp (:db-after (user/set-user-activated conn-datomic email))
;                             user (user/get-user-by-username db-temp email)]
;                         (and (= (db/username-kw user) email)
;                              (= (db/activated-kw user) true)))))

(defspec get-username-should-respect-pw-parameter
         gen-quantity-db
         (prop/for-all [email commons/email-gen pw gen/string]
                                              (let [user-map (user/create-user-map email pw default-role)
                                                    _ (:db-after (user/insert-user conn-datomic email (db/create-entity user-map)))
                                                    db-temp (:db-after (user/set-user-activated conn-datomic email))
                                                    user (user/get-user-by-username db-temp email)]
                                                (and (= (db/username-kw user) email)
                                                     (= (db/activated-kw user) true))))

;(fact "get-username-should-add-password"
;      (let [email "sv@sv.de" pw "sv" role default-role
;            user (user/create-user email pw role)
;            user-fetched (user/get-user-by-username email true)]
;        (:password user-fetched) => truthy
;        (:password (user/get-user-by-username email)) => falsey
;        ))
;
;(fact "retrieve roles for a user"
;      (let [email "sv@sv.de" pw "sv" role default-role
;            _ (user/create-user email pw role)
;            user (user/get-user-by-username email)]
;        (role-kw user) => #{default-role}
;        ))
;
;(fact "user update with a supplied map"
;      (let [email "sv@sv.de" pw "sv" role default-role jirauser "jirauser" jirapw "jirapw" jiraurl "url"
;            old-user (user/create-user email pw role)
;            testmap {:user/jirausername jirauser :user/jirapassword jirapw :user/jiraurl jiraurl}
;            _ (user/update-user email testmap)
;            updated-user (user/get-user-by-username email)
;            ]
;        (set/subset? (set testmap) (set updated-user)) => true
;        ))
