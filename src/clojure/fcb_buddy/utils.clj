(ns fcb-buddy.utils
  (:import [java.lang.reflect Method]))

(defn resource-path
  [path]
  (let [root "resources/"]
    (str root path)))

(defn from-repl?
  "Detects if a block is being evaluated in a REPL."
  []
  (boolean (System/getProperty "clojure.main/repl-requires")))

(defn filter-by-prefix
  "Filter elements of the map m by matching the key to a beginning prefix key.
  Note: Will not match namespaced keywords as prefixes.
  Example: (filter-by-prefix {:apple-fuji 1 :apple-granny-smith 3 :orange-satsuma 4} :apple)"
  [m prefix]
  (filter (fn [[k _]]
            (re-find (re-pattern (str "^" (name prefix))) (name k)))
          m))
