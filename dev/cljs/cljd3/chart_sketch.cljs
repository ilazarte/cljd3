(ns cljd3.chart_sketch
  (:require [cljd3.core  :as core] 
            [cljd3.chart :as chart]
            [cljd3.gog   :as gog]))



(defn test-d3-dsl
  []
  (let [sel  (core/put-by-id (core/select "body") "div" "d3-dsl")
        spec [:svg
              [:.y.axis {:derp "test y-axis"}
               [:text {:transform "rotate(-90)"
                       :y         6
                       :dy        ".71em"
                       :style     {:text-anchor "end"}}
                "test y-label"]
               [:text {:transform "rotate(-45)"
                       :class     "y0 mama!" 
                       :y         6
                       :dy        ".71em"
                       :style     {:text-anchor "end"}}
                "test y-label"]]]
        res  (core/layer sel spec)]
    (js/console.log "rendered svg:")
    (js/console.log res)))

(comment (test-d3-dsl))

; since functions can have arbitrary args,
; present function invocations as children of the tag being selected
; they will only contain their parameters
; so the first question for rendering, is this a tag name or a function invocation.
; uh oh, where is the overlap?
; how are <text> and .text different?

(defn test-d3-dsl-scene []
  (let [svg     nil
        height   500
        alphabet (re-seq #"\w" "abcdefghijklmnopqrstuvwxyz")
        by-32    (fn [d i] (* i 32))
        update2  (fn [udata] 
                   (core/layer 
                     (core/select "body")
                     [:svg.container {:width  960
                                      :height height}
                      [:g {:transform (str "translate(32," (/ height 2) ")")}
                       [:text 
                        [:data udata identity]]]]))
        update   (fn [udata]
                   (let [textsel  (-> svg
                                    (core/select-all "text")
                                    (core/data udata identity))]
                     (-> textsel
                       (core/attr {:class "gup-update gup-text"})
                       (core/transition)
                       (core/duration 750)
                       (core/attr {:x by-32}))
                     
                     (-> textsel
                       (core/enter)
                       (core/append "text")
                       (core/attr {:class "gup-enter gup-text"
                                   :dy    ".35em"
                                   :y     -60
                                   :x     by-32})
                       (core/style {:fill-opacity 0.000001})
                       (core/text  identity)
                       (core/transition)
                       (core/duration 750)
                       (core/attr     {:y 0})
                       (core/style    {:fill-opacity 1}))
                     
                     (-> textsel
                       (core/exit)
                       (core/attr {:class "gup-exit gup-text"})
                       (core/transition)
                       (core/duration 750)
                       (core/attr {:y 60})
                       (core/style {:fill-opacity 0.000001})
                       (core/remove))))]
    (update alphabet)
    
    (js/setInterval 
      #(update (-> (shuffle alphabet)
                 (clj->js)
                 (.slice 0 (js/Math.floor (* (js/Math.random) 26)))
                 (.sort))) 
      1500)))

(comment (test-d3-dsl-scene))