package com.firstpeak;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TweetReader {

	public static void main(String[] args) {
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey("gBYNQT4IaarGiOvONESGju5u8");
		cb.setOAuthConsumerSecret("JBV6hUeG4nmVIT0lvSbli4kTVYjzNeuDo2CpH5ojGwXzcsXkmL");
		cb.setOAuthAccessToken("715551333940461568-JiEe7A93udEw5KHVVYZQQkwmN4FCcK6");
		cb.setOAuthAccessTokenSecret("1Ka94aRp6KMgZSCvQQ3D1n0bHVWAAsGfijO77vOBDYLKY");

		Twitter twitter = new TwitterFactory(cb.build()).getInstance();

		int pageno = 1;
		String user = "cnn";
		List statuses = new ArrayList();

		while (true) {

			try {

				int size = statuses.size();
				Paging page = new Paging(pageno++, 100);
				statuses.addAll(twitter.getUserTimeline(user, page));
				if (statuses.size() == size)
					break;
			} catch (TwitterException e) {

				e.printStackTrace();
			}
		}

		System.out.println("Total: " + statuses.size());

	}

}
