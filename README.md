# Twitter Bot in Clojure

Tweets the provided tweets at the provided times. Easy to use, build and deploy.

## How to build it?

Install boot and then run
> boot build

## How to add my twitter credentials to tweet on my behalf?
Make a file resources/credentials.edn based on the sample resources/credentials.sample.edn

## How to add tweets?
Open the file resources/tweets.edn and add your tweets
```
[{:status "Hello World 1", :type :text, :tweet-at "2015-03-10T16:42:07.766Z", :tweeted? true}
 {:status "Hello World media", :type :image, :tweet-at "2015-03-10T16:42:07.766Z", :tweeted? true, :image "twitter-logo.png"}
 {:status "Test hashtag #test", :type :text, :tweet-at "2015-03-11T12:00:07.766Z", :tweeted? false}
 {:status "Hello World 2", :type :text, :tweet-at "2016-03-10T16:42:07.766Z", :tweeted? false}
 {:status "Hello World 3", :type :text, :tweet-at "2017-03-10T16:42:07.766Z", :tweeted? false}]
```

## How to change the timezone I want to tweet at?

Open the file core.clj and change the time-zone-ids
By default, the server-time-zon is configured to be in the "US/Los_Angleles"
The time-zone to tweet at is "Europe/Paris"
