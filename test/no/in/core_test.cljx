(ns no.in.core-test
  #+cljs (:require-macros [cemerick.cljs.test :refer [deftest is are]])
  (:require [no.in.core :refer [url-encode]]
            #+clj [clojure.test :refer :all]
            #+cljs [cemerick.cljs.test :as t]))

(deftest test-url-encode
  (are [s expected]
    (is (= expected (url-encode s)))
    nil nil
    "" ""
    "a" "a"))
