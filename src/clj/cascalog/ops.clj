 ;    Copyright 2010 Nathan Marz
 ; 
 ;    This program is free software: you can redistribute it and/or modify
 ;    it under the terms of the GNU General Public License as published by
 ;    the Free Software Foundation, either version 3 of the License, or
 ;    (at your option) any later version.
 ; 
 ;    This program is distributed in the hope that it will be useful,
 ;    but WITHOUT ANY WARRANTY; without even the implied warranty of
 ;    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 ;    GNU General Public License for more details.
 ; 
 ;    You should have received a copy of the GNU General Public License
 ;    along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns cascalog.ops
  (:refer-clojure :exclude [count min max])
  (:use [cascalog ops-impl api]))

(defmapop [re-parse [pattern]] [str]
  (re-seq pattern str))

(defparallelagg count :init-var #'one
                      :combine-var #'+
                      :args 0)

(defparallelagg sum :init-var #'identity
                    :combine-var #'+
                    :args 1)

(defparallelagg min :init-var #'identity
                    :combine-var #'clojure.core/min
                    :args 1)

(defparallelagg max :init-var #'identity
                    :combine-var #'clojure.core/max
                    :args 1)

(defparallelagg !count :init-var #'existence-int
                       :combine-var #'+
                       :args 1)

(defparallelbuf limit :hof? true
                      :init-hof-var #'limit-init
                      :combine-hof-var #'limit-combine
                      :extract-hof-var #'limit-extract
                      :num-intermediate-vars-fn (fn [infields outfields] (clojure.core/count infields))
                      :buffer-hof-var #'limit-buffer )

(def limit-rank (merge limit {:buffer-hof-var #'limit-rank-buffer} ))

(def avg (<- [!v :> !avg] (count !c) (sum !v :> !s) (div !s !c :> !avg)))

(def distinct-count (<- [!v :> !c] (:sort !v) (distinct-count-agg !v :> !c)))

;; should be able to do this kind of destructuring:
;; (def distinct-count (<- [:<< [!v & rest-vars] :> !c] (:sort !v) (distinct-count-agg !v :> !c)))
;; (def distinct-count (<- [:<< a :> !c] (:sort !v) (distinct-count-agg :<< a :> !c)))
