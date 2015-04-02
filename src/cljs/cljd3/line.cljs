(ns cljd3.line
  (:require 
    [schema.core :as s :include-macros true]
    [cljd3.util  :as util]))

(def Data
  "expected form of the values to chart"
  {:key    s/Str
   :label  s/Str
   :values [{:x s/Any
             :y s/Any}]})

(def Axis
  "A configuration for a specific axis"
  {(s/optional-key :label)  s/Str
   (s/optional-key :format) s/Str
   (s/optional-key :time)   s/Bool})

(def Config
  "complete configuration for a line chart"
  {:element   s/Any
   :data      [Data]
   (s/optional-key :x)      Axis
   (s/optional-key :y)      Axis
   (s/optional-key :width)  s/Int
   (s/optional-key :height) s/Int
   (s/optional-key :margin) {(s/optional-key :top) s/Int
                             (s/optional-key :right) s/Int
                             (s/optional-key :bottom) s/Int
                             (s/optional-key :left) s/Int}
   (s/optional-key :forceY)    s/Bool
   (s/optional-key :guideline) s/Bool
   (s/optional-key :useDates)  s/Bool
   (s/optional-key :duration)  s/Int})

(def ^:private Defaults
  "default values for configurable values"
  {:margin {:top    10
            :right  10
            :bottom 10
            :left   10}
   :x         {:label nil
               :time  false}
   :y         {:label nil
               :time  false}
   :forceY    false
   :guideline true
   :useDates  false
   :duration  500})

(defn- make-formatter [axis]
  (if (= (:time axis) true)
    (js/d3.time.format (:format axis))
    (js/d3.format      (:format axis))))

(defn- configure-axis [ref cfg]
  (if-let [label (:label cfg)]
      (.axisLabel ref label))
  (if-let [format (:format cfg)]
      (.tickFormat ref (make-formatter cfg))))

; based on lineChartTest.html chart 10 from nvd3 examples
; what is the update model?
(comment
  (defn chart [cfg]
   "return a function which may be invoked multiple times"
   (s/validate Config (util/rmerge Defaults cfg))
   (let [options   (util/rmerge Defaults cfg)
         el        (:element options)
         target    (if (string? (type el)) (str "#" el) el)]
     (js/nv.addGraph 
       (fn [] 
         (let [model (js/nv.models.lineChart)
               chart (.useInteractiveGuideline model (:guideline options))]
           (.x chart (fn [d i] (.-x d)))
           (if-let [w (:width options)] (.width chart w))
           (if-let [h (:height options)] (.height chart h))
           (if (:forceY options) (.forceY chart (clj->js [0])))
           (.margin chart (clj->js (:margin options)))
           (configure-axis (.-xAxis chart) (:x options))
           (configure-axis (.-yAxis chart) (:y options))
           (-> (js/d3.select target)
             (.datum (clj->js (:data options)))
             (.transition)
             (.duration (:duration options))
             (.call chart))
           (js/nv.utils.windowResize (.-update chart))
           chart))))))
