(ns de.sveri.friendui.views.admin-view
  (:require [net.cgrand.enlive-html :as html]
            [de.sveri.friendui.models.db :as db]
            [de.sveri.friendui.globals :as globals]
            [de.sveri.friendui.models.user :as user]))

(def user-admin-table-header [{:header "Username"}
                              {:header "Role"}
                              {:header "Active"}
                              {:header ""}])

(html/defsnippet role-option (str globals/template-path "admin.html") [(html/attr= :field "role_option")]
                 [option selected]
                 [(html/attr= :field "role_option")] (html/do->
                                                       (html/content (name option))
                                                       (if selected (html/set-attr :selected selected) identity)))

(html/defsnippet admin-user-table-content (str globals/template-path "admin.html") [(html/attr= :field "table-entry")]
                 [user]
                 [(html/attr= :field "username-td")] (html/content (db/username-kw user))
                 [(html/attr= :field "role-select")]
                 (html/content (map #(role-option % (when (= (name (db/role-kw user)) (name %)) true))
                                    db/available-roles))
                 [(html/attr= :field "active-td") :input] (when (db/activated-kw user) (html/set-attr :checked true)))

(html/defsnippet admin-user-table-header (str globals/template-path "admin.html") [(html/attr= :field "header-entry")]
                 [{:keys [header]}]
                 [(html/attr= :field "header-entry")] (html/content header))

(html/defsnippet admin-enlive (str globals/template-path "admin.html") [:#admin]
                 []
                 [(html/attr= :field "table-header-row")] (html/content (map #(admin-user-table-header %) user-admin-table-header))
                 [(html/attr= :field "table-entries")] (html/content (map #(admin-user-table-content %) (user/get-all-users))))
