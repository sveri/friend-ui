(ns friendui.globals
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def base-template nil)

(def friendui-config-name "friendui-config.edn")

(defn from-edn [fname]
  "reads an edn file from classpath"
  (with-open [rdr (-> (io/resource fname)
                      io/reader
                      java.io.PushbackReader.)]
    (edn/read rdr)))

(def friendui-config (from-edn friendui-config-name))
