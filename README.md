# Chinook Database in Clojure / EDN

Chinook is a sample database available for database engines. This is a version of the data
easily accessible as Clojure/EDN data.

The Chinook data model represents a digital media store, including tables for artists, albums, media tracks, invoices and customers. It has relationships in the form of `1..1`, `0..*`, `*..*` and also a recursive structure in the `employee` table.

To read more about the Chinookdatabase, see
https://github.com/lerocha/chinook-database

An overview and description/visualizations of the schema can be found at https://www.sqlitetutorial.net/sqlite-sample-database/ Note: for some reason the table names are pluralized in the images (Artists, Albums), but they are in singular (Artist, Album)in the original database.


## Chinook in a var

There are two predefined vars with data, one is a map with vectors and on is a map with sets.

### map with vectors

The var containing a map of vectors is called `chinook/edn` and looks like this:

```clojure
{:artist [{:artist/artist-id 1 :artist/name "AC/DC"}
          {:artist/artist-id 2 :artist/name "Accept"}
          {:artist/artist-id 3 :artist/name "Aerosmith"}
          ...]
 :album [{:album/album-id 1 :album/title "For Those About To Rock We Salute You" :album/artist-id 1}
         {:album/album-id 2 :album/title "Balls to the Wall" :album/artist-id 2}
         {:album/album-id 3 :album/title "Restless and Wild" :album/artist-id 2}
         ...]
 :track [{:track/media-type-id 1 :track/milliseconds 343719 :track/bytes 11170334 ...}
         {:track/media-type-id 2 :track/milliseconds 342562 :track/bytes 5510424 ...}
         {:track/media-type-id 2 :track/milliseconds 230619 :track/bytes 3990994 ...}
         ...]}
```

### map with sets

The var with a map of sets is called `chinook/edn-with-sets` and looks like this:

```clojure
{:artist #{{:artist/artist-id 36 :artist/name "O Rappa"}
           {:artist/artist-id 205 :artist/name "Chris Cornell"}
           {:artist/artist-id 59 :artist/name "Santana"}
           ...}
 :album #{{:album/album-id 109 :album/title "Rock In Rio [CD2]" :album/artist-id 90}
          {:album/album-id 99 :album/title "Fear Of The Dark" :album/artist-id 90}
          {:album/album-id 207 :album/title "Mezmerize" :album/artist-id 135}
          ...}
 :track #{{:track/media-type-id 1 :track/milliseconds 389276 :track/bytes 13022833 ...}
          {:track/media-type-id 1 :track/milliseconds 234893 :track/bytes 7709006 ...}
          {:track/media-type-id 1 :track/milliseconds 401319 :track/bytes 13224055 ...}
          ...}}
```

### Some other shape

If you need another shape of the data, you can of course easily transform to what you want from these vars. However, you can call `chinook/make-edn` to get data in many other shapes. `chinook/make-edn` will use `next.jdbc` to fetch table by table from the SQLite version of the database in the repo, and merge it into a map.

The `opts` can contain `:chinook/key-fn` which should be a function that shapes the map key, and the rest of the options are passed to `next.jdbc/execute!` to shape the table data in the way you want. To read more about the result set builder options, see [RowBuilder and ResultSetBuilder](https://github.com/seancorfield/next-jdbc/blob/develop/doc/result-set-builders.md) in the `next.jdbc` documentation.

Here is an example:

```clojure
(require
  '[camel-snake-kebab.core :refer [->PascalCase]]
  '[chinook])

(chinook/make-edn ds [] {:builder-fn rs/as-unqualified-lower-maps
                         :chinook/key-fn (comp keyword ->PascalCase)})
```

which will return a result shaped like this:

```clojure
{:InvoiceLine [{:invoicelineid 1 :invoiceid 1 :trackid 2 ...}
               {:invoicelineid 2 :invoiceid 1 :trackid 4 ...}
               {:invoicelineid 3 :invoiceid 2 :trackid 6 ...}
               ...]
 :Genre [{:genreid 1 :name "Rock"} {:genreid 2 :name "Jazz"} {:genreid 3 :name "Metal"} ...]
 :PlaylistTrack [{:playlistid 1 :trackid 3402}
                 {:playlistid 1 :trackid 3389}
                 {:playlistid 1 :trackid 3390}
                 ...]
 ...}

```

## Schema

The schema data is extracted from the Chinook XML-file and turned into a clojure datastructure. (Reading the schema it from SQLits gives too little data unfortunately, and involving a non-im-memory database felt out of scope). The XML-file is copied into the `resources` folder of this repo. The var is `chinook/schema` and looks like this:


```clojure
{:table [{:table/name "Genre"}
         {:table/name "MediaType"}
         {:table/name "Artist"}
         {:table/name "Album"}
         ...]
 :column [{:column/table-name "Genre" :column/name "GenreId" :column/type "integer"}
          {:column/table-name "Genre" :column/name "Name" :column/type "string"}
          {:column/table-name "MediaType" :column/name "MediaTypeId" :column/type "integer"}
          {:column/table-name "MediaType" :column/name "Name" :column/type "string"}
          ...]
 :key [{:key/name "PK_Genre"
        :key/primary? true
        :key/table-name "Genre"
        :key/column-names ["GenreId"]}
       {:key/name "PK_MediaType"
        :key/primary? true
        :key/table-name "MediaType"
        :key/column-names ["MediaTypeId"]}
       ...]
 :fk [{:foreign-key/name "FK_Track_PlaylistTrack"
       :foreign-key/refer "PK_Track"
       :foreign-key/table-name "PlaylistTrack"
       :foreign-key/column-names ["TrackId"]}
      {:foreign-key/name "FK_Playlist_PlaylistTrack"
       :foreign-key/refer "PK_Playlist"
       :foreign-key/table-name "PlaylistTrack"
       :foreign-key/column-names ["PlaylistId"]}
      ...]}
```

## deps.edn

Most (all?) use cases for Chinook is to have well prepared test data,
which means you probably want to add `chinook-edn` to your dev or test
alias only.

```clojure
{:aliases
 {:dev {:extra-deps
        {fooheads/chinook-edn {:mvn/version "0.1.4"}}}}}
```

## License

Ths code in this repo is distributed under the Eclipse Public License, the same as Clojure.

The original data comes from https://github.com/lerocha/chinook-database/ and has the following license:
https://github.com/lerocha/chinook-database/blob/master/LICENSE.md

