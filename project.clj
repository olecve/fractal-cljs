(defproject fractal-cljs "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374"
                  :exclusions [org.clojure/tools.reader]]
                 [reagent "0.5.1"]]

  :plugins [[lein-cljsbuild "1.1.2" :exclusions [[org.clojure/clojure]]]
            [lein-figwheel "0.5.0-4"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id           "dev"
                :source-paths ["src"]
                ;:figwheel     {:on-jsload "fractal.core/on-js-reload"}
                :compiler     {:main                 fractal.core
                               :asset-path           "js/compiled/out"
                               :output-to            "resources/public/js/compiled/fractal.js"
                               :output-dir           "resources/public/js/compiled/out"
                               :source-map-timestamp true}}
               {:id           "min"
                :source-paths ["src"]
                :compiler     {:output-to     "resources/public/js/compiled/fractal.js"
                               :main          fractal.core
                               :optimizations :advanced
                               :pretty-print  false}}]}

  :figwheel {:css-dirs ["resources/public/css"]})
