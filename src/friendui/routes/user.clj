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
             [:id "Email is required"])
  (vali/rule (vali/is-email? email)
             [:id "valid Email is required"])
  (vali/rule (vali/min-length? pass 5)
             [:pass "password must be at least 5 characters"])
  (vali/rule (= pass confirm)
             [:confirm "entered passwords do not match"])
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
                 {:id-error    (vali/on-error :id first)
                  :pass-error  (vali/on-error :pass first)
                  :pass1-error (vali/on-error :confirm first)}))

(defn handle-signup [email password confirm req]
  (if (validRegister? email password confirm)
    (do
      (db/create-user email password "free")
      (redirect "/" :flash "Your new account was registered. Please activate it now."))
    (signup)
    ))
;(defn handle-registration [id pass pass1]
;  (if (valid? id pass pass1)
;    (try
;      (do
;        (db/create-user {:id id :pass (crypt/encrypt pass)})
;        (session/put! :user-id id)
;        (resp/redirect "/"))
;      (catch Exception ex
;        (vali/rule false [:id (.getMessage ex)])
;        (register)))
;    (register id)))


(defroutes user-routes
           (GET "/user/login" []
                (login))
           (GET "/user/signup" [] (signup))
           (POST "/user/signup" [email password confirm req]
                 (handle-signup email password confirm req))
           (GET "/user/admin" request
                (friend/authorize #{:admin}
                                  (admin)))
           (friend/logout
             (ANY "/user/logout" [] (redirect "/")))

           )