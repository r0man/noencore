(ns no.in.core
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [replace]]))

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
