(defproject foo "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2755"]
                 [org.omcljs/om "0.8.8"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [jarohen/chord "0.6.0"]]

  :node-dependencies [[source-map-support "0.2.8"]]

  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-npm "0.4.0"]
            [lein-ring "0.9.1"]]

  :source-paths ["src/clj" "target/classes"]

  :clean-targets ["out" "out-adv"]

  :ring {:handler foo.core/app}

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src/cljs"]
              :compiler {
                :main foo.core
                :output-to "resources/public/out/foo.js"
                :output-dir "resources/public/out"
                :asset-path "out"
                :optimizations :none
                :cache-analysis true
                :source-map true}}
             {:id "release"
              :source-paths ["src/cljs"]
              :compiler {
                :main foo.core
                :output-to "resources/public/out-adv/foo.min.js"
                :output-dir "resources/public/out-adv"
                :asset-path "out-adv"
                :optimizations :advanced
                :pretty-print false}}]})


