(ns com.gfredericks.schpec
  (:refer-clojure :exclude [alias])
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as sg]
            [clojure.set :as set]))

(defn limit-keys
  "Given a key spec form, limits its keys to a particular subset."
  [spec-form ks]
  (let [ks (set ks)]
    (s/and spec-form (fn [m] (set/subset? (set (keys m)) ks)))))

(defmacro excl-keys
  "Like [[s/keys]], but closed for extension.

  The generator for this spec will only generate maps with explicitly
  specified keys.

  The :check-keys? keyword argument (true by default) determines if
  the limitation on the keys is enforced. This is useful if you'd like
  to merge the key-specs later: [[s/merge]] assumes that the data
  satisfies the individual specs as well. You can limit the keys
  allowed in a key spec later using [[limit-keys]]."
  [& {:keys [req-un opt-un req opt check-keys?]
      :as keys-spec
      :or {check-keys? true}}]
  (let [keys-spec (dissoc keys-spec :check-keys?)
        bare-un-keys (map (comp keyword name) (concat req-un opt-un))
        all-keys (set (concat bare-un-keys req opt))]
    `(let [ks# (s/keys ~@(apply concat keys-spec))]
       (s/with-gen
         (if ~check-keys?
           (limit-keys ks# ~all-keys)
           ks#)
         (fn [] (sg/fmap (fn [m#] (select-keys m# ~all-keys)) (s/gen ks#)))))))

(defmacro xor
  "Like [[s/or]], but values can only match exactly one spec."
  [& key-pred-forms]
  (let [key-pred-forms (set (partition 2 key-pred-forms))
        opts (mapcat
              (fn [[name spec :as pair]]
                (let [preds (for [s (map second (disj key-pred-forms pair))]
                              `(fn [x#] (not (s/valid? ~s x#))))]
                  [name `(s/and ~spec ~@preds)]))
              key-pred-forms)]
    `(s/or ~@opts)))

(defn alias
  "Like clojure.core/alias, but can alias to non-existing namespaces"
  [alias namespace-sym]
  (try (clojure.core/alias alias namespace-sym)
       (catch Exception _
         (create-ns namespace-sym)
         (clojure.core/alias alias namespace-sym))))

(s/fdef alias
  :args (s/cat :alias simple-symbol? :ns simple-symbol?)
  :ret nil?)
