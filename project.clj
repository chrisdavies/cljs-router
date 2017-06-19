(defproject cljs-router "0.0.1-SNAPSHOT"
  :url "http://github.com/chrisdavies/cljs-router"
  :license {:name "MIT"}
  :description "A simple, order agnostic ClojureScript router"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.562"]]

  :plugins [[lein-cljsbuild "1.1.6"]
            [lein-doo "0.1.7"]
            [lein-codox "0.10.3"]]

  :profiles {:dev {:dependencies [[pjstadig/humane-test-output "0.8.2"]]}}

  :source-paths ["src-cljs"]

  :codox {:language :clojurescript
          :exclude clojure.string
          :metadata {:doc/format :markdown}
          :source-paths ["src-cljs"]}

  :cljsbuild
  {:builds {:minify {:source-paths ["src-cljs"]
                     :compiler {:optimizations :advanced
                                :pretty-print false}}

            :dev {:source-paths ["src-cljs"]
                  :compiler {:optimizations :whitespace}}

            :test {:id "test"
                   :source-paths ["src-cljs" "test"]
                   :compiler {:output-to "target/cljs-tests.js"
                              :output-dir "target"
                              :main cljs-router.runner
                              :optimizations :none
                              :target :nodejs}}}})
