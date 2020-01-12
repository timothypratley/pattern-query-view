(ns pattern-query-view.macros
  #?(:cljs (:require-macros [pattern-query-view.macros]))
  #?(:cljs (:require [reagent.core] [meander.epsilon] [justice.core] [justice.reactive])))

(defmacro defview
  "Creates a Reagent component.
  The `query` pattern is used to search the db for entities.
  The `match` pattern is used to pull out interesting data.
  The `view` pattern is used to construct hiccup representing HTML."
  [component-name query match view]
  `(defn ~component-name []
     (let
      [watch# (reagent.core/atom 0) ;; watch is used to trigger a render
       ;; set up a listener for any TX that affects the data this component cares about
       cleanup# (justice.reactive/rdbfn
                 justice.core/*conn*
                 identity
                 (fn relevant?# [tx-data#]
                   (boolean
                    (some
                     (fn [[entity# attribute# value#]]
                       (contains? #{:github.project/name
                                    :github.project/contributors
                                    :github.user/login
                                    :github.user/name}
                                  attribute#))
                     tx-data#)))
                 (fn on-change# [x#]
                   (swap! watch# inc)))]
       (reagent.core/create-class
        {:display-name ~(name component-name)
         :reagent-render
         (fn ~(symbol (str (name component-name) "-render")) []
           @watch#
           (meander.epsilon/rewrite
            ;; Data queried with a Justice pattern returns entities
            (->> (justice.core/q '~query)
                 ;; entities are like maps, but are not maps. entities are associative
                 ;; TODO: these next 2 lines convert entities to maps... there should be a way to avoid this
                 (map #(into {} %))
                 (map (fn [e#] (update e# :github.project/contributors (fn [xs#] (map (fn [x#] (into {} x#)) xs#))))))
            ;; A pattern to match the entities (might be able to combine into query pattern)
            ~match
            ;; A pattern of the HTML to produce
            ~view))
         :component-will-unmount
         (fn ~(symbol (str (name component-name) "-will-unmount")) [~'this]
           (cleanup#))}))))
