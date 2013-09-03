(ns no.in.core
  #+clj (:import [org.apache.commons.codec.binary Base64])
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [replace]]
            #+cljs [goog.crypt.base64 :as base64]))

(defn base64-encode
  "Returns `s` as a Base64 encoded string."
  [s]
  (when s
    #+clj
    (String. (Base64/encodeBase64 (.getBytes s)) "UTF-8")
    #+cljs
    (base64/encodeString s false)))

(defn base64-decode
  "Returns `s` as a Base64 decoded string."
  [s]
  (when s
    #+clj
    (String. (Base64/decodeBase64 (.getBytes s)) "UTF-8")
    #+cljs
    (base64/decodeString s false)))

(defn url-encode
  "Returns `s` as an URL encoded string."
  [s & [encoding]]
  (when s
    #+clj (-> (java.net.URLEncoder/encode s (or encoding "UTF-8"))
              (replace "%7E" "~")
              (replace "*" "%2A")
              (replace "+" "%20"))
    #+cljs (-> (js/encodeURIComponent s)
               (replace "*" "%2A"))))

(defn url-decode
  "Returns `s` as an URL decoded string."
  [s & [encoding]]
  (when s
    #+clj (java.net.URLDecoder/decode s (or encoding "UTF-8"))
    #+cljs (js/decodeURIComponent s)))
