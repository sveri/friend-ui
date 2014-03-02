(ns friendui.service.user
  (:require [friendui.models.db :refer [hostname mail-from]]
            [clojurewerkz.mailer.core :refer [deliver-email]]
            [taoensso.timbre :as timbre]
))



(defn generate-activation-id []
  (str (java.util.UUID/randomUUID)))

(defn generate-activation-link [activationid]
  (str hostname "user/activate/" activationid))

(defn send-activation-email [email activationid]
  (try
    (future (deliver-email {:from mail-from, :to [email] :subject "Please activate your ARP account."}
                   "templates/email/activation.mustache" {:activationlink (generate-activation-link activationid)}))
    (catch Exception e (timbre/error "Could not send email - Is the SENDMAIL variable set?\n" e)))
  )