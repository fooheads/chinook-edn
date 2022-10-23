(ns chinook.schema
  (:require
    [clojure.core.protocols]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.xml :as xml]
    [clojure.zip :as zip]
    [zippo.core :as zippo]))


(def schema-url
  "The url to the copy of the schema in XML in the resources folder"
  "chinook/ChinookDataSet.xsd")


(def xsd
  (-> schema-url (io/resource) (io/input-stream) (xml/parse)))


(def ^:private xsd-loc (zip/xml-zip xsd))


(defn- loc-find [loc pred?]
  (zippo/loc-find loc (zippo/->loc-pred pred?)))


(defn- loc-find-all [loc pred?]
  (zippo/loc-find-all loc (zippo/->loc-pred pred?)))


;;
;; Preds
;;


(defn element? [node]
  (= (:tag node) :xs:element))


(defn unique-element? [node]
  (= (:tag node) :xs:unique))


(defn keyref-element? [node]
  (= (:tag node) :xs:keyref))


(defn table-element? [node]
  (and (element? node)
       (contains? (:attrs node) :msprop:Generator_UserTableName)))


(defn column-element? [node]
  (and (element? node)
       (contains? (:attrs node) :msprop:Generator_UserColumnName)))


(defn- selector? [node]
  (= (:tag node) :xs:selector))


(defn- field? [node]
  (= (:tag node) :xs:field))


(defn column-with-inline-type? [node]
  (-> node :attrs :type))


;;
;; Mappings
;;


(def column-type
  {"xs:int" "integer"
   "xs:string" "string"
   "xs:decimal" "decimal"
   "xs:dateTime" "datetime"})


(defn make-column [table-name node]
  (let [namn (get-in node [:attrs :name])
        typ
        (if (column-with-inline-type? node)
          (get-in node [:attrs :type])
          (get-in node [:content 0 :content 0 :attrs :base]))]
    ^{:node node}
    {:column/table-name table-name
     :column/name namn
     :column/type (column-type typ)}))


(defn make-columns [table-loc]
  (let [table-node (zip/node table-loc)
        table-name (:name (:attrs table-node))
        children-locs (zippo/loc-children table-loc)]
    (mapcat
      (fn [loc]
        (->>
          (loc-find-all loc column-element?)
          (map zip/node)
          (map (partial make-column table-name))))
      (map (comp zip/xml-zip zip/node) children-locs))))


(defn make-table [node]
  {:table/name (get-in node [:attrs :name])})


(defn- table-name [x]
  (some-> x (get-in [:attrs :xpath]) (str/replace #"\.//mstns:" "")))


(defn- field-name [x]
  (some-> x (get-in [:attrs :xpath]) (str/replace #"mstns:" "")))


(defn make-key [node]
  (let [loc (zip/xml-zip node)
        selector (zip/node (loc-find loc selector?))
        fields (map zip/node (loc-find-all loc field?))]

    {:key/name (get-in node [:attrs :name])
     :key/primary? (boolean (get-in node [:attrs :msdata:PrimaryKey]))
     :key/table-name (table-name selector)
     :key/column-names (mapv field-name fields)}))


(defn make-fk [node]
  (let [loc (zip/xml-zip node)
        selector (zip/node (loc-find loc selector?))
        fields (map zip/node (loc-find-all loc field?))]

    {:foreign-key/name (get-in node [:attrs :name])
     :foreign-key/refer (get-in node [:attrs :refer])
     :foreign-key/table-name (table-name selector)
     :foreign-key/column-names (mapv field-name fields)}))


(defn schema []
  (let [table-locs (loc-find-all xsd-loc table-element?)
        unique-locs (loc-find-all xsd-loc unique-element?)
        keyref-locs (loc-find-all xsd-loc keyref-element?)]
    {:table (mapv (comp make-table zip/node) table-locs)
     :column (vec (mapcat make-columns table-locs))
     :key (mapv (comp make-key zip/node) unique-locs)
     :fk (mapv (comp make-fk zip/node) keyref-locs)}))

