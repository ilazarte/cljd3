(ns cljd3.util
  #+cljs
  (:require-macros
    [cljd3.util :refer [precond]]))

(defn any
  "Return the first value identified by testing keys in order.
   user=> (any {:a 1 :b 2} :c :d)
   nil
   user=> (any {:a 1 :b 2} :c :d :b)
   2
   user=> (any {:a 1 :b 2} :c)
   nil
   user=> (any {:a 1 :b 2} :a)
   1" 
  [m & ks]
  (if (not (empty? ks))
    ((apply some-fn ks) m)))

(defn join
  "Join a collection by a seperator"
  [coll sep]
  (apply str (interpose sep coll)))

(defn rmerge
  "Merge a map recursively.
   Seq: concat
   Map: merge
   Scalar: replaced"
  [x y]
  (letfn [(fx [x y] 
            (cond
              (map? x) (merge x y)
              (seq? x) (concat x y)
              :else    y))]
    (merge-with fx x y)))

(comment
  "Append a *keys* variable to the bindings for introspection
  TODO yagni?"
  (defmacro context-old
   [bindings & forms]
   (let [kw->sym #(symbol (str "*" (name %) "*"))
         isxy    #(or (= :x %) (= :y %))
         isnotxy (complement isxy)
         others  #(filterv notxy %)
         ps      (partition 2 bindings)
         ks      (map first ps)
         vs      (map second ps)
         v       (conj (symbol *keys*) ks)])
   `(binding [~@v] forms)))

(defmacro context
  "Convert the keyword bindings into the context variable."
  [bindings & forms]
  (let [m (into {} bindings)]
    `(binding [*context* m] forms)))

; check to see if data attr is found.
; if so, use data, insert enter and then do append.
; assume identity by default for text
; automatically insert enter invocations if data is found.

(defmacro precond
  "Not needed thanks to modern browser debugging and :pre." 
  [test & args]
  (let [wrapped (map #(list 'cljs.core/clj->js %) args)] 
    `(when test
       (js/console.error ~@wrapped)
       (throw (js/Error. "Failed precondition!")))))