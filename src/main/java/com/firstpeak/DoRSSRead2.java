package com.firstpeak;

import java.util.concurrent.Callable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstpeak.domain.RssNews;
import com.firstpeak.domain.RssStory;

public class DoRSSRead2 implements Callable<RssNews> {

	private static Logger logger = LoggerFactory.getLogger(OpenNLPCategorizer.class);

	String rssSite;
	String subject;
	String rssCompany;
	
	public DoRSSRead2(String rssSite, String rssCompany, String subject) {
		this.rssSite = rssSite;
		this.subject = subject;
		this.rssCompany = rssCompany;
	}
	
	@Override
	public RssNews call() throws Exception {
		RSSFeedParser parser = new RSSFeedParser(rssSite);
		String text = null;
		RssNews rssNews = new RssNews();
		RssStory rssStory = null;
		String messageTitle = null;
		
		try {
			Feed feed = parser.readFeed();
			System.out.println(feed);

			String url = null;
			for (FeedMessage message : feed.getMessages()) {
				System.out.println(message);
				messageTitle = message.title;
				
				if (messageTitle.toLowerCase().contains(subject)) {
					url = message.link;

					if (url != null && !url.isEmpty()) {
						System.out.println("Fetching %s..." + url);
						Document doc = Jsoup.connect(url).get();
					
					text = doc.body().text();
	
					logger.debug(text);
				} else {
					text = "No news found";
					logger.debug("No news found");
				}
				
				rssNews.setRssCompany(rssCompany);
				rssNews.setSubject(subject);
				
				rssStory = new RssStory();
				rssStory.setTitle(messageTitle);
				rssStory.setStory(text);
				
				rssNews.getRssStory().add(rssStory);
				}
			}
		} catch(RuntimeException rex) {
			logger.error("No news found for: " + rssSite, rex);
		} catch(Exception ex) {
			logger.error("No news found for: " + rssSite, ex);
		}
		
		return rssNews;
	}

}
