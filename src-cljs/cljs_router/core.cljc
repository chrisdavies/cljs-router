(ns cljs-router.core
  (:require [clojure.string :as string]))


;; Extracts the query section of a URL and returns a map of {:key "value"}
;; `s` is the query portion of the URL (e.g. if the URL was "foo.bar?hi=there",
;; s would be "hi=there" and the result would be {:hi "there"})
(defn- parse-query [s]
  (when (not (empty? s))
    (->> (string/split s "&")
         (map #(string/split % "="))
         (map (fn [[k v]] [(keyword k) (js/decodeURIComponent v)]))
         (into {}))))


;; Splits a URL path into its component parts
;; `path` is the path portion of a URL (e.g. if the URL was http://foo.bar/baz/bing
;; path would be "baz/bing" or "/baz/bing" or "/baz/bing/" leading and trailing slashes
;; are ignored)
(defn- split-path [path]
  (-> (string/replace path #"^/" "")
      (string/split #"/")))


;; Associates a parameter name with a portion of a URL.
;; `routes` is the portion of the route tree being affected
;; `name` is something like `:id` or `*id` so the leading char is dropped
;; `key` is the used for looking up the name later when matching against a URL
;; it is `:name` for names like `:id` and `:*name` for names like `*id`
(defn- assoc-param-name [routes name key]
  (let [param-name (-> name (subs 1) keyword)
        prev-name (key routes)]
    (if (and prev-name (not= prev-name param-name))
      (throw {:message (str "Inconsistent URL parameter name " prev-name " and " param-name)})
      (assoc routes key param-name))))


;; Associates a route with the specified value
;; `routes` is a map of maps (a tree) representing all known routes
;; `[hd & tl]` is a vector or list of URL components (e.g. ["users" ":id" "edit"])
;; `val` is the value to be associated with matched routes
(defn- assoc-route [routes [hd & tl] val]
  (cond
    (nil? hd)
    (assoc routes "" val)

    (string/starts-with? hd ":")
    (-> (assoc-param-name routes hd :name)
        (assoc :routes (assoc-route (get routes :routes {}) tl val)))

    (string/starts-with? hd "*")
    (-> (assoc-param-name routes hd :*name)
        (assoc :*val val))

    :else
      (assoc routes hd (assoc-route (get routes hd {}) tl val))))


(defn make-routes
  "Takes a map of route patterns and values and produces the route pattern tree.
  `rules` is a map that looks like this `{\"foo/bar\" :foo}` The value (e.g. `:foo`) can
  be any type. The pattern can include named parameters such as `users/:id/edit` or wildcard
  parameters such as `blog/*slug` which will capture the entire URL after `blog` and store it
  in the `slug` route parameter. See the cljs-router readme for more detail."
  [rules]
  (reduce (fn [routes [path val]] (assoc-route routes (split-path path) val)) {} rules))


(defn route
  "Takes a route pattern tree and a URL and returns a tuple of `[matching-value route-params]`.
  `routes` is the route tree created by a call to `make-routes`.
  `url` is a URL path (e.g. \"users/13/edit?foo=bar\")
  The example URL above might produce a return value like this `[:edit-user {:id 13 :foo \"bar\"}]`
  Exact matches take precedence, so, given the following route rules:
  `{\"foo/bar\"    :foo-bar
    \"foo/:id\"    :ident
    \"foo/*stuff\" :etc}`
    The URL `/foo/bar` would produce `[:foo-bar {}]`
    The URL `/foo/baz` would produce `[:ident {:id \"baz\"}]`
    The URL `/foo/baz/bar` would produce `[:etc {:stuff \"baz/bar\"}]`"
  ([routes url]
   (let [[path query] (string/split url #"\?")
         params (or (parse-query query) {})
         url-pieces (split-path path)]
     (or (route routes url-pieces params) [:not-found params])))
  ([routes [hd & tl :as url] params]
   (cond
     (keyword? routes) (when (nil? hd) [routes params])
     (nil? hd)         (some-> (get routes "")
                               (vector params))
     (map? routes)     (or (route (routes hd) tl params)
                           (route (:routes routes) tl (assoc params (:name routes) (js/decodeURIComponent hd)))
                           (some-> (:*val routes) (vector (assoc params (:*name routes) (string/join "/" url)))))
     :else             nil)))
