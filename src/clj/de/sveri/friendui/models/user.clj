(ns de.sveri.friendui.models.user
  (:use [datomic.api :only [q db] :as d])
  (:require [cemerick.friend :as friend]
            [de.sveri.friendui.models.db :refer :all]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [de.sveri.friendui.models.db :as db]))


(def activationid-kw :user/activationid)

(defn username-exists [username]
  (if (> (count (find-by-column-and-search-string username-kw username)) 0) true false))

(defn get-logged-in-username [] (:username (friend/current-authentication)))

(defn- create-user-with-map [data-map]
  (let [temp_id (d/tempid partition-id)]
    @(d/transact (conn-datomic) [(merge {:db/id temp_id} data-map)])))

(defn create-user
  "First form creates a new user with the given constraints if the username does not exist already.
  Second form creates a new user which is inactive."
  ([username data-map]
  (if (not (username-exists username))
    (create-user-with-map (merge {db/username-kw username} data-map))))
  ([email password role & [activationid]]
  (if (not (username-exists email))
    (let [pw_crypted (creds/hash-bcrypt password)]
      (create-user-with-map (merge
                              {username-kw  email
                              pw-kw        pw_crypted
                              activated-kw false
                              role-kw      (keyword role)}
                              (when activationid {activationid-kw activationid})))))))

(defn get-user-password-role-map []
  (let [user-ids (find-all-from-column username-kw)]
    (doall (for [id user-ids]
             (let [user (get-entity-from-vec id)]
               [(username-kw user) (pw-kw user) (role-kw user)])
             ))))

(defn- get-roles
  "retrieves all roles a user belongs to and returns them as a set.
  Username should be an entity id"
  [db-id]
  #{(role-kw db-id)})

(defn get-user-by-username
  "retrieves the users important fields
  if pw param is set, it will give the password back too in map"
  [username & pw]
  (let [db-id (get-entity-from-double-vec (find-by-column-and-search-string username-kw username))
        user (assoc (select-keys db-id all-namespaced-profile-keywords) role-kw (get-roles db-id))]
    (if pw
      (merge user (into {} [[:password (pw-kw db-id)]]))
      user)))

(defn get-user-db-entity
  ([] (get-user-db-entity (get-logged-in-username)))
  ([username]
   (get-entity-from-double-vec (find-by-column-and-search-string username-kw username))))

(defn set-user-activated [username] @(d/transact (conn-datomic) [{:db/id [username-kw username], activated-kw true}]))

(defn account-activated [activationid]
  (let [id (find-by-column-and-search-string activated-kw activationid)]
    (if (= (activated-kw id) true) true false)))

(defn activate-account [activationid]
  (let [id (find-by-column-and-search-string activationid-kw activationid)]
    @(d/transact (conn-datomic) [{:db/id (ffirst id) activated-kw true}])))

(defn get-user-for-activation-id [id]
  (let [user-entity (get-entity-from-double-vec (find-by-column-and-search-string activationid-kw id))]
    {:username (username-kw user-entity)
     :roles    #{(role-kw user-entity)}}))

(defn is-user-activated [username]
  (if (= (activated-kw (get-user-by-username username)) true) true false))

(defn login-user [username]
  (if (is-user-activated username)
    (do
      (let [db-id (get-entity-from-double-vec (find-by-column-and-search-string username-kw username))]
        {:username username :roles (get-roles db-id) :password (pw-kw db-id)}))))

(defn update-user [username data]
  @(d/transact (conn-datomic) [(merge {:db/id [db/username-kw username]} data)]))

(defn get-profile-data
  "Returns all data that is needed for the profile page"
  [username]
  (let [user (get-user-by-username username)]
    (for [field add-profile-fields]
      (let [id (:id-kw field)
            data (get user id)]
        (assoc field :data data)))))

(defn update-loggedin-user [data] (update-user (get-logged-in-username) data))

(defn is-logged-in [] (if (get-logged-in-username) true false))

(defn get-all-users
  "Returns a list of user maps with all data available in database, without the password."
  []
  (sort-by db/username-kw (mapv #(dissoc (into {} (d/touch (get-entity-from-vec %))) pw-kw) (find-all-from-column username-kw))))

(defn get-user-role [username] (first (:user/role (get-user-by-username username))))

(defn get-logged-in-user-role [] (get-user-role (get-logged-in-username)))

(def friend-settings
  {:credential-fn             (partial creds/bcrypt-credential-fn login-user)
   :workflows                 [(workflows/interactive-form)]
   :login-uri                 "/user/login"
   :unauthorized-redirect-uri "/user/login"
   :default-landing-uri       "/"})
