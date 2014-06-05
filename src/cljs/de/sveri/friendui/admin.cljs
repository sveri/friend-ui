(ns de.sveri.friendui.admin
  (:require-macros [cljs.core.async.macros :refer [go]]
                   ;[kioo.core :as kioo]
                   ;[kioo.om :refer [defsnippet deftemplate]]
                   )
  (:require [cljs.core.async :as async :refer [put! chan alts!]]
            ;[kioo.om :refer [content substitute do-> set-attr]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-sync.core :refer [om-sync]]
            [om-sync.util :refer [tx-tag edn-xhr]]
            [goog.dom :as gdom]))

(enable-console-print!)

(def template-path "templates/de/sveri/friendui/user/")

(def app-state (atom {}))

(def user-admin-table-header [
                              ; {:header "Username"}
                              ;{:header "Role"}
                              ;{:header "Active"}
                               [""]])

;(defsnippet admin-user-table-header (str template-path "admin.html") [:.header-entry]
;                 [[header-name]]
;                 ;[{:keys [header]}]
;                 [:.header-entry] (html/content header-name))

;(defsnippet my-nav-item (str template-path "admin.html") [:.header-entry]
;            [[caption]]
;            {[:.header-entry] (content caption) })

;(deftemplate my-page (str template-path "admin.html")
;             [data]
;             {[:header] (substitute (my-header data))
;              [:.content] (content (:content data))})

(defn user-table [users owner]
  (reify
    om/IRenderState
    (render-state [_ _]
      (dom/table #js {:className "table"}
                 (dom/thead nil
                            (dom/tr nil
                                    (dom/td nil "Username")
                                    (dom/td nil "Role")
                                    (dom/td nil "Active")
                                    (dom/td nil "")
                                    )))
      )))

;(deftemplate my-page (str template-path "admin-user-table.html")
;             [data]
;             {[:#testid] (content "foo")
;              })

;(defn user-table [users owner]
;  (reify
;    om/IRender
;    (render [_]
;      ;(om/component (my-page users))
;      (dom/div #js {:className "baar"}
;               (kioo/component (str template-path "admin-user-table.html")
;                       [:#testid]
;                       {[:#subtestid] (content "foo")}))
;      )))

;(kioo/component "templates/release-snippet.html" [(attr= :field "issues-div")]
;                {[(attr= :field "issues")] (content
;                                             (reduce (fn [x y] (str x ", " (:epic/key y))) "" issues))})

(defn user-view [app _]
  (reify
    om/IRenderState
    (render-state [_ _]
      (dom/div #js {:className "foo"}
               (om/build om-sync (:users app)
                         {:opts {:view       user-table
                                 :filter     (comp #{:create :update :delete} tx-tag)
                                 :id-key     :db/id
                                 :on-success (fn [_ _] nil)
                                 :on-error   (fn [err _]
                                               ;(println "error " err)
                                               )}})))))

(edn-xhr
  {:method      :get
   :url         "/user/admin/users"
   :on-complete (fn [res]
                  (reset! app-state res)
                  (println @app-state)
                  (let [tx-chan (chan)
                        tx-pub-chan (async/pub tx-chan (fn [_] :txs))]
                    (om/root user-view app-state
                             {:target    (gdom/getElement "admin")
                              :shared    {:tx-chan tx-pub-chan}
                              :tx-listen (fn [tx-data root-cursor]
                                           (put! tx-chan [tx-data root-cursor]))}))
                    )})


