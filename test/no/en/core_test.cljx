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

(deftest test-parse-query-params
  (are [s expected]
    (is (= expected (c/parse-query-params s)))
    nil nil
    "" {}
    "a=1" {:a "1"}
    "a=1&b=2&c=%2A" {:a "1" :b "2" :c "*"}))

(deftest test-parse-url
  (let [spec (c/parse-url "mysql://localhost/example")]
    (is (= :mysql (:scheme spec)))
    (is (= "localhost" (:server-name spec)))
    (is (= 3306 (:server-port spec)))
    (is (= "/example" (:uri spec))))
  (let [spec (c/parse-url "postgresql://tiger:scotch@localhost/example?a=1&b=2")]
    (is (= :postgresql (:scheme spec)))
    (is (= "tiger" (:user spec)))
    (is (= "scotch" (:password spec)))
    (is (= "localhost" (:server-name spec)))
    (is (= 5432 (:server-port spec)))
    (is (= "/example" (:uri spec)))
    (is (= "a=1&b=2" (:query-string spec)))
    (is (= {:a "1", :b "2"} (:query-params spec))))
  (let [spec (c/parse-url "rabbitmq://tiger:scotch@localhost")]
    (is (= :rabbitmq (:scheme spec)))
    (is (= "tiger" (:user spec)))
    (is (= "scotch" (:password spec)))
    (is (= "localhost" (:server-name spec)))
    (is (= 5672 (:server-port spec)))
    (is (nil? (:uri spec)))
    (is (nil? (:query-params spec)))
    (is (nil? (:query-string spec)))))
