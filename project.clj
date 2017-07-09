(defproject com.gfredericks/schpec "0.1.2-SNAPSHOT"
  :description "A utility library for clojure.spec"
  :url "https://github.com/gfredericks/schpec"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]]
  :profiles {:dev {:plugins [[lein-cljfmt "0.3.0"]]}}
  :deploy-repositories [["releases" :clojars]])
