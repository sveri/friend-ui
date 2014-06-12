(ns friendui.models.usertest
  (:require [datomic.api :as d]
            [midje.sweet :refer :all]
            [de.sveri.friendui.models.user :as user]
            [de.sveri.friendui.models.db :as db]
            [de.sveri.friendui.service.user :as user-service]
            [cemerick.friend.credentials :as creds]
            [clojure.set :as set]
            ))

;(d/create-database uri-datomic)
(def schema-tx (read-string (slurp "resources/schema/datomic-schema.edn")))

(def default-role :free)

(def uri "datomic:mem://friendui")

(d/create-database uri)

(def conn-datomic (d/connect uri))


@(d/transact conn-datomic schema-tx)

(def base-db (d/db conn-datomic))
;
;(defn dbc [] (d/db conn-datomic))
;(defn delete-and-create-db []
;  (d/delete-database uri)
;  (d/create-database uri)
;  @(d/transact conn-datomic schema-tx))
;
;(background (before :facts (delete-and-create-db) :after ()))

(println "kw: " db/username-kw)

;(println (db/find-all-from-column (:db-after (d/with base-db (db/create-entity {db/username-kw "foo@foo.de"}))) db/username-kw))
(println (user/username-exists (:db-after (d/with base-db (db/create-entity {db/username-kw "foo@foo.de"}))) "foo@foo.de"))
(println (db/find-by-column-and-search-string (:db-after (d/with base-db (db/create-entity {db/username-kw "foo@foo.de"}))) db/username-kw "foo@foo.de"))

;(fact "Connection to datomic succeded"
;      (if (conn-datomic) :truthy :falsey) => :truthy)

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

;(defn dbc [] (d/db (fresh-db-conn!)))

; from irc
;(let [user (-> (dbc) (d/with (transaction-for-create-new-user email password role)) (get-user email password))] .. asserts ..)
;(17:51:51) augustl: you can't pass (dbc) to the querying code
;(17:51:58) augustl: because that will give you a new database value from the connection
;(17:52:07) augustl: you need to pass in the db that was returned from "with"

(fact "insert one user should let me retrieve that one user"
      (let [email "sv@sv.de" pw "sv" role default-role
            db (user/create-user conn-datomic email pw role)
            _ (user/create-user conn-datomic email pw role)
            found-entity (d/q '[:find ?c :in $ ?column :where [?c ?column]] (dbc) db/username-kw)]
        ;(count found-entity) => 1
        ;(username-kw (get-entity-from-double-vec found-entity)) => email
        ;(role-kw (get-entity-from-double-vec found-entity)) => role
        ;(activated-kw (get-entity-from-double-vec found-entity)) => false
        ))








;(17:35:59) sveri: augustl: so this is what currently does not work, but what seems to me what ryan suggests, it would be nice if you could have a look at it: http://pastebin.com/pp5pwFW4
;(17:36:38) augustl: sveri: I haven't read much of your code yet.. one thought, though
;(17:36:47) augustl: I like to have pure code that takes values and returns transactions
;(17:37:00) augustl: so, have all the code, including tempid creation, in a function
;(17:37:15) augustl: then all you do with the conn is to call your pure transaction creation code and pass it in to the conn
;(17:37:21) augustl: that will make it easier to reuse for "with" too
;(17:38:28) sveri: augustl: you mean I should put all the stuff into the delete-and-create-db function?
;(17:38:44) augustl: I was talking about create-user-with-map
;(17:38:54) statonjr [~statonjr@rrcs-96-10-65-204.se.biz.rr.com] hat den Raum betreten.
;(17:39:00) augustl: it would make sense to have something like (get-transaction-for-user data-map)
;(17:39:12) augustl: that did all of the transaction creation code
;(17:39:50) augustl: in other words, you end up with transaction creation code that is more reusable - both for passing to transact and to with
;(17:40:06) sveri: I see the advantage, but not how I should do it
;(17:40:50) augustl: (defn create-user-transaction [user-data] [...create transaction here...])
;(17:41:22) augustl: then in the actual code, (d/transact conn (create-user-transaction user-data))
;(17:42:04) sveri: augustl: separate creation of data from insertion of data, right?
;(17:42:29) augustl: more precisely, separate creation of transaction data from executing transactions :)
;(17:42:48) sveri: augustl: ok, I try to write it up, just a sec
;(17:43:39) augustl: now, looking more at your actual code..
;(17:43:53) augustl: your problem is that using (dbc) doesn't let you use "with"?
;(17:44:08) sveri: this is my updated production code: http://pastebin.com/0f0NcpcT
;(17:45:08) sveri: I forgot to delete the db-conn argument in create-user-with-map
;(17:45:28) augustl: if you want to use "with", you have to stop using (dbc) basically :)
;(17:45:58) sveri: augustl: I did not try using with at all yet
;(17:45:58) augustl: then you get faster tests (I guess?) by only testing your transaction creation code using "with" in the tests
;(17:46:39) sveri: And how would I test the data retrieval code then?
;(17:47:25) augustl: in this case you seem to have the query in-lined in the test
;(17:48:02) augustl: but let's assume you have a function (defn get-user [db email pw])
;(17:48:08) sveri: augustl: right, but I have more tests that call production code, that retrieve the data
;(17:49:18) augustl: then your test could do (let [user (-> (dbc) (d/with (transaction-for-create-new-user email password role)) (get-user email password))] .. asserts ..)
;(17:49:45) augustl: if you want your tests to invoke code that takes a connection, you can't use "with" in the tests
;(17:49:58) augustl: if you want to use "with", you can only test the code "one layer down" so to speak
;(17:50:04) augustl: the code that creates transactions and queries db values
;(17:50:12) augustl: s/creates transactions/creates transaction data/
;(17:51:12) sveri: Ok, so basically I am using with for the transaction and then pass the (dbc) to my production code
;(17:51:51) augustl: you can't pass (dbc) to the querying code
;(17:51:58) augustl: because that will give you a new database value from the connection
;(17:52:07) augustl: you need to pass in the db that was returned from "with"
;(17:54:16) sveri: augustl: Ok, that makes more sense, I was wondering how this would work, but retunring a db sounds reasonable
;(17:54:34) wei hat den Raum verlassen (quit: Ping timeout: 252 seconds).
;(17:54:37) augustl: yeah "with" doesn't mutate anything, it returns a new db value
;(17:54:56) augustl: one of the many reasons why as much of your code as possible should work with a "db" not a "conn", for extreme flexiblilty




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
