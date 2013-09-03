(defproject noincore "0.2.9-SNAPSHOT"
  :description "Clojure and ClojureScript fns not in core."
  :url "http://github.com/r0man/noincore"
  :author "Roman Scherer"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1853"]
                 [com.cemerick/clojurescript.test "0.0.4"]]
  :profiles {:dev {:dependencies [[com.cemerick/clojurescript.test "0.0.4"]
                                  [com.keminglabs/cljx "0.3.0"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl
                                                     cljx.repl-middleware/wrap-cljx]}}}
  :plugins [[com.keminglabs/cljx "0.3.0"]
            [lein-cljsbuild "0.3.2"]]
  :hooks [cljx.hooks leiningen.cljsbuild]
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

  :cljsbuild {:test-commands {"phantom" ["runners/phantomjs.js" "target/testable.js"]}
              :builds [{:source-paths ["target/classes" "target/test-classes"]
                        :compiler {:output-to "target/testable.js"
                                   :libs [""]
                                   :optimizations :advanced
                                   :pretty-print true}}]})
