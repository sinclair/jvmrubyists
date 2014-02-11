(defproject jvmrubyists "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.jruby/jruby-complete "1.7.10"]
                 [org.clojure/tools.nrepl "0.2.3"]]

  :repl-options {:nrepl-middleware [jvmrubyists.piggieback/wrap-jruby-repl]}

  :plugins [[lein-nodisassemble "0.1.2"]]
  )
