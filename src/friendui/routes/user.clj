(ns friendui.routes.user
  (:use compojure.core)
  (:require [friendui.views.layout :as layout]
            [ring.util.response :refer [redirect]]
            [cemerick.friend :as friend]
            [clojure.string :as str]
            [friendui.models.user :as user]
            [friendui.models.db :as db]
            [friendui.views.admin-view :as admin]
            [noir.validation :as vali]
            [net.cgrand.enlive-html :as html]
            [friendui.routes.util :as util]
            [friendui.globals :as globals]))



(def content-key (:base-template-content-key globals/friendui-config))
(def title-key (:base-template-title-key globals/friendui-config))

(html/defsnippet error-snippet (str globals/template-path "error-snippet.html") [:div#error] [message]
                 [:#error] (html/content message))

(defn validRegister? [email pass confirm]
  (vali/rule (vali/has-value? email)
             [:id "An email address is required"])
  (vali/rule (vali/is-email? email)
             [:id "A valid email is required"])
  (vali/rule (not (user/username-exists email))
             [:id "This username exists in the database. Please choose another one."])
  (vali/rule (vali/min-length? pass 5)
             [:pass "Password must be at least 5 characters"])
  (vali/rule (= pass confirm)
             [:confirm "Entered passwords do not match"])
  (not (vali/errors? :id :pass :confirm)))

(defn activate-account [id]
  (if (not (user/account-activated id))
    (user/activate-account id))
  (friend/merge-authentication
    (redirect "/")
    (user/get-user-for-activation-id id)))


(html/defsnippet account-created-snippet (str globals/template-path "account-created.html") [:div#account-created] [])
(html/defsnippet account-activated-snippet (str globals/template-path "account-activated.html") [:div#account-activated] [])


(html/defsnippet login-enlive (str globals/template-path "login.html") [:div#login]
                 [error]
                 [:div#error] (when error (fn [_] (error-snippet
                                                    "Bad user / password combination or your account is not activated."))))

(html/defsnippet signup-enlive (str globals/template-path "signup.html") [:div#signup]
                 [{:keys [email-error pass-error confirm-error]}]
                 [:div#email-error] (when email-error (fn [_] (error-snippet email-error)))
                 [:div#pass-error] (when pass-error (fn [_] (error-snippet pass-error)))
                 [:div#confirm-error] (when confirm-error (fn [_] (error-snippet confirm-error))))

(defn login [& [login_failed]] (util/resp (globals/base-template {title-key "Login" content-key (login-enlive login_failed)})))
(defn signup [& errors] (util/resp (globals/base-template {title-key "Signup" content-key  (signup-enlive errors)})))
(defn account-created [] (util/resp (globals/base-template {title-key "Account Created" content-key  (account-created-snippet)})))
(defn account-activated [] (util/resp (globals/base-template {title-key "Account Activated" content-key  (account-activated-snippet)})))

(defn handle-signup [email password confirm]
  (if (validRegister? email password confirm)
    (do
      (user/create-user email password "free")
      (account-created))
    (let [email-error (vali/on-error :id first)
          pass-error (vali/on-error :pass first)
          confirm-error (vali/on-error :confirm first)]
      (signup :email-error email-error :pass-error pass-error :confirm-error confirm-error))))

(defn admin-view []
  (util/resp (globals/base-template {title-key "Administration" content-key (admin/admin-enlive)})))

(defroutes user-routes
           (GET "/user/login" [login_failed] (login login_failed))
           (GET "/user/signup" [] (signup))
           (POST "/user/signup" [email password confirm] (handle-signup email password confirm))
           (GET "/user/accountcreated" [] (account-created))
           (GET "/user/activate/:id" [id] (activate-account id))
           (GET "/user/accountactivated" [] (account-activated))
           (GET "/user/admin" [] (friend/authorize #{:user/admin} (admin-view)))
           (friend/logout (ANY "/user/logout" [] (redirect "/"))))

;took out profile capabilities for now

;(GET "/user/profile" [] (friend/authenticated (profile)))
;(POST "/user/profile" request (friend/authenticated (handle-profile (:params request))))
;(defn profile []
;  (layout/render "user/profile.html"
;                 {:fields (user/get-profile-data (:username (friend/current-authentication)))}))
;
;(defn handle-profile [params]
;  (user/update-user
;    (db/username-kw (friend/current-authentication))
;    (select-keys params db/add-profile-keywords))
;  (profile))