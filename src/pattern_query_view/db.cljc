(ns pattern-query-view.db
  (:require [datascript.core :as d]
            [justice.core :as j]))

(def schema
  (merge
   #:github.user{:login #:db{:unique :db.unique/identity
                             #_#_:valueType :db.type/string}
                 :name #:db{#_#_:valueType :db.type/string}}
   #:github.project{:name #:db{#_#_:valueType :db.type/string}
                    :contributors #:db{:valueType :db.type/ref
                                       :cardinality :db.cardinality/many}}))

(def conn (d/create-conn schema))
(j/attach conn)

(defn add-project! [project contributors]
  (d/transact!
   conn
   [{:github.project/name project
     :github.project/contributors (for [contributor contributors]
                                    {:github.user/login contributor
                                     :github.user/name contributor})}]))

(def seed-data
  [#:github.project{:name "Meander"
                    :contributors [#:github.user{:login "noprompt"
                                                 :name "Joel Holdbrooks"}
                                   #:github.user{:login "jimmyhmiller"
                                                 :name "Jimmy Miller"}]}
   #:github.project{:name "Reagent"
                    :contributors [#:github.user{:login "mike-thompson-day8"
                                                 :name "Mike Thompson"}]}
   #:github.project{:name "DataScript"
                    :contributors [#:github.user{:login "tonsky"
                                                 :name "Nikita Prokopov"}]}])

(d/transact! conn seed-data)
