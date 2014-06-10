(ns de.sveri.friendui.routes.util
  (:require [ring.util.response :as resp]))

(defn render [t]
  (apply str t))

(def resp
  (comp resp/response render))
