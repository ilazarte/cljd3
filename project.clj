(defproject cljd3 "0.2.0-SNAPSHOT"
  :description "ClojureScript wrapper for d3 among other cljs svg experiments" 
  :url "http://github.com/ilazarte/cljd3"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3169"]
                 [org.webjars/d3js "3.5.3"]
                 [garden "1.2.5"]
                 [prismatic/schema "0.4.0"]
                 [figwheel "0.2.5"]]

  :plugins [[com.keminglabs/cljx "0.6.0"]
            [lein-figwheel "0.2.5"]
            [lein-cljsbuild "1.0.5"]
            [lein-pdo "0.1.1"]]

  :jar-exclusions [#"\.cljx|\.svn|\.swp|\.swo|\.DS_Store"]
  
  :source-paths ["src/cljx" "src/clj" "src/cljs"]
  
  :figwheel {:port 3449}
  
  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path  "target/classes"
                   :rules :clj}
                  
                  {:source-paths ["src/cljx"]
                   :output-path  "target/generated/classes"
                   :rules :cljs}]}
       
  :cljsbuild {:builds {:cljd3
                       {:source-paths ["target/classes" "target/generated/classes" "src/cljs"]
                        :compiler {:output-to "resources/public/js/cljd3.js"
                                     :source-map "resources/public/js/cljd3.js.map"
                                   :output-dir "resources/public/js" 
                                   :optimizations :none}}}}
  
     
  :profiles {:dev {:dependencies [[compojure "1.3.3"]
                                  [ring "1.3.2"]
                                  [ring/ring-json "0.3.1"]
                                  [hiccup "1.0.5"]]
                   
                   :plugins [[lein-pdo "0.1.1"]
                             [lein-ring "0.9.3"]]
                   
                   :source-paths ["dev/clj" "dev/cljs" "template/clj"]
                   
                   :cljsbuild {:builds {:cljd3
                                        {:source-paths ["dev/cljs"]}}}

                   :aliases {"js-repl"  ["trampoline" "cljsbuild" "repl-rhino"]
                             "once"     ["do" "cljx" "once," "cljsbuild" "once"]
                             "dev"      ["pdo" "ring" "server-headless" "8080," "cljx" "auto," "figwheel"]}
                   
                   :ring {:handler       cljx-start.core/app
                          :auto-reload?  true
                          :auto-refresh? false}}})