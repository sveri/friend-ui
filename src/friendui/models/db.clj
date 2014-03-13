(ns friendui.models.db
  (:use [datomic.api :only [q db] :as d])
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))


(defn from-edn [fname]
  "reads an edn file from classpath"
  (with-open [rdr (-> (io/resource fname)
                      io/reader
                      java.io.PushbackReader.)]
    (edn/read rdr)))

(def db-config (from-edn "friendui-config.edn"))
(def uri-datomic (:datomic-uri db-config))
(def partition-id (keyword (:partition-id db-config)))

(def username-kw (keyword (:username-kw db-config)))
(def pw-kw (keyword (:pw-kw db-config)))
(def activated-kw (keyword (:activated-kw db-config)))
(def role-kw (keyword (:role-kw db-config)))
(def hostname (:hostname db-config))
(def mail-from (:mail-from db-config))
(def add-profile-fields (:additional-profile-fields db-config))
(def add-profile-keywords (map #(:id %) add-profile-fields))

(def all-namespaced-profile-keywords
  (do
    (conj
      (conj add-profile-keywords activated-kw) username-kw)))

(defn conn-datomic [] (d/connect uri-datomic))

(defn dbc [] (db (conn-datomic)))

(defn find-by-column-and-search-string [column search]
  (q '[:find ?c
       :in $ ?column ?name
       :where [?c ?column ?name]]
     (dbc)
     column
     search))

(defn find-all-from-column [column]
  (d/q '[:find ?c :in $ ?column :where [?c ?column]] (dbc) column))

(defn get-entity-from-double-vec [id]
  (d/entity (dbc) (ffirst id)))

(defn get-entity-from-vec [id]
  (d/entity (dbc) (first id)))






(comment
  ;execute this in the project repl to initialize the db

  (require '[datomic.api :as d])
  (def uri "datomic:dev://localhost:4334/friendui")
  (d/delete-database uri)
  (d/create-database uri)
  (def conn (d/connect uri))
  (def schema-tx (read-string (slurp "resources/schema/datomic-schema.edn")))
  @(d/transact conn schema-tx)

  (def data-tx (read-string (slurp "resources/schema/datomic-data.edn")))
  @(d/transact conn data-tx)

  ;find all releases
  (d/q '[:find ?e :where [?e :release/name]] (d/db conn))

  )
