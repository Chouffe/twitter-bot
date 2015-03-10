(ns chouffe.core
  (:use
    [twitter.oauth]
    [twitter.callbacks]
    [twitter.request]
    [twitter.callbacks.handlers]
    [twitter.api.restful])
  (:require [clj-time.core :as time]
            [clj-time.format :as time-f])
  (:import
    (twitter.callbacks.protocols SyncSingleCallback)))

;; Twitter credentials
(def app-consumer-key "ISOy84VQaXeklZhZc8NKpBNe6")
(def app-consumer-secret "hySm5TAjhnTuM5MHkr3WY4cxBImUzBSUvK6SVa0mmu0lEGBVZc")
(def user-access-token "1372128799-IpTOpW7QvhJSJxeWCYfkyZrYUHA2zYmjGDP721n")
(def user-access-token-secret "kcXtN0XDDTvohs4E7sQJtdLdu9iUV2J8KyYCJlKwqgkDI")

(def my-creds (make-oauth-creds app-consumer-key
                                app-consumer-secret
                                user-access-token
                                user-access-token-secret))

(defn- get-tweets
  "Read tweets from the resource file"
  []
  (read-string (slurp "resources/tweets.edn")))

(defn- write-tweets-to-file
  "Writes the tweets to file"
  [tweets]
  (spit "resources/tweets.edn" tweets))

(defn- mark-tweets-as-tweeted
  "Updates tweeted? true for all the tweets-to-mark in tweets"
  [tweets-to-mark tweets]
  (let [mark-tweet (fn [tweet]
                     (if-not (get (set tweets-to-mark) tweet)
                       tweet
                       (assoc tweet :tweeted? true)))]
    (mapv mark-tweet tweets)))

(defmulti tweet :type)

(defmethod tweet :text
  [{:keys [status] :as tweet-map}]
  (statuses-update :oauth-creds my-creds :params {:status status}))

(defmethod tweet :image
  [{:keys [image status] :as tweet-map}]
  (statuses-update-with-media :oauth-creds my-creds
                              :body [(file-body-part (str "resources/img/" image))
                                     (status-body-part status)]))

(defn- run
  []
  (let [tweets (get-tweets)
        tweets-to-send (->> tweets
                            (remove :tweeted?)
                            (filter #(time/before? (time-f/parse (:tweet-at %))
                                                   (time/now))))
        new-tweets (mark-tweets-as-tweeted tweets-to-send tweets)]

    (println "Tweets: " tweets)
    (println "Tweets to send: " tweets-to-send)
    (println "New tweets: " new-tweets)

    (doseq [t tweets-to-send]
      (println "Tweeting... " t)
      (tweet t))
    (write-tweets-to-file new-tweets)))

(defn -main
  []
  )

;; (-> (get-tweets-edn) (assoc :test :val) write-tweets-edn)

(time-f/parse "2015-03-10T16:42:07.766Z")
