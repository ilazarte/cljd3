(ns cljd3.gog)

;--------------------------------------
; gog wip, not really worth watching this namespace yet
;--------------------------------------
(declare diamonds smooth log10 smooth)

(def spec-short
  [[:init  {:data diamonds
            :aes  ["caret" "price"]}]
   [:geom  :point]
   [:stat  smooth {:method "1m"}]
   [:scale {:x log10
            :y log10}]])

(def spec-long
  [[:layer {:data     diamonds
            :mapping  [:aes {:x "caret"
                             :y "price"}]
            :geom     :point
            :stat     identity
            :position identity}]
   [:layer {:data     diamonds
            :mapping  [:aes {:x "caret"
                             :y "price"}]
            :geom     :smooth
            :stat     smooth
            :position identity
            :method   "1m"}]
   [:scale {:x log10
            :y log10}]
   [:coord :cartesian]])