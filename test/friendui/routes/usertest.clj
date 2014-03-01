(ns friendui.routes.usertest
  (:require [midje.sweet :refer :all]
            [friendui.routes.user :as userroute]
            [noir.util.test :as noir-test]))

;(fact "register validation should ret false for to short password"
;      (noir-test/with-noir (println (userroute/validRegister? "email" "vilvlcvlc" "vil"))))

;(fact "test"
;      3 => 3)
