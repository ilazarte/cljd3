(ns cljd3.chart-dev
  (:require 
    [schema.core :as s :include-macros true]
    [figwheel.client :as figwheel :include-macros true]
    [cljd3.core  :as core :refer [enter 
                                  exit 
                                  transition 
                                  duration]] 
    [cljd3.chart  :as chart]
    [cljd3.gog    :as gog]
    [cljd3.sketch :as sketch]))

(enable-console-print!)

;--------------------------------------
; series
;--------------------------------------

(def ibm-series-wide-dates 
  {:key    "IBM"
   :values [{:x (js/Date. 1405332000000)
             :y 1.042}
            {:x (js/Date. 1405418400000)
             :y 1.015}
            {:x (js/Date. 1437040800000)
             :y 1.007}
            {:x (js/Date. 1437127200000)
             :y 1.242}]})

(def goog-series 
  {:key    "GOOG"
   :values [{:x (js/Date. 1405332000000)
             :y 1.042}
            {:x (js/Date. 1405418400000)
             :y 1.015}
            {:x (js/Date. 1405504800000)
             :y 1.007}
            {:x (js/Date. 1405591200000)
             :y 1.242}]})

(def aapl-series
  {:key    "AAPL"
   :values [{:x (js/Date. 1405332000000)
             :y 1.521}
            {:x (js/Date. 1405418400000)
             :y 1.390}
            {:x (js/Date. 1405504800000)
             :y 1.320}
            {:x (js/Date. 1405591200000)
             :y 1.001}]})

(def series [goog-series])

(js/console.log "loading dev code three series")

(defn test-single-line-chart
  []
  (core/put-by-id (core/select "body") "div" "single-line-chart")
  (js/console.log "dates:")
  (js/console.log (js/Date. 1405332000000))
  (js/console.log (js/Date. 1437127200000))
  (chart/line {:container "#single-line-chart"
               :series series
               :x      {:format "%m-%d-%Y"
                        :type   :datetime
                        :ticks  4
                        :label  "Date (MM-dd-yyyy)"}
               :y      {:label  "PMA (price/MA(20))"}}))

(comment (test-single-line-chart))

(defn test-d3-dsl
  []
  (let [sel  (core/put-by-id (core/select "body") "div" "d3-dsl")
        spec [:svg {:style {:width  400
                            :height 400}} 
              [:.y.axis {:derp "test y-axis"}
               [:text {:transform "rotate(-90)"
                       :y         6
                       :dy        ".71em"
                       :style     {:text-anchor "end"}
                       :text      "node 1"}]
               [:text {:transform "rotate(-45)"
                       :class     "y0 mama!" 
                       :y         6
                       :dy        ".71em"
                       :style     {:text-anchor "end"}
                       :text      "test y-label"}]]]
        res  (core/layer sel spec)]
    (js/console.log "rendered svg:")
    (js/console.log res)))

(comment (test-d3-dsl))

(comment (js/console.log "loading the general update pattern version 3!"))
;
; http://bl.ocks.org/mbostock/raw/3808234/
; http://bl.ocks.org/mbostock/3808234
;
; the test!  update pattern 3
; data has to be invoked first.
; then enter and exit can be allowed
; TODO do mutable shuffle and data function

(defn generate-table-data
  []
  (repeatedly 10 #(identity (let [ri (rand-int 10)]
                              {:num ri
                               :lbl (str "label-" ri)
                               :lbl2 (str "label2- ri")}))))

(defn test-table
  [data columns]
  (let [data (generate-table-data)
        rows nil
        div  (core/layer 
               (core/select "div")
               [:table {:width 600}
                [:thead nil]
                [:tbody nil]])
        render (fn [data]
                 (let [cols (keys (first data))
                       thead (core/select div "thead")
                       tbody (core/select div "tbody")]
                   (core/layer thead
                     [:tr
                      [:th {:data cols}]]
                     )))]
    [:body 
     [:table {:style {:margin-left "250px"}}
      [:thead
       [:tr
        [:th {:data columns}]]]
      [:tbody
       [:td {:data rows}
       ]]]]))

(defn test-general-update-pattern-3 []
  (let [alphabet (re-seq #"\w" "abcdefghijklmnopqrstuvwxyz")
        width    960
        height   500
        height2  (/ height 2)
        bodysel  (core/select "body")
        svg      (core/layer 
                   bodysel
                   [:svg.container {:width  width
                                    :height height}
                    [:g {:transform (str "translate(32," height2 ")")}]])
        by-32    (fn [d i] (* i 32))
        update   (fn [udata]
                   (let [g        (core/select svg "g")
                         textsel  (-> g
                                    (core/select-all "text")
                                    (core/data udata identity))]
                     (-> textsel
                       (core/operators {:class "gup-update gup-text"})
                       (core/transition {:duration 750
                                         :x        by-32}))
                     (-> textsel
                       (core/enter "text" {:class "gup-enter gup-text"
                                           :dy    ".35em"
                                           :y      60
                                           :x      by-32
                                           :text   identity
                                           :style  {:fill-opacity 0.000001}})
                       (core/transition {:duration 750
                                         :y        0
                                         :style    {:fill-opacity 1}}))
                     (-> textsel
                       (core/exit {:class "gup-exit gup-text"})
                       (core/transition {:duration 750
                                         :y        60
                                         :style    {:fill-opacity 0.000001}}) 
                       (core/remove))))]
    (update alphabet)
    
    (comment
      "sketch out a combined format, seems impossible from this angle.
       get more practical first, and find use for such a thing.")
    
    (js/setInterval 
      #(update (-> (shuffle alphabet)
                 (clj->js)
                 (.slice 0 (js/Math.floor (* (js/Math.random) 26)))
                 (.sort))) 
      1500)))

(comment (test-general-update-pattern-3))

;TODO create some simple helpers which would assist in drawing a chart.
;N dimension structures can be created via mapping (table (map row data) (row (map column cols)) etc
;Binding is accomplished via enter/exit pattern (gup above)
;How can we make drawing of charts easier?
; TODO date formatted axis
; TODO simple grids of lines
; layer reordering if necessary
;

(defn wrong-way-line [sel]
  (let [step  10
        lastx -999
        lasty -999
        y     50
        borderx 20
        bordery 10]
    (loop [x borderx]
      (when (> (- width borderx) x)
        (let [y 3])
        (recur (+ x step))))))

(defn draw-primitives []
  (let [width  500
        height 500
        body (core/select "body")
        svg  (core/layer 
               bodysel
               [:svg.container {:width  width
                                :height height} nil])]
    
    
    
    
    
    ))

;-------------------------------------------------------
; MAIN
;-------------------------------------------------------

(defn start []
  (draw-primitives))

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :jsload-callback #(start))

(start)