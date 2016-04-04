package com.firstpeak;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TweetManager {

	public static void main(String args[]) throws Exception {
//		TweetManager tm = new TweetManager();
//		String topic = "google stock";
//		ArrayList<String> tweets = TweetManager.getTweets(topic);
//		for (String tweet : tweets) {
//			System.out.println(tweet);
//		}
		
		NLP.init();
		String tweet = "this movie sucks";
		System.out.println(tweet + " : " + NLP.findSentiment(tweet));
//        for(String tweet : tweets) {
//            System.out.println(tweet + " : " + NLP.findSentiment(tweet));
//        }
	}

	public static ArrayList<String> getTweets(String topic) {

//		Twitter twitter = new TwitterFactory().getInstance();
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey("gBYNQT4IaarGiOvONESGju5u8");
		cb.setOAuthConsumerSecret("JBV6hUeG4nmVIT0lvSbli4kTVYjzNeuDo2CpH5ojGwXzcsXkmL");
		cb.setOAuthAccessToken("715551333940461568-JiEe7A93udEw5KHVVYZQQkwmN4FCcK6");
		cb.setOAuthAccessTokenSecret("1Ka94aRp6KMgZSCvQQ3D1n0bHVWAAsGfijO77vOBDYLKY");

		Twitter twitter = new TwitterFactory(cb.build()).getInstance();

		ArrayList<String> tweetList = new ArrayList<String>();
		try {
			Query query = new Query(topic);
			QueryResult result;
			do {
				result = twitter.search(query);
				List<Status> tweets = result.getTweets();
				for (Status tweet : tweets) {
					tweetList.add(tweet.getText());
				}
			} while ((query = result.nextQuery()) != null);
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to search tweets: " + te.getMessage());
		}
		return tweetList;
	}
}
