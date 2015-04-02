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

(defmacro precond
  "Not needed thanks to modern browser debugging and :pre." 
  [test & args]
  (let [wrapped (map #(list 'cljs.core/clj->js %) args)] 
    `(when test
       (js/console.error ~@wrapped)
       (throw (js/Error. "Failed precondition!")))))