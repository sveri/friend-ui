(ns friendui.models.user
  (:use [datomic.api :only [q db] :as d])
  (:require [cemerick.friend :as friend]
            [friendui.models.db :refer :all]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(defn username-exists [username]
  (if (> (count (find-by-column-and-search-string username-kw username)) 0) true false))


(defn create-user [email password role]
  (let [temp_id (d/tempid partition-id)
        pw_crypted (creds/hash-bcrypt password)]
    @(d/transact (conn-datomic) [{:db/id       temp_id
                                  username-kw  email
                                  pw-kw        pw_crypted
                                  activated-kw false
                                  role-kw      role}]))
  {:username email :roles #{(keyword role)}})

(defn get-user-password-role-map []
  (let [user-ids (find-all-from-column username-kw)]
    (doall (for [id user-ids]
             (let [user (get-entity-from-vec id)]
               [(username-kw user) (pw-kw user) (role-kw user)])
             )))
  )

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

