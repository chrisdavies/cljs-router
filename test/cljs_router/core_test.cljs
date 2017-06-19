(ns cljs-router.core-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [cljs-router.core :as router]))


(deftest handles-basic-routes
  (let [rules (router/make-routes
                {""              :home
                 "hi/bob"        :hi-bob
                 "hi/:id"        :hi-var
                 "hi/:id/there"  :hi-there
                 ":a/:b/cat"     :a-b-cat
                 "register"      :register
                 "some/*slug"    :slugger
                 "*bad-url"      :dunnos})]
    (is (= [:home {}]
           (router/route rules "")))
    (is (= [:hi-bob {}]
           (router/route rules "hi/bob")))
    (is (= [:hi-var {:id "chris"}]
           (router/route rules "hi/chris")))
    (is (= [:hi-there {:id "callie"}]
           (router/route rules "hi/callie/there")))
    (is (= [:hi-there {:id "bob"}]
           (router/route rules "hi/bob/there")))
    (is (= [:a-b-cat {:a "1" :b "2"}]
           (router/route rules "1/2/cat")))
    (is (= [:slugger {:slug "where/out/there"}]
           (router/route rules "some/where/out/there/")))
    (is (= [:dunnos {:bad-url "waldo"}]
           (router/route rules "waldo")))))


(deftest handles-precedence
  (let [rules (router/make-routes
                {"hi/there"      :hi-there
                 "hi/:name"      :hi-name
                 "hi/*slug"      :hi-slug})]
    (is (= [:hi-there {}]
           (router/route rules "/hi/there")))
    (is (= [:hi-name {:name "joe"}]
           (router/route rules "hi/joe")))
    (is (= [:hi-slug {:slug "joe/shmo"}]
           (router/route rules "hi/joe/shmo")))))


(deftest ignores-leading-and-trailing-slashes
  (is (= [:a {}]
         (router/route (router/make-routes {"" :a}) "")))
  (is (= [:b {}]
         (router/route (router/make-routes {"/" :b}) "")))
  (is (= [:c {}]
         (router/route (router/make-routes {"" :c}) "/")))
  (is (= [:d {}]
         (router/route (router/make-routes {"/hullo" :d}) "hullo")))
  (is (= [:e {}]
         (router/route (router/make-routes {"/hullo/" :e}) "hullo"))))


(deftest not-found-default
  (let [rules (router/make-routes {"" :home})]
    (is (= [:not-found {}]
           (router/route rules "shazm")))))


(deftest decodes-url-components
  (let [rules (router/make-routes {"yo/:name" :yo})]
    (is (= [:yo {:name "%@/#?="}]
           (router/route rules "yo/%25%40%2F%23%3F%3D")))))


(deftest parses-query-parameters
  (let [rules (router/make-routes {"user/:id" :hi})]
    (is (= [:hi {:id "23" :name "James"}]
           (router/route rules "user/23?name=James")))
    (is (= [:hi {:id "24" :name "%@/#?="}]
           (router/route rules "user/24?name=%25%40%2F%23%3F%3D")))
    (is (= [:hi {:id "24"}]
           (router/route rules "user/24?id=ignored")))))


;; Hacky helper to check if an exception is thrown with a certain message
;; not sure of a better way to accomplish this in CLJS at the moment.
(defn- throws-message [re f]
  (try (f)
       (catch js/Object e
         (when (not (re-find re (:message e)))
               (is (not= (:message e) (:message e)))))))


(deftest error-if-param-renamed
  (throws-message #"foo" #((router/make-routes {"user/:id"   :hi
                                                "user/:foo"  :ruhroh}))))
