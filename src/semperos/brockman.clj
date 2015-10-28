(ns semperos.brockman)

(defn test-reporter!
  "Activate a test reporter.

  Usage: (test-reporter! :rspec)"
  ([] (test-reporter! :rspec))
  ([style]
   (require (symbol (str "semperos.brockman." (name style))))))
