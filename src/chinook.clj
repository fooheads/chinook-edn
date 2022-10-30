(ns chinook
  "Clojure data/edn version of the chinook database.

  Extracts the data from the SQLite version of the database. For more info,
  see https://github.com/lerocha/chinook-database"
  (:require
    [chinook.schema :as schema]
    [clojure.core.protocols]
    [clojure.string :as str]
    [next.jdbc :as jdbc]
    [next.jdbc.result-set :as rs]))


(def db-url
  "The url to the copy of the SQLite db in the resources folder"
  "jdbc:sqlite::resource:chinook/Chinook_Sqlite.sqlite")


(def ds
  "The datasource to the SQLite database in resources. Don't do any writes
  to this datasource."
  (jdbc/get-datasource db-url))


(defn table-names
  "Gets the table names from the database"
  [ds]
  (->>
    (jdbc/execute! ds ["select * from sqlite_master"])
    (filter #(= (:sqlite_master/type %) "table"))
    (map :sqlite_master/name)))


(def table-k (comp keyword str/lower-case))


(defn table-data
  "Extracts the table data for a given table. opts are the opts passed on to
  `next-jdbc/execute!` and can be used for changing the shape of the result set.
  Default opts is typical clojure kebab-maps."
  ([ds table-name]
   (table-data ds table-name {:builder-fn rs/as-kebab-maps}))
  ([ds table-name opts]
   (jdbc/execute! ds [(str "select * from " table-name)] opts)))


(defn make-edn
  "Creates a map of collections from the database. Default is a map of vectors
  in the shape of

    {:genre [{:genre/genre-id 1 :genre/name \"Rock\"}
             {:genre/genre-id 2 :genre/name \"Jazz\"}
             {:genre/genre-id 3 :genre/name \"Metal\"}]}


  but if you want another collection type for the table data (set, list, ...),
  you can provide an empty collection of that type as `to`."
  ([ds] (make-edn ds []))
  ([ds to] (make-edn ds to {:builder-fn rs/as-kebab-maps}))
  ([ds to opts]
   (let [table-k (or (:chinook/key-fn opts) table-k)]
     (into
       {}
       (for [table-name (table-names ds)]
         [(table-k table-name)
          (into to (with-meta
                     (map #(with-meta % nil) (table-data ds table-name opts))
                     nil))])))))


(def edn
  "edn data from the chinook database as a map of vectors"
  (make-edn ds))


(def edn-with-sets
  "edn data from the chinook database as a map of sets"
  (make-edn ds #{}))


(def schema (schema/schema))

