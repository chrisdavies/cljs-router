# cljs-router

A ClojureScript library designed to handle client-side routing in an order-agnostic way.

- Data-oriented: routes are exposed as maps
- Route order doesn't matter: the most specific route wins
- Supports wildcards
- Supports query strings
- Simple, small, and light-weight
- Ignores leading/trailing slashes


## Usage

Add `[cljs-router "0.1.0"]` to your `project.clj`.

Define your routes using `make-routes` and route URLs using `route`. See the sample code below:

```cljs
(ns cljs-router.core-test
  (:require [cljs-router.core :as router]))


;; Define some routes
(def routes (router/make-routes
             {"users/new"       :new-user
              "users/:id"       :show-user
              "users/:id/edit"  :edit-user
              "users/*path"     :404-user}))


;; Route some URLs using router/route

(println (router/route routes "users/new"))     ; [:new-user {}]
(println (router/route routes "users/32"))      ; [:show-user {:id "32"}]
(println (router/route routes "users/32/edit")) ; [:edit-user {:id "32"}]
(println (router/route routes "users/foo/bar")) ; [:404-user {:path "foo/bar"}]

;; Query strings are also supported and will be added as key/value pairs to the
;; route params map as the following example shows
(println (router/route routes "users/32?name=joe"))      ; [:show-user {:id "32" :name "joe"}]

;; Named route parameters override query parameters. So, in the following example,
;; the `id=joe` query parameter is overriden by the `:id` route parameter.
(println (router/route routes "users/32?id=joe"))      ; [:show-user {:id "32"}]

```

Note: In the examples above (as in my own re-frame projects), the route values are keywords
such as `:show-user` but they could be anything (e.g. `"show-user"` or `(fn [] (println "Hi!"))`).


As of `0.0.3`, you have to wire this up to whatever browser events you want (e.g. hash change or push state or whatever). Convenience functions may be added for these in future releases.


## Testing

If you want to contribute to `cljs-router`, make sure the tests pass before submitting a pull request. Tests can be run using the following command:

`lein doo node test`


## License MIT

Copyright (c) 2017 Christopher Davies

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
