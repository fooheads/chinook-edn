(ns build
  "from https://kozieiev.com/blog/packaging-clojure-into-jar-uberjar-with-tools-build/"
  (:require
    [clojure.string :as str]
    [clojure.tools.build.api :as b]
    [deps-deploy.deps-deploy :as dd]))


(def github-ref (or (System/getenv "GITHUB_REF") "refs/UNKNOWN"))
(def github-repository (System/getenv "GITHUB_REPOSITORY"))
(def clojars-group (System/getenv "CLOJARS_GROUP"))
(def build-folder "target")
(def jar-content (str build-folder "/classes"))
(def basis (b/create-basis {:project "deps.edn"}))


(assert github-repository "GITHUB_REPOSITORY must be set")
(assert clojars-group "CLOJARS_GROUP must be set")

(defn get-version
  []
  (cond-> (last (str/split github-ref #"/"))
    (not (str/starts-with? github-ref "refs/tags/"))
    (str "-SNAPSHOT")))


(def repo-name (last (str/split github-repository #"/")))
(def lib-name (symbol clojars-group repo-name)) 
(def jar-file-name (format "%s/%s-%s.jar" build-folder (name lib-name) (get-version)))


(defn clean
  [_]
  (b/delete {:path build-folder})
  (println (format "Build folder \"%s\" removed" build-folder)))


(defn jar
  [_]
  (clean nil)

  (b/copy-dir {:src-dirs   ["src" "resources"]
               :target-dir jar-content})

  (b/write-pom {:class-dir jar-content
                :lib       lib-name
                :version   (get-version)
                :basis     basis
                :src-dirs  ["src"]})

  (b/jar {:class-dir jar-content
          :jar-file  jar-file-name})
  (println (format "Jar file created: \"%s\"" jar-file-name)))


(defn publish
  [_]
  (jar nil)

  (dd/deploy {:installer :remote
              :artifact jar-file-name
              :pom-file (b/pom-path {:lib lib-name
                                     :class-dir jar-content})}))

