(ns com.gfredericks.schpec-test
  (:require [com.gfredericks.schpec :as schpec]
            [clojure.spec :as s]
            [clojure.test :refer [deftest is are]]))

(s/def ::my-excl-keys
  (schpec/excl-keys
   :opt [::my-opt]
   :req [::my-req]
   :opt-un [::my-opt-un]
   :req-un [::my-req-un]))

(s/def ::my-opt int?)
(s/def ::my-req int?)
(s/def ::my-opt-un int?)
(s/def ::my-req-un int?)

(deftest excl-keys-test
  (let [sample {::my-opt 1
                ::my-req 2
                :my-opt-un 3
                :my-req-un 4}]
    (is (s/valid? ::my-excl-keys sample))
    (let [sample' (assoc sample ::some-other-key 1)]
      (is (= {:clojure.spec/problems
              [{:path []
                :pred '(fn [m] (subset? (set (keys m)) ks))
                :val sample'
                :via [::my-excl-keys]
                :in []}]}
             (s/explain-data ::my-excl-keys sample'))))))

(s/def ::my-xor
  (schpec/xor :x (s/keys :req [::x])
              :y (s/keys :req [::y])))
(s/def ::x int?)
(s/def ::y int?)

(deftest xor-test
  (is (nil? (s/explain-data ::my-xor {::x 1})))
  (is (nil? (s/explain-data ::my-xor {::y 1})))
  (is (some? (s/explain-data ::my-xor {::x 1 ::y 1})))
  (is (some? (s/explain-data ::my-xor {::y "abc"}))))

(schpec/alias 'p 'person)

(deftest alias-test
  (let [aliases (ns-aliases 'com.gfredericks.test-schpec)]
    (is (contains? aliases 'p))
    (is (= 'person (ns-name (aliases 'p))))))
