(ns noincore.core)

(defn url-encode [s & [encoding]]
  (when s
    #+clj (java.net.URLEncoder/encode s (or encoding "UTF-8"))
    #+cljs (js/encodeURIComponent s)))

(url-encode "x")
