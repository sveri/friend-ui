(ns friendui.routes.user
  (:use compojure.core)
  (:require [friendui.views.layout :as layout]
            [ring.util.response :refer [redirect]]
            [cemerick.friend :as friend]
            [clojure.string :as str]
            [friendui.models.user :as db]
            [friendui.models.db :refer [add-profile-fields]]
            [noir.validation :as vali]))

(defn login [& [login_failed]]
  (layout/render "user/login.html"
                 (if login_failed {:error "Wrong username/password combo, or your account is not activated yet."})))

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

(defn signup []
  (layout/render "user/signup.html"
                 {:id-error      (vali/on-error :id first)
                  :pass-error    (vali/on-error :pass first)
                  :confirm-error (vali/on-error :confirm first)}))

(defn account-activated []
  (layout/render "user/account-activated.html"))

(defn activate-account [id]
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
      (account-created))
    (signup)
    ))

(defn profile []
  (layout/render "user/profile.html"
                 (assoc {:username (:username (friend/current-authentication))} :add-fields add-profile-fields)))

(defn handle-profile [email & fields]
  (println email " " fields)
  (profile))

(defroutes user-routes
           (GET "/user/login" [login_failed] (login login_failed))
           (GET "/user/signup" [] (signup))
           (POST "/user/signup" [email password confirm]
                 (handle-signup email password confirm))
           (GET "/user/accountcreated" [] (account-created))
           (GET "/user/activate/:id" [id] (activate-account id))
           (GET "/user/accountactivated" [] (account-activated))
           (GET "/user/admin" request (friend/authorize #{:admin} (admin)))
           (GET "/user/freetest" [] (friend/authorize #{:free} (account-created)))
           (GET "/user/profile" [] (friend/authenticated (profile)))
           (POST "/user/profile" request (handle-profile request))
           (friend/logout (ANY "/user/logout" [] (redirect "/")))
           )