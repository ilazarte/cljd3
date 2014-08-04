(ns cljx-start.core
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.file         :as file]
            [ring.middleware.file-info    :as file-info]
            [ring.middleware.content-type :as content-type]
            [ring.middleware.resource     :as resource]
            [ring.middleware.json         :as json]
            [ring.util.response           :as response]
            [hiccup.page              :refer [html5 include-js include-css]]
            [cljd3.css                :as css]))

; writing middleware http://www.luminusweb.net/docs/middleware.md
; disabling cache: http://stackoverflow.com/questions/49547/making-sure-a-web-page-is-not-cached-across-all-browsers
(defn wrap-nocache [handler]
  "completely disable all cachign on the client" 
  (fn [request]
    (let [response (handler request)]
    (-> response
      (assoc-in [:headers "Cache-Control"] "no-cache, no-store, must-revalidate")
      (assoc-in [:headers "Pragma"] "no-cache")
      (assoc-in [:headers "Expires"] "0")))))

(defroutes app-routes
  
  (GET "/" [] 
       (html5 
         [:head 
          [:title "cljx-start development"]
          (include-css "/css/line-chart.css")]
         [:body 
          [:div "cljx-start loaded"]
          (include-js
            "goog/base.js"
            "/webjars/d3js/3.4.9/d3.js"
            "cljd3.js")
          [:script "goog.require('cljd3.chart_dev');"]]))
  
  (GET "/css/line-chart.css" [] {:headers {"Content-Type" "text/css"}
                                 :body (css/line)})
  
  (route/resources "/")
  
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
    (file/wrap-file "target")
    (resource/wrap-resource "/META-INF/resources")
    (file-info/wrap-file-info)
    (content-type/wrap-content-type)
    json/wrap-json-body
    json/wrap-json-params
    json/wrap-json-response
    wrap-nocache))
  