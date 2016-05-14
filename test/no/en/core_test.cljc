(ns no.en.core-test
  (:require [no.en.core :as c #?(:clj :refer :cljs :refer-macros) [prog1 with-retries]]
            #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [are is deftest]])))

(deftest test-base64-encode
  (is (nil? (c/base64-encode nil)))
  (are [s expected]
      (= expected
         #?(:clj (c/base64-encode (.getBytes s))
            :cljs (c/base64-encode s)))
    "" ""
    "1" "MQ=="
    "x" "eA=="))

(deftest test-base64-decode
  (is (nil? (c/base64-decode nil)))
  (are [s expected]
      (= expected
         #?(:clj (String. (c/base64-decode s))
            :cljs (c/base64-decode s)))
    "" ""
    "MQ==" "1"
    "eA==" "x"))

(deftest test-compact-map
  (are [x expected]
      (= expected (c/compact-map x))
    nil nil
    {} {}
    {:x nil} {}
    {:x []} {}
    {:x {}} {}
    {:x ["x"]} {:x ["x"]}
    {:x {:a 1}} {:x {:a 1}}))

(deftest test-url-encode
  (are [s expected]
      (= expected (c/url-encode s))
    nil nil
    1 "1"
    "" ""
    "a" "a"
    " " "%20"
    "*" "%2A"
    "~" "~"))

(deftest test-url-decode
  (are [s expected]
      (= expected (c/url-decode s))
    nil nil
    "" ""
    "a" "a"
    "1" "1"
    "%20" " "
    "%2A" "*"
    "~" "~"))

(deftest test-format-url
  (is (nil? (c/format-url nil)))
  (are [s]
      (= (dissoc (c/parse-url s) :query-string)
         (dissoc (c/parse-url (c/format-url (c/parse-url s))) :query-string))
    "http://example.com/"
    "https://example.com/"
    "http://bob:secret@example.com/"
    "https://bob:secret@example.com/"
    "https://bob:secret@example.com/?a=1&b=2"
    "https://bob:secret@example.com/?a=1&b=2&c=%2A"
    "https://bob:secret@example.com/?a=1&b=2&c=%2A#_=_"))

(deftest test-public-url
  (let [url (c/parse-url "http://bob:secret@example.com/")]
    (is (nil? (c/public-url nil)))
    (is (= (c/public-url url) "http://bob@example.com/"))))

(deftest test-prog1
  (is (= (prog1) nil))
  (is (= (prog1 1) 1))
  (is (= (prog1 1 2) 1)))

(deftest test-format-query-params
  (are [params expected]
      (= expected (c/format-query-params params))
    nil nil
    {:a nil} nil
    {:a "1"} "a=1"
    {:a 1} "a=1"
    {:a 1 :b 2} "a=1&b=2"))

(deftest test-parse-query-params
  (are [s expected]
      (= expected (c/parse-query-params s))
    nil nil
    "" {}
    "a=1" {:a "1"}
    "a=1&b=2&c=%2A" {:a "1" :b "2" :c "*"}))

(deftest test-parse-url
  (let [spec (c/parse-url "https://example.com")]
    (is (= :https (:scheme spec)))
    (is (= "example.com" (:server-name spec)))
    (is (= 443 (:server-port spec)))
    (is (nil? (:uri spec))))
  (let [spec (c/parse-url "https://example.com/")]
    (is (= :https (:scheme spec)))
    (is (= "example.com" (:server-name spec)))
    (is (= 443 (:server-port spec)))
    (is (= "/" (:uri spec))))
  (let [spec (c/parse-url "mysql://localhost/example")]
    (is (= :mysql (:scheme spec)))
    (is (= "localhost" (:server-name spec)))
    (is (= 3306 (:server-port spec)))
    (is (= "/example" (:uri spec))))
  (let [spec (c/parse-url "postgresql://tiger:scotch@localhost/example?a=1&b=2&c=%2A")]
    (is (= :postgresql (:scheme spec)))
    (is (= "tiger" (:username spec)))
    (is (= "scotch" (:password spec)))
    (is (= "localhost" (:server-name spec)))
    (is (= 5432 (:server-port spec)))
    (is (= "/example" (:uri spec)))
    (is (= "a=1&b=2&c=%2A" (:query-string spec)))
    (is (= {:a "1", :b "2" :c "*"} (:query-params spec))))
  (let [spec (c/parse-url "rabbitmq://tiger:scotch@localhost")]
    (is (= :rabbitmq (:scheme spec)))
    (is (= "tiger" (:username spec)))
    (is (= "scotch" (:password spec)))
    (is (= "localhost" (:server-name spec)))
    (is (= 5672 (:server-port spec)))
    (is (nil? (:uri spec)))
    (is (nil? (:query-params spec)))
    (is (nil? (:query-string spec))))
  (let [spec (c/parse-url "http://example.com/?mode=dev&access-token=e3ff052a0b32fd3309bdf6534115e25a#_=_")]
    (is (= :http (:scheme spec)))
    (is (= "example.com" (:server-name spec)))
    (is (= 80 (:server-port spec)))
    (is (= "/" (:uri spec)))
    (is (= {:mode "dev", :access-token "e3ff052a0b32fd3309bdf6534115e25a"}
           (:query-params spec)))
    (is (= "mode=dev&access-token=e3ff052a0b32fd3309bdf6534115e25a"
           (:query-string spec)))
    (is (= "_=_" (:fragment spec))))
  (let [spec (c/parse-url "http://localhost:20001/map#3/7.68/82.72")]
    (is (= :http (:scheme spec)))
    (is (= "localhost" (:server-name spec)))
    (is (= 20001 (:server-port spec)))
    (is (= "/map" (:uri spec)))
    (is (= (:query-params spec) nil))
    (is (= (:query-string spec) nil))
    (is (= "3/7.68/82.72" (:fragment spec)))))

(deftest test-with-retries
  (let [count (atom 0)]
    (with-retries 10
      (swap! count inc))
    (is (= 1 @count)))
  (let [count (atom 0)]
    (try (with-retries 10
           (swap! count inc)
           (throw (ex-info "boom" {})))
         (catch #?(:clj Exception :cljs js/Error) _ nil))
    (is (= 11 @count))))

(deftest test-parse-bytes
  (is (nil? (c/parse-bytes nil)))
  (is (nil? (c/parse-bytes "")))
  (is (= 1 (c/parse-bytes "1")))
  (is (= 1 (c/parse-bytes "1B")))
  (is (= 1 (c/parse-bytes "1.0B")))
  (is (= 10 (c/parse-bytes "10.0")))
  (is (= -10 (c/parse-bytes "-10.0")))
  (is (= 1024 (c/parse-bytes "1K")))
  (is (= 1048576 (c/parse-bytes "1M")))
  (is (= 1048576 (c/parse-bytes "1.0M"))))

(deftest test-parse-double
  (is (nil? (c/parse-double nil)))
  (is (nil? (c/parse-double "")))
  (is (= 1.0 (c/parse-double "1")))
  (is (= 10.0 (c/parse-double "10.0")))
  (is (= -10.0 (c/parse-double "-10.0")))
  (is (= 1000000.0 (c/parse-double "1M")))
  (is (= 1000000.0 (c/parse-double "1.0M")))
  (is (= 1000000000.0 (c/parse-double "1B")))
  (is (= 1000000000.0 (c/parse-double "1.0B"))))

(deftest test-parse-float
  (is (nil? (c/parse-float nil)))
  (is (nil? (c/parse-float "")))
  (is (= 1.0 (c/parse-float "1")))
  (is (= 10.0 (c/parse-float "10.0")))
  (is (= -10.0 (c/parse-float "-10.0")))
  (is (= 1000000.0 (c/parse-float "1M")))
  (is (= 1000000.0 (c/parse-float "1.0M")))
  (is (= 1000000000.0 (c/parse-float "1B")))
  (is (= 1000000000.0 (c/parse-float "1.0B"))) )

(deftest test-parse-integer
  (is (nil? (c/parse-integer nil)))
  (is (nil? (c/parse-integer "")))
  #?(:clj (is (nil? (c/parse-integer "1.1")))
     :cljs (is (= 1 (c/parse-integer "1.1"))))
  (is (= 1 (c/parse-integer "1")))
  (is (= 1 (c/parse-integer "1-europe")))
  (is (= 10 (c/parse-integer "10")))
  (is (= -10 (c/parse-integer "-10")))
  (is (= 1000000 (c/parse-integer "1M")))
  (is (= 1000000000 (c/parse-integer "1B"))))

(deftest test-parse-long
  (is (nil? (c/parse-long nil)))
  (is (nil? (c/parse-long "")))
  #?(:clj (is (nil? (c/parse-long "1.1")))
     :cljs (is (= 1 (c/parse-long "1.1"))))
  (is (= 1 (c/parse-long "1")))
  (is (= 1 (c/parse-long "1-europe")))
  (is (= 10 (c/parse-long "10")))
  (is (= -10 (c/parse-long "-10")))
  (is (= 1000000 (c/parse-long "1M")))
  (is (= 1000000000 (c/parse-long "1B"))))

(deftest test-parse-percent
  (are [string expected]
      (= expected (c/parse-percent string))
    "+18.84" 18.84
    "+18.84%" 18.84))

(deftest test-pattern-quote
  (is (= "1" (c/pattern-quote "1")))
  (is (= "x" (c/pattern-quote "x")))
  #?(:clj (is (= "\\." (c/pattern-quote "."))))
  #?(:clj (is (= "\\[" (c/pattern-quote "["))))
  #?(:clj (is (= "a\\.b\\.c" (c/pattern-quote "a.b.c")))))

(deftest test-separator
  (is (nil? (c/separator "Message")))
  (is (= "." (c/separator "twitter.hash-tags")))
  (is (= "." (c/separator "twitter.users")))
  (is (= "::" (c/separator "Admin::Post"))))

(deftest test-split-by-regex
  (let [s "1,2,3"]
    (is (= ["1" "2" "3"]
           (c/split-by-regex s #",")
           (c/split-by-regex s #"\s*,\s*")))))

(deftest test-split-by-comma
  (is (nil? (c/split-by-comma nil)))
  (is (nil? (c/split-by-comma "")))
  (is (nil? (c/split-by-comma " ")))
  (is (= ["x"] (c/split-by-comma "x")))
  (is (= ["1" "2" "3"] (c/split-by-comma "1,2,3")))
  (is (= ["1" "2" "3"] (c/split-by-comma ["1" "2" "3"]))))

(deftest test-map-keys
  (is (= (c/map-keys name {:a 1 :b 2})
         {"a" 1 "b" 2}))
  (is (= (c/map-keys name (sorted-map :a 1 :b 2))
         (sorted-map "a" 1 "b" 2))))

(deftest test-map-vals
  (is (= (c/map-vals inc {:a 1 :b 2})
         {:a 2 :b 3}))
  (is (= (c/map-vals inc (sorted-map :a 1 :b 2))
         (sorted-map :a 2 :b 3))))

(deftest test-deep-merge
  (is (= (c/deep-merge
          {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
          {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
         {:a {:b {:z 3, :c 2, :d {:z 9, :x 1, :y 2}}, :e 100}, :f 4})))

(deftest test-deep-merge-with
  (is (= (c/deep-merge-with
          +
          {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
          {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
         {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4})))
