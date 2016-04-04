package com.firstpeak;

import java.util.concurrent.Callable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoRSSRead implements Callable<String> {

	private static Logger logger = LoggerFactory.getLogger(OpenNLPCategorizer.class);

	String rssSite;
	String subject;
	
	public DoRSSRead(String rssSite, String subject) {
		this.rssSite = rssSite;
		this.subject = subject;
	}
	
	@Override
	public String call() throws Exception {
		RSSFeedParser parser = new RSSFeedParser(rssSite);
		String text = null;
		
		try {
			Feed feed = parser.readFeed();
			System.out.println(feed);

			String url = null;
			for (FeedMessage message : feed.getMessages()) {
				System.out.println(message);
				if (message.title.toLowerCase().contains(subject)) {
					url = message.link;
					break;
				}
			}

			if (url != null && !url.isEmpty()) {
				System.out.println("Fetching %s..." + url);
				Document doc = Jsoup.connect(url).get();
				
				text = doc.body().text();

				logger.debug(text);
			} else {
				text = "No news found";
				logger.debug("No news found");
			}
		} catch(RuntimeException rex) {
			logger.error("No news found for: " + rssSite, rex);
		} catch(Exception ex) {
			logger.error("No news found for: " + rssSite, ex);
		}
		
		return text;
	}

}
