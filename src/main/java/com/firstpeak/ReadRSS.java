package com.firstpeak;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;

public class ReadRSS {
	public static void main(String[] args) throws IOException {
		RSSFeedParser parser = new RSSFeedParser("http://rss.cnn.com/rss/money_news_companies.rss");
		Feed feed = parser.readFeed();
		System.out.println(feed);
		
		String url = null;
		for (FeedMessage message : feed.getMessages()) {
			System.out.println(message);
			if(message.title.toLowerCase().contains("amazon")) {
				url = message.link;
				break;
			}
		}

//		Validate.isTrue(args.length == 1, "usage: supply url to fetch");
//		String url = "http://rss.cnn.com/c/35493/f/676926/s/4e773014/sc/21/l/0Lmoney0Bcnn0N0C20A160C0A30C230Cnews0Ccompanies0Camazon0Etarget0Cindex0Bhtml0Dsection0Fmoney0Inews0Icompanies/story01.htm";
		System.out.println("Fetching %s..." + url);

		Document doc = Jsoup.connect(url).get();
		
		String text = doc.body().text();
		
		System.out.println(text);

	}
}