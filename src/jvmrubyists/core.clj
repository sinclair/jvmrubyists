(ns jvmrubyists.core
  (:require [clojure.reflect :as reflect]
            [clojure.string :as s]
            [clojure.core.reducers :as reducers]
            [no.disassemble])
  (:import [org.jruby.embed ScriptingContainer LocalVariableBehavior LocalContextScope]))

(defmacro benchmark
  [expr]
  `(do (println "Benchmarking:" '~expr)
     (dotimes [_# 20]
       (time ~expr))))

(defn format-method
  [{:keys [name return-type parameter-types]}]
  (println return-type name (str "(" (s/join ", " parameter-types) ")" ";")))

(defn methods
  [obj]
  (->> (reflect/reflect obj)
       :members
       (filter (partial instance? clojure.reflect.Method))
       (sort-by :name)
       (mapv format-method))
  nil)

(defmacro dbg[x] `(let [x# ~x] (println '~x "=" x#) x#))

(defn disassemble [x]
  (println (no.disassemble/disassemble x)))

(defn types
  [obj]
  (supers (if (instance? java.lang.Class obj)
            obj
            (class obj))))

(defn safe-println
  [& args]
  (locking #'safe-println
    (apply println args)))

(comment 

;;; Interop
(def al (new java.util.ArrayList [1 2 3 4 5]))
(class al)
(types al)
(methods al)
(.add al 6)
al

(def the-vector [1 2 3 4 5])
(class the-vector)
(types the-vector)

(methods clojure.lang.PersistentVector)
;;; Concurrency
(def input (vec (range 10000000)))

(benchmark (reduce + input))

(benchmark (reducers/reduce + input))

(benchmark (reducers/fold 1024 + + input))
;; also works: (benchmark (reducers/fold + input))


;;; Out of order execution
(time (let [evens (future
                    (Thread/sleep 100)
                    (doseq [x (filter even? (range 100))]
                      (safe-println x)))
            odds (future
                   (Thread/sleep 100)
                   (doseq [x (filter odd? (range 100))]
                     (safe-println x)))]))

;;; JRuby/Clojure fun
(def c (ScriptingContainer. LocalContextScope/SINGLETHREAD LocalVariableBehavior/PERSISTENT))

(defn run
  [s]
  (. c runScriptlet s))

(def ruby (.getRuntime c))

(defprotocol Conv
  (to-ruby [x] "Wrap an object to a ruby object")
  (from-ruby [x] "Bring an object back to java"))

(extend-protocol Conv
  Number
  (to-ruby [x] (org.jruby.RubyFixnum. ruby x))
  org.jruby.RubyFixnum
  (to-ruby [x] x)
  (from-ruby [x] (.getLongValue x)))

(def rb+ (run "Proc.new {|x,y| x+y}"))

(defn ruby-add
  ([] 0)
  ([a b]
     (-> rb+
         (.call (org.jruby.runtime.ThreadContext/newContext ruby)
                (into-array [(to-ruby a) (to-ruby b)]))
         from-ruby)))

(benchmark (reduce ruby-add 0 (range 10)))
(benchmark (reduce + 0 (range 10)))

(benchmark (reducers/fold 1024 ruby-add ruby-add input))



)
