(ns cljd3.core
  (:require 
    [cljd3.util :as util])
  (:refer-clojure :exclude [remove]))

; A reasonable wrapper around d3
; other functions typically work on d3 selections
;
; note: select/select return an array of an arrays. see d3 docs.
; TODO bug fix the display try css from demo

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

;TODO somewhere the dom name needs to be used.
;NOTE the create function is passed the data being bound...
;TODO deprecatng dom pooling, seems to make animation less smooth...
(def ^:private dompool (atom {}))

(defn- factory [context tag]
  (let [nameq (js/d3.ns.qualify tag)]
    (if (.-local nameq)
      (.createElementNS 
        (.-ownerDocument context) 
        (.-space nameq)
        (.-local nameq))
      (let [document (or (.-ownerDocument context) (.-document js/window))
            namespace (.-namespaceURI context)]
        (if namespace
          (.createElementNS document namespace tag)
          (.createElement document tag))))))

(defn- create [context tag]
  (let [cache (@dompool tag)]
    (cond
      (nil? cache)
      (do
        (js/console.log "NEWCACHE:" tag) 
        (swap! dompool assoc tag (array))
        (factory context tag))
      (= 0 (.-length cache))
      (do
        (js/console.log (js/window.performance.now) "empty:" tag)
        (factory context tag))
      :else
      (do
        ;(js/console.log "alloc:" tag)
        (.pop cache)))))

(defn- release [selection]
  (let [els (nth selection 0)]
    (doseq [el els]
      (when el
        (let [tag  (.-tagName el)
              cache (@dompool tag)]
          ;(js/console.log "release:" tag el)
          (.push cache el))))))

(defn- tag-def 
  "Return a map of:
   tag   - default tag string is g if none is specified
   id    - the id string specified by a # sign
   class - any class added as a vector (or empty vector)"
  [kw]
  (let [kwstr (name kw)]
    {:tag   (or (first (re-seq #"^\w+" kwstr)) "g")
     :id    (-> (re-seq #"\#(\w+)" kwstr) first second)
     :class (map second (re-seq #"\.(\w+)" kwstr))}))

(defn- level-def 
  "Combine the tag map and the attr map to form a definition"
  [spec]
  (let [tag  (tag-def (first spec))
        maps (filter map? spec)
        attr (if (not (empty? maps))
               (let [m (first maps)] 
                 (if (contains? m :class)
                   (update-in m [:class] #(re-seq #"\w+" %))
                   m)))
        def  (if (nil? attr) tag (merge tag attr))]
    def))

(defn- execute-api
  "Execute the d3 api according to the key found
   Special handling includes the following:
   class: converted to a string
   call:  invoked with value
   text:  invoked with value
   on:    each key found in map executed
   style: each key found in map executed
   default is to treat item as a simple attr k/v
   Data/datum noticeably absent in order to encourage careful placement in invocations."
  [selection k m]
  (let [v    (k m)
        kstr (name k)] 
    (condp = k
      :class (cond
               (empty? v)
               selection
               (coll? v)
               (.attr selection kstr (util/join v " "))
               (string? v)
               (.attr selection kstr v)) 
      :call  (.call selection v)
      :text  (.text selection v)
      :html  (.html selection v)
      :on    (reduce-kv #(doto %1 (.on (name %2) %3)) selection v) 
      :style (reduce-kv #(doto %1 (.style (name %2) %3)) selection v)
      :property (reduce-kv #(doto %1 (.property (name %2) %3)) selection v)
      (.attr selection kstr v))))

(defn- apply-api
  "Apply execute api, handles a super set of d3 attrs.
   :on {} event handlers
   :style {} style attributes
   :text text content
   :html html content
   :duration time
   This should only be called by the layer api, or operators.
   It's a tiny wrap around the reduce statement."
  [selection props]
  (reduce-kv #(doto %1 (execute-api %2 props)) selection props))

(defn- render-svg 
  "Append the current spec to the selection and return it as the new selection.
   Execute the d3 api against all the keys of the spec."
  [parentsel spec]
  {:pre [parentsel]}
  (let [def       (level-def spec)
        selection (.append parentsel (:tag def))
        rdef      (dissoc def :tag)]
    (apply-api selection rdef)))

; TODO what if someone invokes (layer svg [:child1 ..] [:child2 ..])
; TODO currently api only supports (layer svg [:child1 ..])
; TODO hiccup allows a string as a child... is that a innerhtml basically?
; TODO since we have svg elements which may be finicky, :text and :html
(defn layer
  "The main entrypoint to the hiccup like api.
  The process starts with a d3 selection."
  [selection spec]
  (let [selection (if (not (js/Array.isArray selection)) (js/d3.select selection) selection)
        nsel      (render-svg selection spec)  
        nspecs    (filter vector? spec)]
    (reduce #(doto %1 (layer %2)) nsel nspecs)))

(defn datum
  [selection]
  (.datum selection))

(defn select  
  ([selector]
    (js/d3.select selector))
  ([selection selector]
    (.select selection selector)))

(defn select-all
  ([selector]
    (js/d3.selectAll selector))
  ([selection selector]
    (.selectAll selection selector)))

(defn operators
  "Convenience function to set a variety of d3 attrs/styles etc on at one time.
   Some functions such as attrs, call it internally.
   Handles the occasional edge case such as reordering invocations to invoke duration first."
  [selection m]
  (if (:duration m)
    (apply-api 
      (.duration selection (:duration m)) 
      (dissoc m :duration))
    (apply-api selection m)))

(defn transition 
  ([selection]
    (.transition selection))
  ([selection m]
    (-> selection
      (.transition)
      (operators m))))

(defn append [selection item]
  (.append selection item))

(defn size [selection]
  (.size selection))

(defn attr [selection k v]
  (.attr selection k v))

(defn attrs [selection m]
  (operators selection m))

(defn style [selection k v]
  (.style selection k v))

(defn styles [selection m]
  (operators selection {:style m}))

(defn text [selection v]
  (.text selection v))

(defn duration [selection v]
  (.duration selection v))

(defn enter 
  ([selection]
    (.enter selection))
  ([selection m]
    (-> selection
      (.enter)
      (operators m)))
  ([selection tag m]
    (-> selection
      (.enter)
      (append tag)
      (operators m))))

(defn exit 
  ([selection]
    (.exit selection))
  ([selection m]
    (-> selection
      (.exit)
      (operators m))))

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
  (let [sel (select selection tagname)]
    (if-let [el  (first (first sel))] 
      el
      (-> selection 
        (.append tagname)))))

(defn put-by-id 
  "Add an element if it does not exist
   The element is given the id and added to the parent selection a
   If the id already exists, it will be returned"
  [selection tagname id]
  (let [sel (select selection (str "#" id))]
    (if-let [el (first (first sel))]
      el
      (-> selection 
        (.append tagname) 
        (.attr "id" id)))))