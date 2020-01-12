(ns pattern-query-view.core
  (:require [pattern-query-view.macros :as p]
            [pattern-query-view.db :as db]
            [clojure.string :as string]
            [goog.dom.forms :as forms]
            [reagent.core :as reagent]))

(p/defview github-project-contributors-table
  ;; Justice pattern that queries for entities
  #:github.project{:name _}
  ;; A pattern to match the entities
  (#:github.project{:name !project-name
                    :contributors (#:github.user{:login !user-login
                                                 :name !user-name}
                                   ..!n)}
   ...)
  ;; Hiccup pattern of the view
  [:table {:style {:border "1px solid"}}
   [:thead
    [:tr
     [:th "Project"] [:th "Contributors"]]]
   [:tbody
    .
    [:tr
     [:td !project-name] [:td [:ul
                               .
                               [:li !user-name " (" !user-login ")"]
                               ..!n]]]
    ...]
   [:tfoot
    [:tr
     [:td.numeric "Total: " ~(count !project-name)] [:td.numeric "Total: " ~(count !user-login)]]]])

;;;; Regular Reagent static form for adding more data

(defn add-data-form []
  [:form
   {:on-submit
    (fn on-add-data-form-submit [e]
      (.preventDefault e)
      (let [form (.-target e)
            project (forms/getValueByName form "project")
            contributors (map string/trim (string/split (forms/getValueByName form "contributors")
                                                        #","))]
        (db/add-project! project contributors)))
    :style {:border "1px solid blue"}}
   [:h2 "Add data form:"]
   [:label "Project: " [:input {:name "project" :type "text"}]]
   [:br]
   [:label "Contributors: " [:input {:name "contributors" :type "text"}]]
   [:br]
   [:input {:type "submit" :value "transact data"}]])


;;;; The Reagent app root

(defn app []
  [:div
   [:h2 "Table 1: Github project contributors"]
   [github-project-contributors-table]
   [add-data-form]])

(reagent/render-component
 [app]
 (js/document.getElementById "app"))
