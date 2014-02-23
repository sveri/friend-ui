(ns friendui.models.user
  (:use [datomic.api :only [q db] :as d])
  (:require [cemerick.friend :as friend]
            [lweb.models.db :refer :all]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(defn create-user [email password]
  (println "binr")
  (let [temp_id (d/tempid :lweb)
        pw_crypted (creds/hash-bcrypt password)]
    @(d/transact conn-datomic [{:db/id temp_id
                                :user/email email
                                :user/password pw_crypted
                                :user/activated false
                                :user/role ":free"}])
    {:username email
     :roles #{:free}}))



(def users {"admin" {:username "admin"
                    :password (creds/hash-bcrypt "zzzzzz")
                    :roles #{:admin}}
            "vip" {:username "vip"
                    :password (creds/hash-bcrypt "zzzzzz")
                    :roles #{:vip}}
            "free" {:username "free"
                    :password (creds/hash-bcrypt "zzzzzz")
                    :roles #{:free}}
            })

(def friend-settings
  {:credential-fn (partial creds/bcrypt-credential-fn users)
   :workflows [(workflows/interactive-form)]
   :login-uri "/user/login"
   :unauthorized-redirect-uri "/user/login"
   :default-landing-uri "/"})

