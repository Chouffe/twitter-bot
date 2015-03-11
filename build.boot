(set-env!
  :source-paths    #{"src/clj/"}
  :resource-paths  #{"resources/"}
  :dependencies '[[twitter-api "0.7.8"]
                  [clj-time "0.9.0"]])

(deftask build
  "Builds Uberjar"
  []
  (task-options!
    pom {:project 'twitter-app
         :version "0.1.0"}
    aot {:namespace '#{chouffe.core}}
    jar {:main 'chouffe.core
         :manifest {"Description" "Twitter Bot"
                    ;; TODO
                    "Url" "http://github.com"}})
  (comp (aot) (pom) (uber) (jar)))
