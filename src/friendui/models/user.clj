(ns friendui.models.user
  (:use [datomic.api :only [q db] :as d])
  (:require [cemerick.friend :as friend]
            [friendui.models.db :refer :all]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [friendui.service.user :as userservice]))




(defn username-exists [username]
  (if (> (count (find-by-column-and-search-string username-kw username)) 0) true false))



(defn create-user [email password role]
  (if (not (username-exists email))
    (let [temp_id (d/tempid partition-id)
          pw_crypted (creds/hash-bcrypt password)
          activationid (userservice/generate-activation-id)]
      @(d/transact (conn-datomic) [{:db/id       temp_id
                                    username-kw  email
                                    pw-kw        pw_crypted
                                    activated-kw false
                                    role-kw      (keyword role)
                                   :user/activationid activationid
                                    }])
      (userservice/send-activation-email email activationid)
      {:username email :roles #{(keyword role)} :activated false})))


(defn get-user-password-role-map []
  (let [user-ids (find-all-from-column username-kw)]
    (doall (for [id user-ids]
             (let [user (get-entity-from-vec id)]
               [(username-kw user) (pw-kw user) (role-kw user)])
             )))
  )

(defn get-user-by-username [username]
        (let [db-id (get-entity-from-double-vec (find-by-column-and-search-string username-kw username))]
          (into {} [[:username (username-kw db-id)] [:roles #{(role-kw db-id)}] [:activated (activated-kw db-id)]])
  ))


(defn set-user-activated [username]
  @(d/transact (conn-datomic) [{:db/id [username-kw username], activated-kw true}]))

(def users {"admin" {:username "admin"
                     :password (creds/hash-bcrypt "zzzzzz")
                     :roles    #{:admin}}
            "vip"   {:username "vip"
                     :password (creds/hash-bcrypt "zzzzzz")
                     :roles    #{:vip}}
            "free"  {:username "free"
                     :password (creds/hash-bcrypt "zzzzzz")
                     :roles    #{:free}}
            })

(def friend-settings
  {:credential-fn             (partial creds/bcrypt-credential-fn users)
   :workflows                 [(workflows/interactive-form)]
   :login-uri                 "/user/login"
   :unauthorized-redirect-uri "/user/login"
   :default-landing-uri       "/"})