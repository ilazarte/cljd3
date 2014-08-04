(ns cljd3.core
  (:refer-clojure :exclude [remove]))

; A reasonable wrapper around d3
; See scene for a super simple hiccup like d3 enabled rendering method.
; other functions typically work on d3 selections
;
; note: select/select return an array of an arrays. see d3 docs.
; TODO bug fix the display try css from demo

(def ^:private not-nil? (complement nil?))

(def ^:private not-empty? (complement empty?))

#_(comment
  "the next few functions deal with producing the rendered svg graph via d3
   the examples below show the current reach of the api
   no transitions or data binding yet, tbd but might not need it
   caveat, if you ignore data binding now transitions impossible later"
  (level-def [:g 
              [:circle {:r 5.0}]
              [:text   {:x 9 :dy ".35em"}]]) 
  (level-def [:rect {:class  "overlay"
                     :width  "chart-width"
                     :height "chart-height"}])
  (level-def [:rect#hello.derp.man {:width  "chart-width"
                                    :height "chart-height"}])
  (level-def [:rect#hello {:class  "maybe baby"
                           :width  "chart-width"
                           :height "chart-height"}])
  (label-def [:.y.axis {:call y-axis}
              [:text {:transform "rotate(-90)"
                      :y         6
                      :dy        ".71em"
                      :style     {:text-anchor "end"}
                      :text      y-label}]]))

(defn- is-event? [key]
  (some #(= key %) ["mouseover" "mouseout" "mousemove"]))

(defn- join [sep coll]
  (apply str (interpose sep coll)))

(defn- tag-def [kw]
  "provide the initial map from the first spec component in the vector"
  (let [kwstr (name kw)]
    {:tag   (or (-> (re-seq #"^\w+" kwstr) first) "g")
     :id    (-> (re-seq #"\#(\w+)" kwstr) first second)
     :class (map second (re-seq #"\.(\w+)" kwstr))}))

(defn- level-def [spec]
  "at this spec level combine the tag map and the attr map to form a definition"
  (let [tag  (tag-def (first spec))
        maps (filter map? spec)
        attr (if (not-empty? maps)
               (let [m (first maps)]
                 (if (contains? m :class)
                   (update-in m [:class] #(re-seq #"\w+" %))
                   m))
               nil)]
    (if (not-nil? attr)
      (merge tag attr)
      tag)))

; there is a looping issue somewhere in the svg code
; it suggests that i'm missing the last iteration...

#_(loop [f (first (range 1 5)) 
       r (range 2 5)]
   (println f) 
  (if (empty? r) 
    f 
    (recur (first r) (rest r))))

(defn- render-svg! [parentsel spec]
  "invoke the d3 api using definition with the remaining keys.
   append the current tag and return it as the new selection"
  (let [def       (level-def spec)
        selection (.append parentsel (:tag def))
        tkeys     (filter #(not= :tag %) (keys def))]
    (loop [rsel  selection
           rkeys tkeys]
      (if (empty? rkeys)
        rsel
        (let [tkey    (first rkeys)
              tkeystr (clj->js tkey)
              nrsel   (cond 
                        (= tkey :class) (.attr rsel "class" (join " " (:class def)))
                        (= tkey :call)  (.call rsel (tkey def))
                        (= tkey :text)  (.text rsel (tkey def))
                        (is-event? tkey) (.on rsel (tkey def))
                        (= tkey :style) (let [smap  (:style def)
                                              skeys (keys smap)]
                                          (doseq [skey skeys]
                                            (let [skeystr (clj->js skey)] 
                                              (.style rsel skeystr (skey smap)))))
                        :else (.attr rsel tkeystr (tkey def)))] 
          (recur nrsel (rest rkeys)))))))

; 
; stupid recursive crap...
; debugged this and render-svg for hours.. thanks non-refreshing source maps!
;
; http://stackoverflow.com/questions/7813497/clojure-what-exactly-is-tail-position-for-recur
; http://stackoverflow.com/questions/1217131/recursive-doall-in-clojure
;
(defn scene [sel spec]
  (let [nsel   (render-svg! sel spec)  
        nspecs (filter vector? spec)]
    (loop [rsel   nsel
           rspecs nspecs]
      (if (empty? rspecs)
        rsel
        (recur (scene rsel (first rspecs)) (rest rspecs))))))

(defn select 
  "d3 select first wrapper" 
  ([selector]
    (js/d3.select selector))
  ([selection selector]
    (.select selection selector)))

(defn select-all
  "d3 select all wrapper"
  ([selector]
    (js/d3.selectAll selector))
  ([selection selector]
    (.selectAll selection selector)))

(defn transition [selection]
  (.transition selection))

(defn append [selection item]
  (.append selection item))

(defn size [selection]
  (.size selection))

(defn attr [selection m]
  (loop [ks  (keys m)
         sel selection]
    (if (empty? ks)
      sel
      (let [k (first ks)
            r (rest ks)
        n (name k)
        v (clj->js (k m)) 
            s (.attr selection n v)]
        (recur r s)))))

(defn style [selection m]
  (loop [ks  (keys m)
         sel selection]
    (if (empty? ks)
      sel
      (let [k (first ks)
            r (rest ks)
            n (name k)
            v (clj->js (k m))
            s (.style selection n v)]
        (recur r s)))))

(defn text [selection v]
  (.text selection v))

(defn duration [selection v]
  (.duration selection v))

(defn enter [selection]
  (.enter selection))

(defn exit [selection]
  (.exit selection))

(defn remove [selection]
  (.remove selection))

(defn data 
  ([selection]
    (.data selection))
  ([selection v]
    (.data selection (clj->js v)))
  ([selection v e]
    (.data selection (clj->js v) (clj->js e))))

(defn put-by-tag
  "Add an element if it does not exist
   The element will be added to the parent selection
   If the tag is already a child of the element
   the first result will be returned"
  [selection tagname]
  (let [sel (select selection tagname)
        el  (first (first sel))]
    (if (not-nil? el)
      el
      (-> selection 
        (.append tagname)))))

(defn put-by-id 
  "Add an element if it does not exist
   The element is given the id and added to the parent selection a
   If the id already exists, it will be returned"
  [selection tagname id]
  (let [sel (select selection (str "#" id))
        el  (first (first sel))]
     (if (not-nil? el)
      el
      (-> selection 
         (.append tagname) 
         (.attr "id" id)))))
