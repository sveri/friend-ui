(ns de.sveri.friendui.routes.user
  (:require [de.sveri.friendui.views.layout :as layout]
            [ring.util.response :refer [redirect]]
            [cemerick.friend :as friend]
            [clojure.string :as str]
            [de.sveri.friendui.models.api.user-api :as user-api]
            [de.sveri.friendui.models.db :as db]
            [de.sveri.friendui.views.admin-view :as admin]
            [noir.validation :as vali]
            [net.cgrand.enlive-html :as html]
            [de.sveri.friendui.routes.util :as util]
            [de.sveri.friendui.service.user :as userservice]
            [de.sveri.friendui.globals :as globals]
            [noir.response :as resp]
            [datomic.api :as d]
            [de.sveri.clojure.commons.lists.util :as list-utils]
            [compojure.core :as compojure :refer [GET POST ANY]]))


(def content-key (:base-template-content-key globals/friendui-config))
(def title-key (:base-template-title-key globals/friendui-config))

(defn- map-checkbox-with-bool
  "Returns true if val is not nil"
  [val]
  (if val true false))

(defn create-keywordized-role-set [role]
  #{(keyword "user" role)})

(html/defsnippet error-snippet (str globals/template-path "error-snippet.html") [:div#error] [message]
                 [:#error] (html/content message))

(defn validRegister? [email pass confirm]
  (vali/rule (vali/has-value? email)
             [:id "An email address is required"])
  (vali/rule (vali/is-email? email)
             [:id "A valid email is required"])
  (vali/rule (not (user-api/username-exists? (db/get-new-conn) email))
             [:id "This username exists in the database. Please choose another one."])
  (vali/rule (vali/min-length? pass 5)
             [:pass "Password must be at least 5 characters"])
  (vali/rule (= pass confirm)
             [:confirm "Entered passwords do not match"])
  (not (vali/errors? :id :pass :confirm)))

(defn activate-account [storage id]
  (if (not (user-api/account-activated? id))
    (user-api/activate-account id))
  (friend/merge-authentication
    (redirect "/")
    (user-api/get-user-for-activation-id (db/get-new-conn) id)))


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
(defn signup [& [errors]] (util/resp (globals/base-template {title-key "Signup" content-key (signup-enlive errors)})))
(defn account-created [] (util/resp (globals/base-template {title-key "Account Created" content-key (account-created-snippet)})))
(defn account-activated [] (util/resp (globals/base-template {title-key "Account Activated" content-key (account-activated-snippet)})))

(defn admin-view [& [data]]
  (util/resp (globals/base-template
               {title-key   "User Administration"
                content-key (admin/admin-enlive
                              (let [users (user-api/get-all-users)
                                    username-filter (:username-filter data)]
                                (if username-filter
                                  (list-utils/filter-list users username-filter db/username-kw)
                                  users))
                              data)})))

(defn add-user
  "Creates a new user in the database. Acts for both the signup and the administrator form.
  If send_email is not nil it will send an activation email to the given email adress with a link that the user can use
  to activate it's account."
  [storage email password confirm succ-page error-page & [send_email]]
  (if (validRegister? email password confirm)
    (do
      (if send_email
        (let [activationid (userservice/generate-activation-id)]
          (do
            (globals/create-user storage email password activationid)
            (userservice/send-activation-email email activationid))))
      (resp/redirect succ-page))
    (let [email-error (vali/on-error :id first)
          pass-error (vali/on-error :pass first)
          confirm-error (vali/on-error :confirm first)]
      (error-page {:email-error email-error :pass-error pass-error :confirm-error confirm-error}))))

(defn update-user [username role active]
  (user-api/update-user username {db/role-kw (create-keywordized-role-set role) db/activated-kw (map-checkbox-with-bool active)})
  (resp/redirect "/user/admin"))

(defn user-routes [storage]
  (compojure/routes
    (GET "/user/login" [login_failed] (login login_failed))
    (GET "/user/signup" [] (signup))
    (POST "/user/signup" [email password confirm]
          (add-user storage email password confirm "/user/accountcreated" signup true))
    (GET "/user/accountcreated" [] (account-created))
    (GET "/user/activate/:id" [id] (activate-account storage id))
    (GET "/user/accountactivated" [] (account-activated))
    (GET "/user/admin" [filter] (friend/authorize #{:user/admin} (admin-view {:username-filter filter})))
    (POST "/user/update" [username role active] (friend/authorize #{:user/admin} (update-user username role active)))
    (POST "/user/add" [email password confirm]
          (friend/authorize #{:user/admin} (add-user email password confirm "/user/admin" admin-view)))
    (friend/logout (ANY "/user/logout" [] (redirect "/")))))
;
;(22:38:13) hiredman: sveri: type hinting using a protocol doesn't make sense, the constructor call for you deftype is wrong, using deftype for that is weird, etc, etc
;(22:38:34) aconbere [~aconbere@71-212-34-18.tukw.qwest.net] hat den Raum betreten.
;(22:38:45) noonian: sveri: when you define a protocol, the functions of the protocol are defined in that namespace, everything from other namespaces always needs to be either namespace qualified or referred to in order to be called

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