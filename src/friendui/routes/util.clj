(ns friendui.util
  ;(:require [noir.io :as io])
  (:use    [ring.util.response :as response]))

(defn render [t]
  (apply str t))

(def resp
  (comp response render))
