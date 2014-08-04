(ns cljd3.css
  (:require [garden.core :as garden]))

(defn line
  []
  (garden/css 
    [:body {:font "10px sans-serif"}]
    [:.gup-text {:font "bold 48px monospace"}]
    [:.gup-enter {:fill "green"}]
    [:.gup-update {:fill "#333"}]
    [:.gup-exit {:fill "brown"}]
    [:.axis 
     [:line :path {:fill   "none" 
                   :stroke "#000"
                   :shape-rendering "crispEdges"}]]
    [:.x [:&.axis [:path {:display "none"}]]]
    [:.line {:fill         "none"
             :stroke       "steelblue"
             :stroke-width "1.5px"}]
    [:.overlay {:fill           "none"
                :pointer-events "all"}]
    [:.focus [:circle {:fill   "none"
                      :stroke "steelblue"}]]))