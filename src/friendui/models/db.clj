(ns friendui.models.db
  (:use [datomic.api :only [q db] :as d]))

(def uri-datomic "datomic:dev://localhost:4334/friendui")

(def conn-datomic (d/connect uri-datomic))

(defn dbc [] (db conn-datomic))

(defn find-by-column-and-search-string [column search]
  (q '[:find ?c
       :in $ ?column ?name
       :where [?c ?column ?name]]
     (dbc)
     column
     search))






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
