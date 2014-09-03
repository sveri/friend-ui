(ns de.sveri.friendui.routes.user
  (:require [ring.util.response :refer [redirect]]
            [cemerick.friend :as friend]
            [cemerick.friend.credentials :as creds]
            [clojure.string :as str]
            [de.sveri.friendui.service.user :as user-api]
            [de.sveri.friendui.views.admin-view :as admin]
            [noir.validation :as vali]
            [net.cgrand.enlive-html :as html]
            [de.sveri.friendui.routes.util :as util]
            [de.sveri.friendui.service.user :as userservice]
            [de.sveri.friendui.globals :as globals]
            [noir.response :as resp]
            [de.sveri.clojure.commons.lists.util :as list-utils]
            [compojure.core :as compojure :refer [GET POST ANY]]
            [ring.middleware.anti-forgery :as af]
            ))


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

(defn validRegister? [storage email pass confirm]
  (vali/rule (vali/has-value? email)
             [:id "An email address is required."])
  (vali/rule (vali/is-email? email)
             [:id "A valid email is required."])
  (vali/rule (not (globals/username-exists? storage email))
             [:id "This username exists in the database. Please choose another one."])
  (vali/rule (vali/min-length? pass 5)
             [:pass "Password must be at least 5 characters."])
  (vali/rule (= pass confirm)
             [:confirm "Entered passwords do not match."])
  (not (vali/errors? :id :pass :confirm)))

(defn is-change-pw-valid? [storage old-pw pass confirm]
  (vali/rule (creds/bcrypt-verify old-pw (globals/get-old-pw-hash storage))
             [:old-pass "You entered the wrong current password."])
  (vali/rule (vali/min-length? pass 5)
             [:pass "Password must be at least 5 characters."])
  (vali/rule (= pass confirm)
             [:confirm "Entered passwords do not match."])
  (not (vali/errors? :old-pass :pass :confirm)))

(defn activate-account [storage id & [{:keys [activate-account-succ-func]}]]
  (if (not (globals/account-activated? storage id))
    (globals/activate-account storage id))
  (friend/merge-authentication
    (redirect globals/account-activated-redirect)
    (let [user (globals/get-user-for-activation-id storage id)]
      (when activate-account-succ-func (activate-account-succ-func user))
      user)))


