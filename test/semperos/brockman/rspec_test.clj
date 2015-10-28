(ns semperos.brockman.rspec-test
  (:require [clojure.test :refer :all]
            [clojure.string :refer [blank? join]]
            [clojure.test.check.generators :as gen]
            [com.gfredericks.test.chuck.clojure-test :refer [checking]]
            [com.gfredericks.test.chuck.properties :refer [for-all]]
            [semperos.brockman.rspec :as rspec])
  (:import clojure.lang.ExceptionInfo))

(deftest test-styles
  (testing "When coloring output,"
    (is (= [:green :bright] (:pass rspec/styles))
        "success should be green")
    (is (= [:red :bright] (:fail rspec/styles))
        "failure should be red")
    (is (= [:red :bright :underline] (:error rspec/styles))
        "errors should be underlined red")))

(def char-non-escape
  "All characters except the escape sequence for ANSI terminals."
  (gen/fmap char
            (gen/one-of [(gen/choose 0 26)
                         (gen/choose 28 255)])))

(def string-non-escape
  "String of all characters except escape sequence."
  (gen/fmap join (gen/vector char-non-escape)))

(deftest test-style
  (checking "Style should retain the text given it" 50
            ;; colorstr escapes the escape sequence
            [s string-non-escape]
            (is (.contains ^String (rspec/style :pass s) s))
            (is (.contains ^String (rspec/style :fail s) s))
            (is (.contains ^String (rspec/style :error s) s)))
  (try (rspec/style :not-supported "foo")
       (catch ExceptionInfo e
         (is (= #{:pass :fail :error} (get (ex-data e) :supported-styles))
             "An exception should be thrown on unsupported styles"))))

(deftest test-indent
  (checking "Indenting" 50
            [n gen/int]
            (let [result (rspec/indent n)]
              (is (zero? (mod (count result) 4)) "should be in multiples of 4")
              (is (every? (partial = \space) result) "should be spaces"))))

(deftest test-indent-newline
  (testing "Indent on newline"
    (let [a (str "one two" \newline "three")
          b (str "one" \newline "two" \newline "three")
          c "one two three"
          i 2]
      (is (= (str "one two" \newline "        three")
             (rspec/indent-newline i a))
          "should work with two lines")
      (is (= (str "one" \newline "        two" \newline "        three")
             (rspec/indent-newline i b))
          "should work with three lines")
      (is (= "one two three"
             (rspec/indent-newline i c))
          "should work with one line"))))

(def testy "testing var")

(deftest test-testing-vars-str
  (testing "Custom testing-vars-str"
   (binding [clojure.test/*testing-vars* (list #'testy)]
     (is (= "semperos.brockman.rspec-test/testy"
            (rspec/testing-vars-str {}))))))

(deftest test-indent-contexts
  (testing "Indenting test contexts"
   (binding [clojure.test/*testing-contexts* ["alpha" "beta" "gamma"]]
     (is (= ["    alpha" "        beta" "            gamma"]
            (rspec/indent-contexts))))))
