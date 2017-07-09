(ns com.gfredericks.schpec.defn+spec
  (:refer-clojure :exclude [defn])
  (:require [clojure.core :as core]
            [clojure.spec.alpha :as s]))

(core/defn ^:private non-&-sym? [x] (and (symbol? x) (not= '& x)))

(s/def ::arglist
  (s/cat :normal-args (s/* (s/cat :name non-&-sym?
                                  :spec-form (s/? (s/cat :- #{:-}
                                                         :spec any?))))
         :varargs (s/? (s/cat :& #{'&}
                              :name non-&-sym?
                              :spec-form (s/? (s/cat :- #{:-}
                                                     :spec any?))))))

(s/fdef kw->sym :args (s/cat :kw simple-keyword?) :ret simple-symbol?)
(core/defn ^:private kw->sym [kw] (symbol (str kw)))

(core/defn ^:private parse-arglist
  "Returns [spec-form destructuring-form]."
  [{:keys [normal-args varargs]}]
  (let [spec-form
        `(s/cat ~@(mapcat (fn [{:keys [name], {:keys [spec] :as spec-provided?} :spec-form}]
                            (let [name-kw (keyword (str name))]
                              [name-kw (if spec-provided?
                                         `(s/spec ~spec)
                                         `(s/spec any?))]))
                          normal-args)
                ~@(when varargs
                    [(-> varargs :name str keyword)
                     `(s/* ~(-> varargs :spec-form :spec (or any?)))]))
        normal-arg-names (->> normal-args
                              (map :name)
                              (map kw->sym))
        destructuring-form (cond-> {:keys (vec normal-arg-names)}
                             varargs
                             (assoc (:name varargs) :more))]
    [spec-form destructuring-form]))

(s/def ::fntail (s/cat :arglist (s/spec ::arglist)
                       :body (s/* any?)))

(s/def ::defn-args
  (s/cat :name symbol?
         :fntails
         (s/alt
          :unwrapped-fntail ::fntail
          :wrapped-fntails (s/* (s/spec ::fntail)))) )

(s/fdef defn :args ::defn-args)
(defmacro defn
  "A primitive variant of defn where args can be decorated with specs (via :-)
  and there can be multiple bodies with the same arity, in which case the
  first one for which the args match the specs is used."
  [& args]
  (let [{:keys [name fntails]}  (s/conform ::defn-args args)
        fntails (cond-> (second fntails) (= :unwrapped-fntail (first fntails)) list)
        forms (map (comp parse-arglist :arglist) fntails)
        impl-names (take (count fntails) (map #(keyword (str "clause-" %)) (range)))
        or-spec `(s/or ~@(interleave impl-names (map first forms)))
        conformed-name (gensym "conformed_")]
    `(let [arglist-spec# ~or-spec]
       (core/defn ~name
         [& args#]
         (let [~conformed-name (s/conform arglist-spec# args#)]
           (if (= ::s/invalid ~conformed-name)
             (throw (ex-info ~(str "Bad args to " name)
                             {:args args#
                              :explain (s/explain-data arglist-spec# args#)}))
             (case (first ~conformed-name)
               ~@(mapcat (fn [{:keys [body]} impl-name [_ destructuring-form]]
                           [impl-name
                            `(let [~destructuring-form (second ~conformed-name)]
                               ~@body)])
                         fntails
                         impl-names
                         forms))))))))
