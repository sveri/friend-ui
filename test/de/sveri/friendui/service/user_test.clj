(ns de.sveri.friendui.service.user-test
  (:require [clojure.test :refer :all]
            [de.sveri.friendui.globals :refer [activation-placeholder]]
            [de.sveri.friendui.service.user :refer :all]))

(deftest replace-activation-placeholder
  (is (= (replace-activation "foo {{activationlink}} bar" 123 activation-placeholder)
         "foo http://example.com/user/activate/123 bar")))
