(ns com.gfredericks.schpec.defn+spec-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is]]
            [com.gfredericks.schpec.defn+spec :as defn+spec]))

(defn+spec/defn single-body
  [a :- integer? b]
  [a b])

(deftest single-body-test
  (is (= [42 49] (single-body 42 49)))
  (is (= [0 "nil"] (single-body 0 "nil")))
  (is (thrown-with-msg? Exception #"Bad args to single-body"
                        (single-body 42)))
  (is (thrown-with-msg? Exception #"Bad args to single-body"
                        (single-body "not a number" 42))))

(defn+spec/defn thomas
  ([a :- integer?, b :- boolean?]
   [:int-and-bool a b])
  ([a b]
   [:any-two-args a b])
  ([a b c :- integer? d & more]
   [:four-args-1-int+varargs a b c d "here's the varargs ->" more])
  ([a b c d]
   [:any-four-args a b c d]))

(defn+spec/defn my-identity
  ([a] a))

(deftest defn+spec-test
  (is (= 42 (my-identity 42)))
  (is (= (thomas 1 2)
         [:any-two-args 1 2]))
  (is (= (thomas 42 true)
         [:int-and-bool 42 true]))
  (is (= (thomas "one" "two" "three" "four")
         [:any-four-args "one" "two" "three" "four"]))
  (is (= (thomas "one" "two" 3 "four" "five" "six")
         [:four-args-1-int+varargs "one" "two" 3 "four" "here's the varargs ->" ["five" "six"]]))

  (is (thrown-with-msg? Exception #"Bad args to thomas"
                        (thomas 42))))
