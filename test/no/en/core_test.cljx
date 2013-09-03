(ns no.en.core-test
  #+cljs (:require-macros [cemerick.cljs.test :refer [deftest is are]])
  (:require [no.en.core :as c]
            #+clj [clojure.test :refer :all]
            #+cljs [cemerick.cljs.test :as t]))

(deftest test-base64-encode
  (are [s expected]
    (is (= expected (c/base64-encode s)))
    nil nil
    "" ""
    "x" "eA=="))

(deftest test-base64-decode
  (are [s expected]
    (is (= expected (c/base64-decode s)))
    nil nil
    "" ""
    "eA==" "x"))

(deftest test-url-encode
  (are [s expected]
    (is (= expected (c/url-encode s)))
    nil nil
    "" ""
    "a" "a"
    " " "%20"
    "*" "%2A"
    "~" "~"))

(deftest test-url-decode
  (are [s expected]
    (is (= expected (c/url-decode s)))
    nil nil
    "" ""
    "a" "a"
    "%20" " "
    "%2A" "*"
    "~" "~"))
