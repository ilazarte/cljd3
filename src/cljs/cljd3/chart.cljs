(ns cljd3.chart
  (:require [cljd3.core :refer [select select-all style]]))

; TODO create the schema validations!
; TODO add simple horizontal guide lines!
; TODO allow for dated guide lines!
; TODO allow for configurable mouseover on svg!
; x/type can be either :int :float :date :date-time :time

(def ^:private line-defaults 
  {:margin {:top    20
            :right  80
            :bottom 30
            :left   50} 
   :height 300
   :width  300
   :interpolate "linear"
   :x      {:label     "X Axis"
            :orient    "bottom"
            :type      :int
            :ticks     5
            :mouseover nil
            :format    nil}
   :y      {:label     "Y Axis"
            :orient    "left"
            :type      :float
            :ticks     5
            :format    nil}})

; there has to be something preexisting for this
; user=> (any-of {:a 1 :b 2} :c :d)
; nil
; user=> (any-of {:a 1 :b 2} :c :d :b)
; 2
; user=> (any-of {:a 1 :b 2} :c)
; nil
; user=> (any-of {:a 1 :b 2} :a)
; 1
(defn- any-of 
  [map & keys]
  (let [ks (flatten keys)]
    (if (empty? ks) 
      nil
      (let [key (first ks)]
        (if (contains? map key)
          (key map)
          (recur map (rest ks)))))))

