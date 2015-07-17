(ns cljd3.sketch  
  (:require
    [schema.core :as s :include-macros true]
    [cljd3.core  :as core]
    [cljd3.util  :as util]))

; https://developer.mozilla.org/en-US/docs/Web/SVG/Element/marker
; https://developer.mozilla.org/en-US/docs/Web/SVG/Tutorial/Fills_and_Strokes
; start with: http://www.w3.org/TR/SVG/attindex.html

(def ^:private state (atom {}))

(def ^:private old (atom {}))

(defn- apply-attrs [sel]
  (if-let [ks (keys @state)]
    (doseq [k ks]
      (.attr sel k (k @state)))))

(defn push-state []
  (reset! old state))

(defn pop-state []
  (reset! state old))

(defn- generate-swapper [x]
  (let [kw (keyword x)]
    (fn [v] (swap! state assoc kw v))))

(def color-interpolation (generate-swapper "color-interpolation"))
(def color-interpolation-filters (generate-swapper "color-interpolation-filters"))
(def color-profile (generate-swapper "color-profile"))
(def color-rendering (generate-swapper "color-rendering"))
(def fill (generate-swapper "fill"))
(def fill-opacity (generate-swapper "fill-opacity"))
(def fill-rule (generate-swapper "fill-rule"))
(def image-rendering (generate-swapper "image-rendering"))
(def marker (generate-swapper "marker"))
(def marker-end (generate-swapper "marker-end"))
(def marker-mid (generate-swapper "marker-mid"))
(def marker-start (generate-swapper "marker-start"))
(def shape-rendering (generate-swapper "shape-rendering"))
(def stroke (generate-swapper "stroke"))
(def stroke-dasharray (generate-swapper "stroke-dasharray"))
(def stroke-dashoffset (generate-swapper "stroke-dashoffset"))
(def stroke-linecap (generate-swapper "stroke-linecap"))
(def stroke-linejoin (generate-swapper "stroke-linejoin"))
(def stroke-miterlimit (generate-swapper "stroke-miterlimit"))
(def stroke-opacity (generate-swapper "stroke-opacity"))
(def stroke-width (generate-swapper "stroke-width"))
(def text-rendering (generate-swapper "text-rendering"))
(def alignment-baseline (generate-swapper "alignment-baseline"))
(def baseline-shift (generate-swapper "baseline-shift"))
(def dominant-baseline (generate-swapper "dominant-baseline"))
(def glyph-orientation-horizontal (generate-swapper "glyph-orientation-horizontal"))
(def glyph-orientation-vertical (generate-swapper "glyph-orientation-vertical"))
(def kerning (generate-swapper "kerning"))
(def text-anchor (generate-swapper "text-anchor"))
(def writing-mode (generate-swapper "writing-mode"))

(defn selection [sel]
  (swap! state assoc :selection sel))

(defn translate [x y]
  (swap! state assoc :translate {:x x :y y}))

(defn line 
  [x1 y1 x2 y2]
  (let [x (get-in @state [:translate :x] 0)
        y  (get-in @state [:translate :y] 0)
        el (.append (:selection @state) "line")]
    
    (apply-attrs el)
    
    (-> el
      (.attr "x1" (+ x1 x))
      (.attr "y1" (+ y1 y))
      (.attr "x2" (+ x2 x))
      (.attr "y2" (+ y1 y)))))

(comment
  "something like this maybe?
   how can we limit the number of properties being set?
   this isnt meant to draw anything, more or less just sketching..."
  (do
    
    (defn move [arr]
      (context
        (selection "items")
        (data (get-x-values arr))
        (transition
          (color (map-color identity))
          (line 0 identity 0 30))))
    
    
    (defn background []
      
      (ellipse 0 50 33 33)
      
      (context
        (selection "#id")
        (translate 10 10)
        (class "el1")
        (on-click ".el1" move) 
        (ellipse 50 50 33 3)
        (line 10 10 20 20))
      
      (circle 0 10 20))))

(comment
  "sample of a chart imperatively 500x300 (w/h)
  axes is svg oriented.  tabling for now due to annoyance of api design."
  
  (defn grid [x y width length interval]
    (for [i (range 10)
          :let [x-inc (/ 500 10)
                y-inc (/ 300 10)
                x (* i x-inc)
                y (* i y-inc)]]
      (line x 0 x 300) ; vertical lines
      (line 0 y 500 y)))
  
  (def svg [:svg {:width  400
                  :height 400}
            [translate 50 30
             [:grid]]]))