(defproject cljd3 "0.1.0-SNAPSHOT"
  :description "ClojureScript wrapper for d3 among other cljs svg experiments" 
  :url "http://github.com/ilazarte/cljd3"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2665"]
                 [org.webjars/d3js "3.5.2"]
                 [garden "1.2.5"]]

  :plugins [[com.keminglabs/cljx "0.5.0"]
            [lein-cljsbuild "1.0.4"]]

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
  
     
  :profiles {:dev {:dependencies [[compojure "1.3.1"]
                                  [ring "1.3.2"]
                                  [ring/ring-json "0.3.1"]
                                  [hiccup "1.0.5"]]
                   
                   :plugins [[lein-pdo "0.1.1"]
                             [lein-ring "0.8.13"]]
                   
                   :source-paths ["dev/clj" "dev/cljs" "template/clj"]
                   
                   :cljsbuild {:builds {:cljd3
                                        {:source-paths ["dev/cljs"]}}}
                   
                   :aliases {"js-repl"  ["trampoline" "cljsbuild" "repl-rhino"]
                             "headless" ["ring" "server-headless" "8080"]
                             "server"   ["ring" "server" "8080"]
                             "dev"      ["pdo" "headless," "cljsbuild" "auto"]}
                   
                   :ring {:handler       cljx-start.core/app
                          :auto-reload?  true
                          :auto-refresh? true}}})