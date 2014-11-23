(defproject noencore "0.1.19-SNAPSHOT"
  :description "Clojure and ClojureScript fns not in core."
  :url "http://github.com/r0man/noencore"
  :author "r0man"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[commons-codec/commons-codec "1.10"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371" :scope "provided"]]
  :cljx {:builds [{:source-paths ["src"]
                   :output-path "target/classes"
                   :rules :clj}
                  {:source-paths ["src"]
                   :output-path "target/classes"
                   :rules :cljs}
                  {:source-paths ["test"]
                   :output-path "target/test-classes"
                   :rules :clj}
                  {:source-paths ["test"]
                   :output-path "target/test-classes"
                   :rules :cljs}]}
  :deploy-repositories [["releases" :clojars]]
  :prep-tasks [["cljx" "once"]]
  :profiles {:dev {:plugins [[com.cemerick/austin "0.1.5"]
                             [org.clojars.cemerick/cljx "0.5.0-SNAPSHOT" :exclusions [org.clojure/clojure]]
                             [com.cemerick/clojurescript.test "0.3.1"]
                             [lein-cljsbuild "1.0.3"]]
                   :hooks [leiningen.cljsbuild]
                   :cljsbuild {:test-commands {"node" ["node" :node-runner "target/testable.js"]
                                               "phantom" ["phantomjs" :runner "target/testable.js"]}
                               :builds [{:source-paths ["target/classes" "target/test-classes"]
                                         :compiler {:output-to "target/testable.js"
                                                    :optimizations :advanced
                                                    :pretty-print true}}]}
                   :repl-options {:nrepl-middleware [cljx.repl-middleware/wrap-cljx]}
                   :test-paths ["target/test-classes"]}
             :test {:prep-tasks [["cljsbuild" "once"]]}})
