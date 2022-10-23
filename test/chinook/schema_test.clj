(ns chinook.schema-test
  (:require
    [chinook.schema :as schema]
    [clojure.test :refer [deftest is]]))


(declare table-playlist-track)
(declare pk-invoice-line)
(declare pk-playlist-track)
(declare fk-album-artist)
(declare column-invoice-line-unit-price)
(declare column-customer-fax)


(deftest make-key-test
  (is (= {:key/name "PK_InvoiceLine"
          :key/primary? true
          :key/table-name "InvoiceLine"
          :key/column-names ["InvoiceLineId"]}
         (schema/make-key pk-invoice-line)))

  (is (= {:key/name "PK_PlaylistTrack"
          :key/primary? true
          :key/table-name "PlaylistTrack"
          :key/column-names ["PlaylistId" "TrackId"]}
         (schema/make-key pk-playlist-track))))


(deftest make-fk-test
  (is (= {:foreign-key/name "FK_Artist_Album"
          :foreign-key/refer "PK_Artist"
          :foreign-key/table-name "Album"
          :foreign-key/column-names ["ArtistId"]}
         (schema/make-fk fk-album-artist))))


(deftest make-column-test
  (is (= {:column/table-name "InvoiceLine" :column/name "UnitPrice" :column/type "decimal"}
         (schema/make-column "InvoiceLine" column-invoice-line-unit-price)))

  (is (= {:column/table-name "Customer" :column/name "Fax" :column/type "string"}
         (schema/make-column "Customer" column-customer-fax))))


(deftest make-table-test
  (is (= {:table/name "PlaylistTrack"}
         (schema/make-table table-playlist-track))))


(deftest schema-test
  (is (= {"MediaType" {{:column/table-name "MediaType"} 2}
          "Artist" {{:column/table-name "Artist"} 2}
          "Album" {{:column/table-name "Album"} 3}
          "Customer" {{:column/table-name "Customer"} 13}
          "Invoice" {{:column/table-name "Invoice"} 9}
          "Playlist" {{:column/table-name "Playlist"} 2}
          "Employee" {{:column/table-name "Employee"} 15}
          "Track" {{:column/table-name "Track"} 9}
          "Genre" {{:column/table-name "Genre"} 2}
          "InvoiceLine" {{:column/table-name "InvoiceLine"} 5}
          "PlaylistTrack" {{:column/table-name "PlaylistTrack"} 2}}

         (as->
           (schema/schema) $
           (:column $)
           (group-by :column/table-name $)
           (update-vals $ (fn [xs] (map #(select-keys % [:column/table-name]) xs)))
           (update-vals $ frequencies)))))


(def table-playlist-track
  {:tag :xs:element
   :attrs {:msprop:Generator_RowChangedName "PlaylistTrackRowChanged"
           :msprop:Generator_RowDeletingName "PlaylistTrackRowDeleting"
           :msprop:Generator_RowClassName "PlaylistTrackRow"
           :msprop:Generator_TableVarName "tablePlaylistTrack"
           :msprop:Generator_UserTableName "PlaylistTrack"
           :name "PlaylistTrack"
           :msprop:Generator_TableClassName "PlaylistTrackDataTable"
           :msprop:Generator_RowEvHandlerName "PlaylistTrackRowChangeEventHandler"
           :msprop:Generator_RowDeletedName "PlaylistTrackRowDeleted"
           :msprop:Generator_TablePropName "PlaylistTrack"
           :msprop:Generator_RowChangingName "PlaylistTrackRowChanging"
           :msprop:Generator_RowEvArgName "PlaylistTrackRowChangeEvent"}
   :content [{:tag :xs:complexType
              :attrs nil
              :content [{:tag :xs:sequence
                         :attrs nil
                         :content [{:tag :xs:element
                                    :attrs {:type "xs:int"
                                            :msprop:Generator_ColumnPropNameInTable "PlaylistIdColumn"
                                            :msprop:Generator_ColumnPropNameInRow "PlaylistId"
                                            :msprop:Generator_ColumnVarNameInTable "columnPlaylistId"
                                            :msprop:Generator_UserColumnName "PlaylistId"
                                            :name "PlaylistId"}
                                    :content nil}
                                   {:tag :xs:element
                                    :attrs {:type "xs:int"
                                            :msprop:Generator_ColumnPropNameInTable "TrackIdColumn"
                                            :msprop:Generator_ColumnPropNameInRow "TrackId"
                                            :msprop:Generator_ColumnVarNameInTable "columnTrackId"
                                            :msprop:Generator_UserColumnName "TrackId"
                                            :name "TrackId"}
                                    :content nil}]}]}]})


(def pk-invoice-line
  {:tag :xs:unique
   :attrs {:msdata:PrimaryKey "true" :name "PK_InvoiceLine"}
   :content [{:tag :xs:selector :attrs {:xpath ".//mstns:InvoiceLine"} :content nil}
             {:tag :xs:field :attrs {:xpath "mstns:InvoiceLineId"} :content nil}]})


(def pk-playlist-track
  {:tag :xs:unique
   :attrs {:msdata:PrimaryKey "true" :name "PK_PlaylistTrack"}
   :content [{:tag :xs:selector :attrs {:xpath ".//mstns:PlaylistTrack"} :content nil}
             {:tag :xs:field :attrs {:xpath "mstns:PlaylistId"} :content nil}
             {:tag :xs:field :attrs {:xpath "mstns:TrackId"} :content nil}]})


(def fk-album-artist
  {:tag :xs:keyref
   :attrs {:msdata:ConstraintOnly "true" :refer "PK_Artist" :name "FK_Artist_Album"}
   :content [{:tag :xs:selector :attrs {:xpath ".//mstns:Album"} :content nil}
             {:tag :xs:field :attrs {:xpath "mstns:ArtistId"} :content nil}]})


(def column-invoice-line-unit-price
  {:tag :xs:element
   :attrs {:type "xs:decimal"
           :msprop:Generator_ColumnPropNameInTable "UnitPriceColumn"
           :msprop:Generator_ColumnVarNameInTable "columnUnitPrice"
           :msprop:Generator_ColumnPropNameInRow "UnitPrice"
           :msprop:Generator_UserColumnName "UnitPrice"
           :name "UnitPrice"}
   :content nil})


(def column-customer-fax
  {:tag :xs:element
   :attrs {:minOccurs "0"
           :msprop:Generator_ColumnPropNameInTable "FaxColumn"
           :msprop:Generator_ColumnPropNameInRow "Fax"
           :msprop:Generator_ColumnVarNameInTable "columnFax"
           :msprop:Generator_UserColumnName "Fax"
           :name "Fax"}
   :content [{:tag :xs:simpleType
              :attrs nil
              :content [{:tag :xs:restriction
                         :attrs {:base "xs:string"}
                         :content [{:tag :xs:maxLength :attrs {:value "24"} :content nil}]}]}]})

