(ns de.sveri.friendui.globals
  (:require [de.sveri.clojure.commons.files.edn :as commons]))

(def template-path "templates/de/sveri/friendui/user/")

(def base-template nil)

(def friendui-config-name "friendui-config.edn")

(def friendui-config (commons/from-edn friendui-config-name))

(def username-kw (:username-kw friendui-config))
(def pw-kw (:pw-kw friendui-config))
(def activated-kw (:activated-kw friendui-config))
(def role-kw (:role-kw friendui-config))
(def hostname (:hostname friendui-config))
(def available-roles (:available-roles friendui-config))
(def mail-from (:mail-from friendui-config))
(def new-user-role (:new-user-role friendui-config))
(def user-signup-redirect (:user-signup-redirect friendui-config))
(def account-activated-redirect (:account-activated-redirect friendui-config))
(def send-activation-email (:send-activation-email friendui-config))


(defprotocol FrienduiStorage
  "Defines methods to acces user storage for retrieval and update."
  (account-activated? [this activationid]
                      "Provides an id. Expects a boolean return value indicating if the user, belonging to the id is
                      activated or not.")
  (activate-account [this activationid]
                    "Should set the user with the given id to activated. After this function was called successfully
                    (account-activated?) should return true.")
  (create-user [this email password role activationid]
               "Should add a new user to your data store. Return value is not checked.")
  (get-all-users [this]
                 "Called from the admin view. Expects a list of all known users in this format:
                 ({:user/activated false, :user/role :user/admin, :user/email unique-email@host.com}
                 ...)
                 Where each key corresponds to the configured keyword from friendui-config.edn")
  (get-user-for-activation-id [this id]
                              "Should return a map containing the username and role of this user like this:
                              {:username username :roles #{role}}")
  (update-user [this username data-map]
               "Updates the user with the given data map of the form: {:user/activated boolean :user/role :user/free}")
  (username-exists? [this username]
                    "Expects true if the username exists already in the storage, false otherwise."))