; options is mostly the type of line-defaults
; additional
; container (id/dom node ref/d3 selection)
; series    
;    container (the selector of the parent div)
;    vector containing map.  
;    each map has key, label, and values vector
;    label is defaulted to key
;    the key is unique across all
;    the label is a text string to display
;    each elem in values has x and y val
;
; do not confuse series label vs axis label!
; 
; http://bl.ocks.org/mbostock/3884955
; http://bl.ocks.org/mbostock/raw/3884955/
;
(defn line
  [options]
  (let [not-nil?     (complement nil?)
        cil?         #(and (contains? %1 %2) (not-nil? (%2 %1)))
        is-time?     #(or (= :date %) (= :date-time %) (= :time %))
        get-map      #(merge (% line-defaults) (% options))
        get-val      #(or (% options) (% line-defaults))
        get-val-fmt  #(let [type   (:type %)
                            fmtstr (:format %)
                            fx     (if (is-time? type) js/d3.time.format js/d3.format)]
                        (if (not-nil? fmtstr) (fx fmtstr) identity))
        get-scale    #(if (is-time? (:type %)) (js/d3.time.scale) (js/d3.scale.linear))
        get-vals     #(map %2 (:values %1))
        margin  (get-map :margin)
        height  (get-val :height)
        width   (get-val :width)
        x       (get-map :x)
        y       (get-map :y)
        x-mo    (:mouseover x)
        x-label (:label x)
        y-label (:label y)
        top     (:top margin)
        left    (:left margin)
        right   (:right margin)
        bottom  (:bottom margin)
        chart-height (- height top bottom)
        chart-width  (- width left right)
        interpolate  (get-val :interpolate)
        color-scale  (js/d3.scale.category10)
        x-fmt   (get-val-fmt x)
        x-scale (-> (get-scale x)
                  (.range (clj->js [0 chart-width])))
        y-scale (-> (get-scale y)
                  (.range (clj->js [chart-height 0])))
        x-axis  (-> (js/d3.svg.axis)
                  (.scale x-scale)
                  (.ticks (:ticks x))
                  (.tickFormat (get-val-fmt x))
                  (.orient (:orient x)))
        y-axis  (-> (js/d3.svg.axis)
                  (.scale y-scale)
                  (.ticks (:ticks y)) 
                  (.orient (:orient y)))
        line    (-> (js/d3.svg.line)
                  (.interpolate interpolate)
                  (.x #(x-scale (clj->js (.-x %))))
                  (.y #(y-scale (clj->js (.-y %)))))
        container (:container options)
        svg       (-> (select container)
                    (.append "svg")
                    (.attr "width" width)
                    (.attr "height" height)
                    (.append "g")
                    (.attr "transform" (str "translate(" left "," top ")")))
        series     (:series options)
        keys       (map :key series)
        all-x      (get-vals (first series) :x)
        all-x-last (- (count all-x) 1)
        all-y      (mapcat #(get-vals % :y) series)]
    
    (.domain color-scale (clj->js (map :key series)))                                                    
                                                        
    (.domain x-scale (js/d3.extent (clj->js all-x)))
    
    (.domain y-scale (js/d3.extent (clj->js all-y)))
    
    (-> svg
      (.append "g")
      (.attr "class" "x axis")
      (.attr "transform" (str "translate(0," chart-height ")"))
      (.call x-axis))
        
    (-> svg
      (.append "g")
      (.attr "class" "y axis")
      (.call y-axis)
      (.append "text")
      (.attr "transform" "rotate(-90)")
      (.attr "y" 6)
      (.attr "dy" ".71em")
      (.style "text-anchor" "end")
      (.text y-label))
    
    ;
    ; mouseidx function finds the closest index
    ; iterate over the series and place values on their y values
    ; idea behind mouseidx is find the closest index
    ; somehow date arithmetic worked in the date sample... look into it
    ;   
    
    (let [series->cls #(-> (:key %) (str "-overlay"))
          plot        (-> (.selectAll svg ".plot")
                        (.data (clj->js series))
                        (.enter)
                        (.append "g")
                        (.attr "class" "plot"))
          clamp       (fn [x min max] (cond (< x min) min
                                            (> x max) max
                                            :else x))
          mouseidx    #(let [xy  (.mouse js/d3 %)
                             x0  (.invert x-scale (nth xy 0))
                             i   (js/d3.bisect (clj->js all-x) x0 1)
                             ic  (clamp i 0 all-x-last)
                             imc (clamp (- i 1) 0 all-x-last)
                             s0  (nth all-x imc)
                             s1  (nth all-x ic)
                             sx  (if (> (- x0 s0) (- s1 x0)) ic imc)]
                         (comment (js/console.log (str "i:" i " sx:" sx))) 
                         sx)
          mousemove   #(let [this (js* "this")
                             idx  (mouseidx this)
                             xval (nth all-x idx)
                             xscl (x-scale xval)
                             xovr (select ".x-overlay")
                             xftt (x-fmt xval)
                             mdata (for [s series 
                                         :let [key (:key s)
                                               val (nth (get-vals s :y) idx)]]
                                     {:key key 
                                      :y   val})]
                         
                         (when x-mo
                           (x-mo mdata))
                         (-> xovr
                           (.attr "transform" (str "translate(" xscl "," chart-height ")")))
                         (-> xovr
                           (.select "text")
                           (.text   (str xftt)))
                         
                         ; TODO the new mouseover data above might obviate some code below
                         
                         (doseq [s series]
                           (let [selector (str "." (series->cls s))
                                 key      (:key s)
                                 yvals    (get-vals s :y)
                                 yval     (nth yvals idx)
                                 yscl     (y-scale yval)
                                 node     (select selector)]
                             (-> node
                                 (.attr "transform" (str "translate(" xscl "," yscl ")")))
                             (-> node
                                 (.select "text")
                                 (.text   (str key " " yval))))))]
      
      ;
      ; draw the lines
      ;       
      (-> plot
        (.append "path")
        (.attr "class" "line")
        (.attr "d" #(line (clj->js (.-values %))))
        (.style "stroke" #(color-scale (clj->js 
                                         (cond
                                           (not-nil? (.-label %)) (.-label %)
                                           (not-nil? (.-key %)) (.-key %)
                                           :else nil)))))
      
      (-> plot
        (.append "text")
        (.datum #(identity {:label (any-of % :label :key)
                            :value (last (:values %))}))
        (.attr "transform"
          #(str 
             "translate(" 
             (x-scale (-> (:value %) :x))
             ","
             (y-scale (-> (:value %) :y))
             ")"))
        (.attr "x" 3)
        (.attr "dy" ".35em")
        (.text #(:label %)))
      
      ;
      ; render the place holders for the text values
      ; add general plot-overlay class to all overlays
      ; dont mix this up with the .overlay rect which captures the events
      ; its also targeted in styles
      ; 
      
      #_(scene svg [:g {:class (series->cls s)
                       :style {:display "none"}}
                   [:text {:x 9
                           :dy ".35em"}]])
      
      ; add an overlay just for the x axis
      
      (-> svg
        (.append "g")
        (.attr   "class" "x-overlay  plot-overlay")
        (.style  "display" "none")
        (.append "text")
        (.attr   "x" 9)
        (.attr   "dy" ".35em"))
      
      (doseq [s series]
        (let [focus (.append svg "g")]
          
          (-> focus
            (.attr "class" (str (series->cls s) " plot-overlay"))
            (.style "display" "none"))
          
          (-> focus
            (.append "text")
            (.attr "x" 9)
            (.attr "dy" ".35em"))))
      
      (-> svg
        (.append "rect")
        (.attr "class" "overlay")
        (.attr "width" chart-width)
        (.attr "height" chart-height)
        (.on "mouseover" #(-> (select-all ".plot-overlay") (.style "display" nil)))
        (.on "mouseout"  #(-> (select-all ".plot-overlay") (.style "display" "none")))
        (.on "mousemove" mousemove)))))
