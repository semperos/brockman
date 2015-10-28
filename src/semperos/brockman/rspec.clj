(ns semperos.brockman.rspec
  "RSpec-style reporter for clojure.test test suites.

  Use this namespace by invoking `(semperos.brockman/test-reporter! :rspec)` at the beginning of your suite."
  (:require [clojure.string :refer [blank? join] :as s]
            [clojure.stacktrace :as stack]
            [clojure.test :as t]
            [colorstr.core :as color]))

;; Make order of contexts convenient for our reporting
(alter-var-root #'clojure.test/*testing-contexts* (fn [_] (vector)))

(def styles
  "Arguments that colorstr.core/style will accept for ANSI terminal codes."
  {:pass [:green :bright]
   :fail [:red :bright]
   :error [:red :bright :underline]})

(def style-set
  "Supported styles for test reporting"
  (set (keys styles)))

(defn style
  "Style non-blank strings according to supported test reporting styles."
  [style & args]
  (when-not (style-set style)
    (throw (ex-info (str "The style " (pr-str style)
                         " is not supported. Use one of " (pr-str style-set))
                    {:supported-styles style-set})))
  (let [msg (join args)]
    (if (blank? msg)
      msg
      (apply color/style msg (style styles)))))

(defn indent
  "Indentation of (* n 4) spaces."
  [n]
  (apply str (take (* n 4) (repeat " "))))

(defn indent-newline
  "Ensure newlines in output conform to indentation level."
  [indent-level s]
  (s/replace s #"(?m)\n *" (str \newline (indent indent-level))))

(defn testing-vars-str
  "Returns a string representation of the current test name.  Renders names
  in *testing-vars* as a list, then the source file and line of
  current assertion if available.

  Differs from default clojure.test impl in that:
    * Doesn't print out file/line data if not present
    * Includes the namespace of the var
    * Assumes only one testing var (no nesting)."
  [m]
  (let [{:keys [file line]} m]
    (str
     (ns-name (:ns (meta (first t/*testing-vars*)))) "/"
     (first (map #(:name (meta %)) t/*testing-vars*))
     (when file (str " (" file ":" line ")")))))

(defn indent-contexts
  "Indent test contexts for printing purposes according to depth in the clojure.test/*testing-contexts* vector."
  []
  (map-indexed (fn [idx ctx]
                 (str (indent (inc idx)) ctx))
               (seq t/*testing-contexts*)))

;; Supercede default clojure.test/report defmethod's.
;; This allows us to continue using methods we don't care to override.
;;
;; Alternative approach is to use a custom multimethod and
;; rebind the dynamic clojure.test/report itself to this custom multimethod.
(defmethod t/report :pass [m]
  (t/with-test-out
    (t/inc-report-counter :pass)
    (println (style :pass "\nSuccess: " (testing-vars-str m)))
    (let [contexts (indent-contexts)
          msg-indent (inc (count contexts))]
      (doseq [context contexts]
        (println (style :pass context)))
      (when-let [message (:message m)]
        (println (style :pass (indent msg-indent) message))))))

(defmethod t/report :fail [m]
  (t/with-test-out
    (t/inc-report-counter :fail)
    (println (style :fail "\nFailure: " (testing-vars-str m)))
    (let [contexts (indent-contexts)
          msg-indent (inc (count contexts))
          comp-indent (inc msg-indent)]
      (doseq [context contexts]
        (println (style :fail context)))
      (when-let [message (:message m)]
        (println (style :fail (str (indent msg-indent) message))))
      (println (style :fail (indent comp-indent) "expected: " (pr-str (:expected m))))
      (println (style :fail (indent comp-indent) "  actual: " (pr-str (:actual m)))))))

(defmethod t/report :error [m]
  (t/with-test-out
    (t/inc-report-counter :error)
    (println (style :error "\nError: " (testing-vars-str m)))
    (let [contexts (indent-contexts)
          msg-indent (inc (count contexts))
          comp-indent (inc msg-indent)]
      (doseq [context contexts]
        (println (style :error context)))
      (when-let [message (:message m)]
        (println (style :error (str (indent msg-indent) message))))
      (println (style :error (indent comp-indent) "expected: " (pr-str (:expected m))))
      (println (style :error (indent comp-indent) "  actual: " (indent-newline (inc comp-indent) (pr-str (:actual m)))))
      (let [actual (:actual m)
            actual-str (if (instance? Throwable actual)
                         (with-out-str (stack/print-cause-trace actual t/*stack-trace-depth*))
                         (pr-str actual))]
        (println (style :error (indent comp-indent) (indent-newline (inc comp-indent) actual-str)))))))
