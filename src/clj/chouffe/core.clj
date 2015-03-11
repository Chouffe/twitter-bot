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
    (twitter.callbacks.protocols SyncSingleCallback))
  (:gen-class))

;; Twitter credentials
(def creds-map (read-string (slurp "resources/credentials.edn")))

(def my-creds (make-oauth-creds (get creds-map :app-consumer-key)
                                (get creds-map :app-consumer-secret)
                                (get creds-map :user-access-token)
                                (get creds-map :user-access-token-secret)))

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

;; Multimethod that dispatches on the tweet type
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
  "Gets the tweets from the file, keeps the one that need to be send, sends
  them and mark them as tweeted
  "
  []
  (let [time-zone-france (time/time-zone-for-id "Europe/Paris")
        time-zone-server (time/time-zone-for-id "America/Los_Angeles")
        ;; TEST
        ;; now (time-f/parse "2015-03-11T03:59:07.766Z")
        time-server-now (time/from-time-zone (time/now) time-zone-server)
        tweets (get-tweets)

        parse-date-time
        #(time/from-time-zone (time-f/parse (:tweet-at %)) time-zone-france)

        tweets-to-send
        (->> tweets
             (remove :tweeted?)
             (filter #(time/before? (parse-date-time %) time-server-now)))
        new-tweets (mark-tweets-as-tweeted tweets-to-send tweets)]

    ;; TODO: log instead
    (println "Time Server Now: " time-server-now)
    (println "Tweets: " tweets)
    (println "Tweets to send: " tweets-to-send)
    (println "New tweets: " new-tweets)

    (doseq [t tweets-to-send]
      ;; TODO: log instead
      (println "Tweeting... " t)
      (tweet t))
    (write-tweets-to-file new-tweets)))

(defn- repeatedly-schedule-computation
  "Schedules the computation every `interval-s` seconds in another thread"
  [interval-s f]
  (future (Thread/sleep (* interval-s 1000))
          (f)
          (repeatedly-schedule-computation interval-s f)))

(defn -main
  "Main entrypoint of the program"
  []
  (let [interval-s (* 1 60)]
    ;; Runs every minute
    (repeatedly-schedule-computation interval-s run)))
