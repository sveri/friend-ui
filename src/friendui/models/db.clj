(ns friendui.models.db
  (:use [datomic.api :only [q db] :as d])
  (:require [friendui.globals :as globals]))


(def uri-datomic (:datomic-uri globals/friendui-config))
(def partition-id (:partition-id globals/friendui-config))

(def username-kw (:username-kw globals/friendui-config))
(def pw-kw (:pw-kw globals/friendui-config))
(def activated-kw (:activated-kw globals/friendui-config))
(def role-kw (:role-kw globals/friendui-config))
(def hostname (:hostname globals/friendui-config))
(def mail-from (:mail-from globals/friendui-config))
(def available-roles (:available-roles globals/friendui-config))
(def add-profile-fields (:additional-profile-fields globals/friendui-config))
(def add-profile-keywords (map #(:id %) add-profile-fields))

(def all-namespaced-profile-keywords
  (conj (conj add-profile-keywords activated-kw) username-kw))

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
  ; not needed for the tests, but something like this should be done in the project that uses friendui

  (require '[datomic.api :as d])
  (def uri "datomic:dev://localhost:4334/friendui")
  (d/delete-database uri)
  (d/create-database uri)
  (def conn (d/connect uri))
  (def schema-tx (read-string (slurp "resources/schema/datomic-schema.edn")))
  @(d/transact conn schema-tx)

  (def data-tx (read-string (slurp "resources/schema/datomic-data.edn")))
  @(d/transact conn data-tx)

  )
