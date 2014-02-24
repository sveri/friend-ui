(ns friendui.models.usertest
  (:require [datomic.api :as d]
            [midje.sweet :refer :all]
            [friendui.models.user :as user]
            [friendui.models.db :refer :all]
            [cemerick.friend.credentials :as creds]))

(d/create-database uri-datomic)
(def schema-tx (read-string (slurp "resources/schema/datomic-schema.edn")))



(defn delete-and-create-db []
  (d/delete-database uri-datomic)
  (d/create-database uri-datomic)
  @(d/transact (conn-datomic) schema-tx))

(background (before :facts (delete-and-create-db) :after ()))

(fact "Connection to datomic succeded"
      (if (conn-datomic) :truthy :falsey) => :truthy)

(fact "insert one user should return the right map"
      (let [email "sv@sv.de" pw "sv" role "free"
            user (user/create-user email pw role)]
        (:username user) => email
        (:roles user) => #{:free}))

(fact "insert one user should let me retrieve that one user"
      (let [email "sv@sv.de" pw "sv" role "free"
            user (user/create-user email pw role)
            found-entity (d/q '[:find ?c :where [?c :user/email]] (dbc))]
        (count found-entity) => 1
        (:user/email (get-entity found-entity)) => email
        (:user/role (get-entity found-entity)) => role
        (:user/activated (get-entity found-entity)) => false
        (:user/password (get-entity found-entity)) => (creds/hash-bcrypt pw)
        ))

