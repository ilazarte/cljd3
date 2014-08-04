(defproject cljd3 "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  ; when patch is available, switch dev opt to :none and version to > :2268
  
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2268"]
                 [org.webjars/d3js "3.4.9"]
                 [garden "1.2.1"]]

  :plugins [[com.keminglabs/cljx "0.4.0"]
            [lein-cljsbuild "1.0.4-SNAPSHOT"]]

  :source-paths ["src/clj" "src/cljs"]

  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :clj}
                  
                  {:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :cljs}]}
  
  :cljsbuild {:builds {:cljd3
                       {:source-paths ["src/cljs"]
                        :compiler {:output-to "target/cljd3.js"
                                   :source-map "target/cljd3.js.map"
                                   :output-dir "target" 
                                   :optimizations :none}}}}
  
  ; :injections
  #_(defn piggieback-repl []
      (cemerick.piggieback/cljs-repl))
  #_(defn rhino-repl []
      (cljs.repl/repl (cljs.repl.rhino/repl-env)))
  
  :profiles {:dev {:dependencies [[compojure "1.1.8"]
                                  [ring "1.3.0"]
                                  [ring/ring-json "0.3.1"]
                                  [hiccup "1.0.5"]]
                   
                   :plugins [[lein-ring "0.8.11"]
                             [com.cemerick/austin "0.1.4"]]
                   
                   :source-paths ["dev/clj" "dev/cljs" "template/clj"]
                   
                   :cljsbuild {:builds {:cljd3
                                        {:source-paths ["dev/cljs"]}}}
  
                   :aliases {"js-repl"  ["trampoline" "cljsbuild" "repl-rhino"]
                             "headless" ["ring" "server-headless" "8080"]
                             "server"   ["ring" "server" "8080"]}
              
                   :ring {:handler cljx-start.core/app}}})
