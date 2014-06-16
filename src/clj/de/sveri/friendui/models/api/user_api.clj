(ns de.sveri.friendui.models.api.user-api
  (:require [de.sveri.friendui.models.db :as db]
            [de.sveri.friendui.models.user :as user]
            [cemerick.friend :as friend]
            [cemerick.friend.credentials :as creds]
            [datomic.api :as d]))

(defn get-logged-in-username [] (:username (friend/current-authentication)))

(defn username-exists? [db-val username]
  (> (count (db/find-by-column-and-search-string db-val db/username-kw username)) 0))

(defn create-user-map
  "First form creates a new user which is inactive.
  Second form creates a new user with the given constraints if the username does not exist already."
  ([email password role & [activationid]]
   {:pre [(not (nil? (and email password role)))]}
   (let [pw_crypted (creds/hash-bcrypt password)]
     (create-user-map email (merge
                              {db/username-kw  email
                               db/pw-kw        pw_crypted
                               db/activated-kw false
                               db/role-kw      (keyword role)}
                              (when activationid {user/activationid-kw activationid})))))
  ([username data-map]
   (merge {db/username-kw username} data-map)))

(defn get-user-for-activation-id
  "Returns the user for a given activation id."
  [db-conn id]
  (let [user-entity (db/get-entity-from-double-vec db-conn (db/find-by-column-and-search-string db-conn user/activationid-kw id))]
    {:username (db/username-kw user-entity)
     :roles    #{(db/role-kw user-entity)}}))

(defn insert-user
  "inserts a user only if the user does not exist already. Otherwise it throws an AssertionError."
  [db-conn username data-map]
  {:pre [(not (username-exists? (d/db db-conn) username))]}
  (db/insert-entity db-conn data-map))

(defn get-user-password-role-map []
  (let [db (db/get-new-conn)
        user-ids (db/find-all-from-column db db/username-kw)]
    (doall (for [id user-ids]
             (let [user (db/get-entity-from-vec db id)]
               [(db/username-kw user) (db/pw-kw user) (db/role-kw user)])))))

(defn get-user-by-username
  "retrieves the users important fields
  if pw param is set, it will give the password back too in map"
  [db-val username & pw]
  (let [db-id (db/get-entity-from-double-vec db-val (db/find-by-column-and-search-string db-val db/username-kw username))
        user (assoc (select-keys db-id db/all-namespaced-profile-keywords) db/role-kw (user/get-roles db-id))]
    (if pw
      (merge user (into {} [[:password (db/pw-kw db-id)]]))
      user)))

(defn account-activated? [activationid]
  (let [db-conn (db/get-new-conn)
        id (db/find-by-column-and-search-string db-conn db/activated-kw activationid)]
    (if (= (db/activated-kw id) true) true false)))

(defn activate-account [activationid]
  (let [db-conn (db/get-new-conn)
        id (db/find-by-column-and-search-string db-conn user/activationid-kw activationid)]
    @(d/transact db/conn-datomic [{:db/id (ffirst id) db/activated-kw true}])))

(defn update-user [username data]
  @(d/transact db/conn-datomic [(merge {:db/id [db/username-kw username]} data)]))

(defn update-loggedin-user [data] (update-user (get-logged-in-username) data))

(defn is-logged-in? [] (if (get-logged-in-username) true false))

(defn get-all-users
  "Returns a list of user maps with all data available in database, without the password."
  []
  (let [db-conn (db/get-new-conn)]
    (sort-by
      db/username-kw
      (mapv #(dissoc (into {} (d/touch (db/get-entity-from-vec db-conn %))) db/pw-kw)
            (db/find-all-from-column db-conn db/username-kw)))))

(defn get-user-role [username] (first (:user/role (get-user-by-username username))))

(defn get-logged-in-user-role [] (get-user-role (get-logged-in-username)))

;(defn get-profile-data
;  "Returns all data that is needed for the profile page"
;  [username]
;  (let [user (get-user-by-username username)]
;    (for [field db/add-profile-fields]
;      (let [id (:id-kw field)
;            data (get user id)]
;        (assoc field :data data)))))
