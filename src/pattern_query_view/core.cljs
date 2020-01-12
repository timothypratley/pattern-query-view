(ns pattern-query-view.core
  (:require [reagent.core :as reagent]
            [justice.core :as j]
            [justice.reactive :as jr]
            [meander.epsilon :as m]
            [datascript.core :as d]
            [goog.dom.forms :as forms]
            [clojure.string :as string]))


;;;; Set up a database

(def schema
  {:github.user/login {:db/unique :db.unique/identity
                       #_#_:db/valueType :db.type/string}
   :github.user/name {#_#_:db/valueType :db.type/string}
   :github.project/name {#_#_:db/valueType :db.type/string}
   :github.project/contributors {:db/valueType :db.type/ref
                                 :db/cardinality :db.cardinality/many}})

(def seed-data
  [{:github.project/name "Meander"
    :github.project/contributors [{:github.user/login "noprompt"
                                   :github.user/name "Joel Holdbrooks"}
                                  {:github.user/login "jimmyhmiller"
                                   :github.user/name "Jimmy Miller"}]}
   {:github.project/name "Reagent"
    :github.project/contributors [{:github.user/login "mike-thompson-day8"
                                   :github.user/name "Mike Thompson"}]}
   {:github.project/name "DataScript"
    :github.project/contributors [{:github.user/login "tonsky"
                                   :github.user/name "Nikita Prokopov"}]}])

(def conn (d/create-conn schema))
(j/attach conn)
(d/transact! conn seed-data)

(defn tx-contains-attributes? [attributes {:keys [tx-data]}]
  (boolean
   (some
    (fn [[entity attribute value]]
      (contains? attributes attribute))
    tx-data)))


;;;; Define a Reagent view component

(defn github-project-contributors-table []
  (reagent/with-let
   [watch (reagent/atom 0) ;; watch is used to trigger a render
    ;; set up a listener for any TX that affects the data this component cares about
    cleanup (jr/rdbfn conn
                      identity
                      (fn relevant? [tx-report]
                        (tx-contains-attributes? #{:github.project/name
                                                   :github.project/contributors
                                                   :github.user/login
                                                   :github.user/name}
                                                 tx-report))
                      (fn on-change [x]
                        (swap! watch inc)))]
    @watch
    (m/rewrite

     ;; Data queried with a Justice pattern returns entities
     (->> (j/q '{:github.project/name _})
          ;; entities are like maps, but are not maps. entities are associative
          ;; TODO: these next 2 lines convert entities to maps... there should be a way to avoid this
          (map #(into {} %))
          (map #(update % :github.project/contributors (fn [xs] (map (fn [x] (into {} x)) xs)))))

     ;; A pattern to match the entities (might be able to combine into query pattern)
     (#:github.project{:name !project-name
                       :contributors
                       ;; TODO: can this be #{}?
                       (m/seqable
                        #:github.user{:login !user-login
                                      :name !user-name}
                        ..!n)}
      ...)

     ;; A pattern of the HTML to produce
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

    ;; when the component is unmounted, the listener is removed
    (finally (cleanup))))


;;;; Static form for adding more data

(defn add-data-form []
  [:form {:on-submit (fn on-add-data-form-submit [e]
                       (.preventDefault e)
                       (let [form (.-target e)]
                         (js/console.log "form")
                         ;; here we transact data into the DataScript db,
                         ;; the listeners detect this and trigger rendering of the table
                         (d/transact! conn
                                      [{:github.project/name (forms/getValueByName form "project")
                                        :github.project/contributors
                                        (for [contributor (string/split
                                                           (forms/getValueByName form "contributors")
                                                           #",")]
                                          {:github.user/login contributor
                                           :github.user/name contributor})}])))
          :style {:border "1px solid blue"}}
   [:h2 "Add data form:"]
   [:label
    "Project: "
    [:input {:name "project"
             :type "text"}]]
   [:br]
   [:label
    "Contributors: "
    [:input {:name "contributors"
             :type "text"}]]
   [:br]
   [:input {:type "submit"
            :value "transact data"}]])


;;;; The Reagent app root

(defn app []
  [:div
   [:h2 "Table 1: Github project contributors"]
   [github-project-contributors-table]
   [add-data-form]])

(reagent/render-component
 [app]
 (js/document.getElementById "app"))

(defn on-js-reload [])
