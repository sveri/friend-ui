(ns friendui.models.usertest
  (:require [datomic.api :as d]
            [midje.sweet :refer :all]
            [friendui.models.user :as user]
            [friendui.models.db :refer :all]
            [friendui.service.user :refer :all]
            [cemerick.friend.credentials :as creds]
            ))

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
        user => {:username email :roles #{:free} :activated false}))
;
;(fact "insert one user should let me retrieve that one user"
;      (let [email "sv@sv.de" pw "sv" role :free
;            user (user/create-user email pw role)
;            found-entity (d/q '[:find ?c :where [?c :user/email]] (dbc))]
;        (count found-entity) => 1
;        (:user/email (get-entity-from-double-vec found-entity)) => email
;        (:user/role (get-entity-from-double-vec found-entity)) => role
;        (:user/activated (get-entity-from-double-vec found-entity)) => false
;        ))
;
;(fact "insert one user should let me retrieve that one user"
;      (let [email "sv@sv.de" pw "sv" role :free
;            user (user/create-user email pw role)
;            found-entity (d/q '[:find ?c :where [?c :user/email]] (dbc))]
;        (count found-entity) => 1
;        (:user/email (get-entity-from-double-vec found-entity)) => email
;        (:user/role (get-entity-from-double-vec found-entity)) => role
;        (:user/activated (get-entity-from-double-vec found-entity)) => false
;        ))
;
;
;(fact "get a list of usernames and password (also test if usernames are unique)"
;      (let [mails ["sv@sv.de" "bla@bla.de" "foo@bar.de" "msdf@mdf.com" "foo@bar.de"]]
;        (doall (for [mail mails] (user/create-user mail "pw" "role")))
;        (count (d/q '[:find ?c :where [?c :user/email]] (dbc))) => 4
;        (count (clojure.set/difference (set mails) (set (map first (user/get-user-password-role-map))))) => 0
;        (count (set (map second (user/get-user-password-role-map)))) => 4
;        (count (set (map #(nth % 2) (user/get-user-password-role-map)))) => 1
;        ))
;
;(fact "username exists"
;      (let [email "sv@sv.de" pw "sv" role "free"]
;        (user/username-exists email) => false
;        (user/create-user email pw role) => {:username email :roles #{:free} :activated false}
;        (user/username-exists email) => true
;        ))
;
;(fact "generate correct activation link"
;      (let [activationid (generate-activation-id)]
;        (generate-activation-link activationid) => (str "http://example.com/user/activate/" activationid )))
;
;(fact "activate user"
;      (let [email "sv@sv.de" pw "sv" role :free
;            user (user/create-user email pw role)]
;        (user/get-user-by-username email) => {:username email :roles #{role} :activated false}
;        (user/set-user-activated email)
;        (user/get-user-by-username email) => {:username email :roles #{role} :activated true}
;        ))
