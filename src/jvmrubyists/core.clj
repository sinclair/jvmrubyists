(ns jvmrubyists.core
  (:import [org.jruby.embed ScriptingContainer LocalVariableBehavior LocalContextScope]))

(def c (ScriptingContainer. LocalContextScope/SINGLETHREAD LocalVariableBehavior/PERSISTENT))

(defn run
  [s]
  (. c runScriptlet s))

(run "Proc.new {|x| x+1}")
