(ns friendui.routes.user
  (:use compojure.core)
  (:require [friendui.views.layout :as layout]
    [ring.util.response :refer [redirect]]
            [cemerick.friend :as friend]
            [clojure.string :as str]
            [friendui.models.user :as db]))

(defn login []
  (layout/render "user/login.html"))

(defn admin []
  (layout/render "user/profile.html"))

(defn signup [email password confirm req]
  (if (and (not-any? str/blank? [email password confirm])
           (= password confirm))
    (let [user (db/create-user email password "free")]
      ;; HERE IS WHERE YOU'D PUSH THE USER INTO YOUR DATABASES if desired
      (friend/merge-authentication
        (redirect "/")
        ;(redirect (misc/context-uri req username))
        user))
    (assoc (redirect (str (:context req) "/")) :flash "passwords don't match!")))

(defroutes user-routes
           (GET "/user/login" []
                (login))
           (GET "/user/signup" []
                (layout/render "user/signup.html"))
           (POST "/user/signup" [email password confirm req]
                 (signup email password confirm req))
           (GET "/user/admin" request
                (friend/authorize #{:admin}
                                  (admin)))
           (friend/logout
             (ANY "/user/logout" [] (redirect "/")))

           )
