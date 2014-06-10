(ns de.sveri.friendui.views.layout
  (:require
    ;[selmer.parser :as parser]
            [clojure.string :as s]
            [ring.util.response :refer [content-type response]]
            [compojure.response :refer [Renderable]]))

(def template-path "friendui/views/templates/")
