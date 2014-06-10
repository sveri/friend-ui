(ns friendui.models.usertest
  (:require [datomic.api :as d]
            [midje.sweet :refer :all]
            [de.sveri.friendui.models.user :as user]
            ;[de.sveri.friendui.models.db :refer :all]
            [de.sveri.friendui.service.user :refer :all]
            [cemerick.friend.credentials :as creds]
            [clojure.set :as set]
            ))

;(d/create-database uri-datomic)
;(def schema-tx (read-string (slurp "resources/schema/datomic-schema.edn")))
;
;(def default-role :free)
;
;(defn delete-and-create-db []
;  (d/delete-database uri-datomic)
;  (d/create-database uri-datomic)
;  @(d/transact (conn-datomic) schema-tx))
;
;(background (before :facts (delete-and-create-db) :after ()))

;(fact "Connection to datomic succeded"
;      (if (conn-datomic) :truthy :falsey) => :truthy)

(defn fresh-db-conn! []
  (let [uri "datomic:mem://test"]
    (d/delete-database uri)
    (d/create-database uri)
    (let [c (d/connect uri)]
      (d/transact c (load-file "resources/schema/datomic-schema.edn"))
      c)))

;(background (around :facts (with-redefs [myapp.db/conn (fresh-db-conn!)]
(background (around :facts (with-redefs [de.sveri.friendui.models.db/conn-datomic (fresh-db-conn!)]
                                        ?form)))

(fact "iaternuiae"
      (user/create-user "email" "pw" "role"))

;(fact "insert one user should let me retrieve that one user"
;      (let [email "sv@sv.de" pw "sv" role default-role
;            user (user/create-user email pw role)
;            found-entity (d/q '[:find ?c :in $ ?column :where [?c ?column]] (dbc) username-kw)]
;        (count found-entity) => 1
;        (username-kw (get-entity-from-double-vec found-entity)) => email
;        (role-kw (get-entity-from-double-vec found-entity)) => role
;        (activated-kw (get-entity-from-double-vec found-entity)) => false
;        ))
;
;(fact "insert one user should let me retrieve that one user"
;      (let [email "sv@sv.de" pw "sv" role default-role
;            user (user/create-user email pw role)
;            found-entity (d/q '[:find ?c :in $ ?col :where [?c ?col]] (dbc) username-kw)]
;        (count found-entity) => 1
;        (username-kw (get-entity-from-double-vec found-entity)) => email
;        (role-kw (get-entity-from-double-vec found-entity)) => role
;        (activated-kw (get-entity-from-double-vec found-entity)) => false
;        ))
;
;(fact "get a list of usernames and password (also test if usernames are unique)"
;      (let [mails ["sv@sv.de" "bla@bla.de" "foo@bar.de" "msdf@mdf.com" "foo@bar.de"]]
;        (doall (for [mail mails] (user/create-user mail "pw" :role)))
;        (count (d/q '[:find ?c :in $ ?col :where [?c ?col]] (dbc) username-kw)) => 4
;        (count (clojure.set/difference (set mails) (set (map first (user/get-user-password-role-map))))) => 0
;        (count (set (map second (user/get-user-password-role-map)))) => 4
;        (count (set (map #(nth % 2) (user/get-user-password-role-map)))) => 1
;        ))
;
;(fact "username exists"
;      (let [email "sv@sv.de" pw "sv" role default-role]
;        (user/username-exists email) => false
;        (user/create-user email pw role)
;        (user/username-exists email) => true
;        ))
;
;(fact "generate correct activation link"
;      (let [activationid (generate-activation-id)]
;        (generate-activation-link activationid) => (str "http://example.com/user/activate/" activationid )))
;
;(fact "activate user"
;      (let [email "sv@sv.de" pw "sv" role default-role
;            user (user/create-user email pw role)]
;        (user/get-user-by-username email) => {username-kw email role-kw #{role} activated-kw false}
;        (user/is-user-activated email) => false
;        (user/set-user-activated email)
;        (user/get-user-by-username email) => {username-kw email role-kw #{role} activated-kw true}
;        (user/is-user-activated email) => true
;        ))
;
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
