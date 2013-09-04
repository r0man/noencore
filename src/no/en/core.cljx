(ns no.en.core
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [join replace split]]
            #+cljs [goog.crypt.base64 :as base64])
  #+clj (:import [java.net URLEncoder URLDecoder]
                 [org.apache.commons.codec.binary Base64]))

(def port-number
  {:http 80
   :https 443
   :mysql 3306
   :postgresql 5432
   :rabbitmq 5672})

(def url-regex #"([^:]+)://(([^:]+):([^@]+)@)?(([^:/]+)(:([0-9]+))?((/[^?]*)(\?(.*))?)?)")

(defn utf8-string
  "Returns `bytes` as an UTF-8 encoded string."
  [bytes]
  #+clj (String. bytes "UTF-8")
  #+cljs (throw (ex-info "utf8-string not implemented yet" bytes)))

(defn base64-encode
  "Returns `s` as a Base64 encoded string."
  [s]
  (when s
    #+clj (utf8-string (Base64/encodeBase64 (.getBytes (str s))))
    #+cljs (base64/encodeString s false)))

(defn base64-decode
  "Returns `s` as a Base64 decoded string."
  [s]
  (when s
    #+clj (utf8-string (Base64/decodeBase64 (.getBytes s)))
    #+cljs (base64/decodeString s false)))

(defn url-encode
  "Returns `s` as an URL encoded string."
  [s & [encoding]]
  (when s
    #+clj (-> (URLEncoder/encode (str s) (or encoding "UTF-8"))
              (replace "%7E" "~")
              (replace "*" "%2A")
              (replace "+" "%20"))
    #+cljs (-> (js/encodeURIComponent (str s))
               (replace "*" "%2A"))))

(defn url-decode
  "Returns `s` as an URL decoded string."
  [s & [encoding]]
  (when s
    #+clj (URLDecoder/decode s (or encoding "UTF-8"))
    #+cljs (js/decodeURIComponent s)))

(defn- parse-number [s f]
  #+clj (try (f (str s))
             (catch NumberFormatException _ nil))
  #+cljs (let [n (f (str s))]
           (if-not (js/isNaN n) n)))

(defn parse-integer
  "Parse `s` as a integer number."
  [s] (parse-number s #(#+clj Integer/parseInt #+cljs js/parseInt %1)))

(defn format-query-params
  "Format the map `m` into a query parameter string."
  [m]
  (->> (seq m)
       (map #(vector (url-encode (name (first %1)))
                     (url-encode (second %1))))
       (map #(join "=" %1))
       (join "&")))

(defn format-url
  "Format the Ring map as an url."
  [m]
  (str (name (:scheme m)) "://"
       (let [{:keys [user password]} m]
         (if user (str (if user user) (if password (str ":" password)) "@")))
       (:server-name m)
       (if-let [port (:server-port m)]
         (if-not (= port (port-number (:scheme m)))
           (str ":" port)))
       (:uri m)
       (if-let [query-params (:query-params m)]
         (str "?" (format-query-params query-params)))))

(defn parse-query-params
  "Parse the query parameter string `s` and return a map."
  [s]
  (if s
    (->> (split (str s) #"&")
         (map #(split %1 #"="))
         (filter #(= 2 (count %1)))
         (mapcat #(vector (keyword (url-decode (first %1))) (url-decode (second %1))))
         (apply hash-map))))

(defn parse-url
  "Parse the url `s` and return a Ring compatible map."
  [s]
  (if-let [matches (re-matches url-regex (str s))]
    (let [scheme (keyword (nth matches 1))]
      {:scheme scheme
       :user (nth matches 3)
       :password (nth matches 4)
       :server-name (nth matches 6)
       :server-port (or (parse-integer (nth matches 8)) (port-number scheme))
       :uri (nth matches 10)
       :query-params (parse-query-params  (nth matches 12))
       :query-string (nth matches 12)})))
