(ns de.sveri.friendui.admin
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [kioo.core :as kioo]
                   [kioo.om :refer [defsnippet deftemplate]])
  (:require [cljs.core.async :as async :refer [put! chan alts!]]
            [kioo.om :refer [content substitute do-> set-attr]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-sync.core :refer [om-sync]]
            [om-sync.util :refer [tx-tag edn-xhr]]))

(enable-console-print!)

(def app-state (atom {}))

(defn user-table [users owner]
  (reify
    om/IRenderState
    (render-state [_ _]
      (dom/div nil
               (kioo/component "de/sveri/friendui/views/templates/user/admin.html" [(attr= :field "admin")]
                               {
                                 [:table] (content "fooo")
                                 }
  )
               ))))

;(edn-xhr
;  {:method      :get
;   :url         "/user/admin/users"
;   :on-complete (fn [res]
;                  (reset! app-state res)
;                  (let [tx-chan (chan)
;                        tx-pub-chan (async/pub tx-chan (fn [_] :txs))]
;                    (om/root om-func app-state
;                             {:target    (gdom/getElement target)
;                              :shared    {:tx-chan tx-pub-chan}
;                              :tx-listen (fn [tx-data root-cursor]
;                                           (put! tx-chan [tx-data root-cursor]))})))})