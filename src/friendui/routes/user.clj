(ns friendui.routes.user
  (:use compojure.core)
  (:require [friendui.views.layout :as layout]
            [ring.util.response :refer [redirect]]
            [cemerick.friend :as friend]
            [clojure.string :as str]
            [friendui.models.user :as db]
            [noir.validation :as vali]))

(defn login []
  (layout/render "user/login.html"))

(defn admin []
  (layout/render "user/profile.html"))

(defn validRegister? [email pass confirm]
  (vali/rule (vali/has-value? email)
             [:id "An email address is required"])
  (vali/rule (vali/is-email? email)
             [:id "A valid email is required"])
  (vali/rule (not (db/username-exists email))
             [:id "This username exists in the database. Please choose another one."])
  (vali/rule (vali/min-length? pass 5)
             [:pass "Password must be at least 5 characters"])
  (vali/rule (= pass confirm)
             [:confirm "Entered passwords do not match"])
  (not (vali/errors? :id :pass :confirm)))


;(defn handle-signup [email password confirm req]
;  (if (and (not-any? str/blank? [email password confirm])
;           (= password confirm))
;    (let [user (db/create-user email password "free")]
;      ;; HERE IS WHERE YOU'D PUSH THE USER INTO YOUR DATABASES if desired
;      (friend/merge-authentication
;        (redirect "/")
;        ;(redirect (misc/context-uri req username))
;        user))
;    (assoc (redirect (str (:context req) "/")) :flash "passwords don't match!")))


(defn signup []
  (layout/render "user/signup.html"
                 {:id-error      (vali/on-error :id first)
                  :pass-error    (vali/on-error :pass first)
                  :confirm-error (vali/on-error :confirm first)}))

(defn account-activated []
  (layout/render "user/account-activated.html"))

(defn activate-account [id]
  (println (db/get-user-for-activation-id id))
  (if (not (db/account-activated id))
    (db/activate-account id))
  (friend/merge-authentication
    (redirect "/")
    (db/get-user-for-activation-id id))
  )


(defn account-created []
  (layout/render "user/account-created.html"))

(defn handle-signup [email password confirm]
  (if (validRegister? email password confirm)
    (do
      (db/create-user email password "free")
      ;(redirect "/" :flash "Your new account was registered. Please activate it now.")
      (account-created)
      )
    (signup)
    ))

(defroutes user-routes
           (GET "/user/login" [] (login))
           (GET "/user/signup" [] (signup))
           (POST "/user/signup" [email password confirm]
                 (handle-signup email password confirm))
           (GET "/user/accountcreated" [] (account-created))
           (GET "/user/activate/:id" [id] (activate-account id))
           (GET "/user/accountactivated" [] (account-activated))
           (GET "/user/admin" request (friend/authorize #{:admin} (admin)))
           (GET "/user/freetest" [] (friend/authorize #{:free} (account-created)))
           (friend/logout (ANY "/user/logout" [] (redirect "/")))
           )