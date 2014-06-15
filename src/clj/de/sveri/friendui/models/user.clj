(ns de.sveri.friendui.models.user
  (:use [datomic.api :only [q db] :as d])
  (:require [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [de.sveri.friendui.models.db :as db]
            ))


(def activationid-kw :user/activationid)


(defn get-roles
  "retrieves all roles a user belongs to and returns them as a set.
  Username should be an entity id"
  [db-id]
  #{(db/role-kw db-id)})


(defn get-user-db-entity
  ;([] (get-user-db-entity (get-logged-in-username)))
  ([username]
   (let [db-conn (db/get-new-conn)]
     (db/get-entity-from-double-vec db-conn (db/find-by-column-and-search-string db-conn db/username-kw username)))))

(defn set-user-activated [db-conn username]
  @(d/transact db-conn [{:db/id [db/username-kw username], db/activated-kw true}]))

(defn get-user-for-activation-id [id]
  (let [db-conn (db/get-new-conn)
        user-entity (db/get-entity-from-double-vec db-conn (db/find-by-column-and-search-string db-conn activationid-kw id))]
    {:username (db/username-kw user-entity)
     :roles    #{(db/role-kw user-entity)}}))

(defn is-user-activated? [user]
  (if (= (db/activated-kw user) true) true false))

(def friend-settings
  {:credential-fn             (partial creds/bcrypt-credential-fn login-user)
   :workflows                 [(workflows/interactive-form)]
   :login-uri                 "/user/login"
   :unauthorized-redirect-uri "/user/login"
   :default-landing-uri       "/"})
