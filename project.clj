(defproject noencore "0.3.1"
  :description "Clojure and ClojureScript fns not in core."
  :url "http://github.com/r0man/noencore"
  :author "r0man"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[commons-codec/commons-codec "1.10"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.51" :scope "provided"]]
  :aliases {"ci" ["do" ["test"] ["doo" "phantom" "test" "once"] ["lint"]]
            "lint" ["do"  ["eastwood"]]}
  :cljsbuild {:builds [{:id "test"
                        :compiler {:main 'no.en.test
                                   :optimizations :advanced
                                   :output-to "target/testable.js"
                                   :pretty-print true}
                        :source-paths ["src" "test"]}]}
  :deploy-repositories [["releases" :clojars]]
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]]
                   :plugins [[jonase/eastwood "0.2.3"]
                             [lein-cljsbuild "1.1.2"]
                             [lein-difftest "2.0.0"]
                             [lein-doo "0.1.6"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
