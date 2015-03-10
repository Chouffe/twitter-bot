(set-env!
  :source-paths    #{"src/clj"}
  :resource-paths  #{"resources"}
  :dependencies '[[twitter-api "0.7.8"]
                  [clj-time "0.9.0"]])

(deftask build []
  (task-options! pom {:project 'twitter-app
                      :version "0.1.0"})
  (comp (pom) (jar) (install)))
