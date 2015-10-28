(defproject com.semperos/brockman "0.1.0"
  :description "Custom test reporting for clojure.test"
  :url "https://github.com/semperos/brockman"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :global-vars {*warn-on-reflection* true}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [colorstr "0.1.0"]]
  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :creds :gpg}]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.8.2"]
                                  [com.gfredericks/test.chuck "0.2.0"]]}})
