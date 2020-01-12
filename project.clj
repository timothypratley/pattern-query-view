(defproject pattern-query-view "0.1.0-SNAPSHOT"
  :description "Query, match, view patterns"
  :url "http://github.com/pattern-query-view"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.9.1"

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"]
                 [org.clojure/core.async  "0.7.559"]
                 [meander/epsilon "0.0.373"]
                 [justice "0.0.4-alpha"]
                 [datascript "0.18.8"]
                 [reagent "0.8.1"]]

  :plugins [[lein-figwheel "0.5.19"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]

                :figwheel {:open-urls ["http://localhost:3449/index.html"]}

                :compiler {:main pattern-query-view.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/pattern_query_view.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           ;; To console.log CLJS data-structures make sure you enable devtools in Chrome
                           ;; https://github.com/binaryage/cljs-devtools
                           :preloads [devtools.preload]}}
               ;; This next build is a compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/pattern_query_view.js"
                           :main pattern-query-view.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.11"]
                                  [figwheel-sidecar "0.5.19"]]
                   :source-paths ["src" "dev"]
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