(html/defsnippet account-created-snippet (str globals/template-path "account-created.html") [:div#account-created] [])
(html/defsnippet account-activated-snippet (str globals/template-path "account-activated.html") [:div#account-activated] [])

(html/defsnippet changepassword-snippet (str globals/template-path "change-password.html") [:#changepassword]
                 [{:keys [old-pass pass conf]}]
                 [:div#old-pass-error] (when old-pass (fn [_] (error-snippet old-pass)))
                 [:div#pass-error] (when pass (fn [_] (error-snippet pass)))
                 [:div#confirm-error] (when conf (fn [_] (error-snippet conf))))


(html/defsnippet login-enlive (str globals/template-path "login.html") [:div#login]
                 [error]
                 [(html/attr= :name "__anti-forgery-token")] (html/set-attr :value af/*anti-forgery-token*)
                 [:div#error] (when error (fn [_] (error-snippet
                                                    "Bad user / password combination or your account is not activated."))))

(html/defsnippet signup-enlive (str globals/template-path "signup.html") [:div#signup]
                 [{:keys [email-error pass-error confirm-error email]}]
                 [(html/attr= :name "__anti-forgery-token")] (html/set-attr :value af/*anti-forgery-token*)
                 [:#confirmation_message] (when globals/send-activation-email (html/remove-attr :style))
                 [:#email-error] (when email-error (fn [_] (error-snippet email-error)))
                 [:#pass-error] (when pass-error (fn [_] (error-snippet pass-error)))
                 [:#confirm-error] (when confirm-error (fn [_] (error-snippet confirm-error)))
                 [:#email] (if email (html/set-attr :value email) identity))

(defn login [& [login_failed]] (util/resp (globals/base-template {title-key "Login" content-key (login-enlive login_failed)})))
(defn signup [& [errors]] (util/resp (globals/base-template {title-key "Signup" content-key (signup-enlive errors)})))
(defn account-created [] (util/resp (globals/base-template {title-key "Account Created" content-key (account-created-snippet)})))
(defn account-activated [] (util/resp (globals/base-template {title-key "Account Activated" content-key (account-activated-snippet)})))
(defn unauthorized-access [] (util/resp (globals/base-template {title-key "Login"
                                                                content-key "You don't have sufficient rights to view this page."})))

(defn admin-view [storage & [data]]
  (util/resp (globals/base-template
               {title-key   "User Administration"
                content-key (admin/admin-enlive
                              (let [users (globals/get-all-users storage)
                                    username-filter (:username-filter data)]
                                (if username-filter
                                  (list-utils/filter-list users username-filter globals/username-kw)
                                  users))
                              data)}
               (globals/role-kw (globals/get-loggedin-user-map storage)))))

(defn add-user
  "Creates a new user in the database. Acts for both the signup and the administrator form.
  If send_email is not nil it will send an activation email to the given email adress with a link that the user can use
  to activate it's account."
  [storage email password confirm succ-page error-page send_email & [{:keys [signup-succ-func]}]]
  (if (validRegister? storage email password confirm)
    (do
      (let [activationid (userservice/generate-activation-id)
            pw_crypted (creds/hash-bcrypt password)]
        (do
          (globals/create-user storage email pw_crypted globals/new-user-role activationid)
          (if (and send_email globals/send-activation-email)
            (userservice/send-activation-email email activationid))
          (when signup-succ-func (signup-succ-func))))
      (resp/redirect succ-page))
    (let [email-error (vali/on-error :id first)
          pass-error (vali/on-error :pass first)
          confirm-error (vali/on-error :confirm first)]
      (error-page {:email-error email-error :pass-error pass-error :confirm-error confirm-error :email email}))))

(defn update-user [storage username role active]
  (globals/update-user storage username {globals/role-kw (create-keywordized-role-set role) globals/activated-kw (map-checkbox-with-bool active)})
  (resp/redirect "/user/admin"))

(defn changepassword
  ([storage] (changepassword storage nil))
  ([storage errormap]
   (util/resp (globals/base-template
                   {title-key   "User Administration"
                    content-key (changepassword-snippet errormap)}
                   (globals/role-kw (globals/get-loggedin-user-map storage)))))
  ([storage oldpassword password confirm]
   (if (is-change-pw-valid? storage oldpassword password confirm)
     (do (globals/change-password storage (creds/hash-bcrypt password))
         (resp/redirect "/user/changepassword"))
       (changepassword storage {:old-pass (vali/on-error :old-pass first)
                                :pass (vali/on-error :pass first)
                                :conf (vali/on-error :confirm first)}))))

(defn friend-routes [storage & [callback-map]]
  (compojure/routes
    (GET "/user/login" [login_failed] (login login_failed))
    (GET "/user/signup" [] (signup))
    (vali/wrap-noir-validation
      (POST "/user/signup" [email password confirm]
            (add-user storage email password confirm globals/user-signup-redirect signup true callback-map)))
    (GET "/user/accountcreated" [] (account-created))
    (GET "/user/activate/:id" [id] (activate-account storage id callback-map))
    (GET "/user/accountactivated" [] (account-activated))
    (GET "/user/changepassword" [] (friend/authorize #{globals/new-user-role} (changepassword storage)))
    (POST "/user/changepassword" [oldpassword password confirm]
          (friend/authorize #{globals/new-user-role} (changepassword storage oldpassword password confirm)))
    (GET "/user/admin" [filter] (friend/authorize #{:user/admin} (admin-view storage {:username-filter filter})))
    (POST "/user/update" [username role active] (friend/authorize #{:user/admin} (update-user storage username role active)))
    (vali/wrap-noir-validation
      (POST "/user/add" [email password confirm]
            (friend/authorize #{:user/admin}
                              (add-user storage email password confirm "/user/admin" (partial admin-view storage) false))))
    (friend/logout (ANY "/user/logout" [] (redirect "/")))))