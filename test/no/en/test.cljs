(ns no.en.test
  (:require [no.en.core-test]
            [doo.runner :refer-macros [doo-tests]]))

(doo-tests 'no.en.core-test)
