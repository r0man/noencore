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
    1 "MQ=="
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
    1 "1"
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
    "1" "1"
    "%20" " "
    "%2A" "*"
    "~" "~"))

(deftest test-format-url
  (are [s]
    (is (= (dissoc (c/parse-url s) :query-string)
           (dissoc (c/parse-url (c/format-url (c/parse-url s))) :query-string)))
    "http://bob:secret@example.com"
    "https://bob:secret@example.com/"
    "https://bob:secret@example.com/?a=1&b=2"
    "https://bob:secret@example.com/?a=1&b=2&c=%2A"))

(deftest test-format-query-params
  (are [params expected]
    (is (= expected (c/format-query-params params)))
    {:a "1"} "a=1"
    {:a 1} "a=1"
    {:a 1 :b 2} "a=1&b=2"))

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
  (let [spec (c/parse-url "postgresql://tiger:scotch@localhost/example?a=1&b=2&c=%2A")]
    (is (= :postgresql (:scheme spec)))
    (is (= "tiger" (:user spec)))
    (is (= "scotch" (:password spec)))
    (is (= "localhost" (:server-name spec)))
    (is (= 5432 (:server-port spec)))
    (is (= "/example" (:uri spec)))
    (is (= "a=1&b=2&c=%2A" (:query-string spec)))
    (is (= {:a "1", :b "2" :c "*"} (:query-params spec))))
  (let [spec (c/parse-url "rabbitmq://tiger:scotch@localhost")]
    (is (= :rabbitmq (:scheme spec)))
    (is (= "tiger" (:user spec)))
    (is (= "scotch" (:password spec)))
    (is (= "localhost" (:server-name spec)))
    (is (= 5672 (:server-port spec)))
    (is (nil? (:uri spec)))
    (is (nil? (:query-params spec)))
    (is (nil? (:query-string spec)))))
