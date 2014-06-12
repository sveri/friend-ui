(ns de.sveri.friendui.models.db
  ;(:use [datomic.api :only [q db] :as d])
  (:require [de.sveri.friendui.globals :as globals]
            [datomic.api :as d]))


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

;(defn conn-datomic [] (d/connect uri-datomic))
;(defn conn-datomic [] (delay (d/connect uri-datomic)))

(def conn-datomic (delay (d/connect uri-datomic)))


;(defn dbc [] (db (conn-datomic)))

(defn get-new-conn [] (d/db @conn-datomic))

(defn create-entity [data-map]
  (let [temp_id (d/tempid partition-id)]
    [(merge {:db/id temp_id} data-map)]))

(defn find-by-column-and-search-string [db column search]
  (d/q '[:find ?c
       :in $ ?column ?name
       :where [?c ?column ?name]]
     db
     column
     search))

(defn find-all-from-column [db column]
  (d/q '[:find ?c :in $ ?column :where [?c ?column]] db column))

(defn get-entity-from-double-vec [db id]
  (d/entity db (ffirst id)))

(defn get-entity-from-vec [db id]
  (d/entity db (first id)))






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
