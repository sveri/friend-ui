(ns de.sveri.friendui.views.admin-view
  (:require [net.cgrand.enlive-html :as html]
            [de.sveri.friendui.globals :as globals]
            [ring.middleware.anti-forgery :as af]))

(def user-admin-table-header [{:header "Username"}
                              {:header "Role"}
                              {:header "Active"}
                              {:header ""}])

(html/defsnippet error-snippet (str globals/template-path "error-snippet.html") [:div#error] [message]
                 [:#error] (html/content message))

(html/defsnippet role-option (str globals/template-path "admin.html") [(html/attr= :field "role_option")]
                 [option selected]
                 [(html/attr= :field "role_option")] (html/do->
                                                       (html/content (name option))
                                                       (if selected (html/set-attr :selected selected) identity)))

(html/defsnippet admin-user-table-content (str globals/template-path "admin.html") [(html/attr= :field "user-row")]
                 [user]
                 [(html/attr= :name "__anti-forgery-token")] (html/set-attr :value af/*anti-forgery-token*)
                 [(html/attr= :field "username")] (html/content (globals/username-kw user))
                 [(html/attr= :field "username-hidden")] (html/set-attr :value (globals/username-kw user))
                 [(html/attr= :field "role-select")]
                    (html/content (map #(role-option % (when (= (name (globals/role-kw user)) (name %)) true))
                                       globals/available-roles))
                 [(html/attr= :field "active") :input] (if (globals/activated-kw user) (html/set-attr :checked true) identity)
                 [(html/attr= :field "change_password")] (html/set-attr :href (str "/user/changepassword/" (globals/username-kw user))))

(html/defsnippet admin-user-table-header (str globals/template-path "admin.html") [(html/attr= :field "header-entry")]
                 [{:keys [header]}]
                 [(html/attr= :field "header-entry")] (html/content header))

(html/defsnippet add-user (str globals/template-path "signup.html") [:form]
                 [{:keys [email-error pass-error confirm-error]}]
                 [(html/attr= :name "__anti-forgery-token")] (html/set-attr :value af/*anti-forgery-token*)
                 [:div#email-error] (when email-error (fn [_] (error-snippet email-error)))
                 [:div#pass-error] (when pass-error (fn [_] (error-snippet pass-error)))
                 [:div#confirm-error] (when confirm-error (fn [_] (error-snippet confirm-error)))
                 [:form] (html/do-> (html/set-attr :class "form-inline")
                                    (html/set-attr :action "/user/add"))
                 [:h3] nil
                 [:.btn] (html/do-> (html/set-attr :value "Add a new user")
                                    (html/set-attr :style "margin-top: 15px;")))

(html/defsnippet admin-enlive (str globals/template-path "admin.html") [:#admin]
                 [users & [add-user-errors]]
                 [:#add-user-div] (html/content (add-user add-user-errors))
                 [(html/attr= :field "table-header-row")] (html/content (map #(admin-user-table-header %) user-admin-table-header))
                 [(html/attr= :field "user-row")] (html/content (map #(admin-user-table-content %) users)))
