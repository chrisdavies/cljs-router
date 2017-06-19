(ns cljs-router.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [pjstadig.humane-test-output]
            [cljs-router.core-test]))

(doo-tests 'cljs-router.core-test)
