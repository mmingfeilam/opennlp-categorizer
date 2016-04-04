package com.firstpeak;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstpeak.domain.RssNews;
import com.firstpeak.domain.RssStory;
import com.firstpeak.json.RssSentiment;
import com.firstpeak.json.RssSite;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

public class OpenNLPCategorizer {
	protected static final String NO_NEWS_FOUND = "No news found";
	DoccatModel model;
	static ObjectMapper mapper = new ObjectMapper();

	private static Logger logger = LoggerFactory.getLogger(OpenNLPCategorizer.class);

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		OpenNLPCategorizer openNLPCategorizer = new OpenNLPCategorizer();

		// List<RssSite> rss = new ArrayList<>();
		// RssSite site1 = new RssSite();
		// site1.setCompany("cnn");
		// site1.getRssFeeds().add("link1");
		//
		// RssSite site2 = new RssSite();
		// site2.setCompany("cnbn");
		// site2.getRssFeeds().add("link1");
		//
		// rss.add(site1);
		// rss.add(site2);
		//
		// mapper.writeValue(new File("user.json"), rss);

		List<RssSite> rssSites = openNLPCategorizer.getRssSitesFromJsonFile();

		// List<String> rssSites = openNLPCategorizer.getRssSites();

		// twitterCategorizer.writeProperties();
		// openNLPCategorizer.loadProperties("resources/config.properties");

		openNLPCategorizer.trainModel();
		List<String> contents = new ArrayList<>();

		// String content = twitterCategorizer.readFile("input/amazon.txt");
		// twitterCategorizer.classifyNewTweet(content);

		// content = twitterCategorizer.readFile("input/google_bad.txt");
		// twitterCategorizer.classifyNewTweet(content);
		String company = "google";
		logger.debug("Sentiment Analysis for: " + company);

		List<RssNews> rssNewsList = null;
		rssNewsList = openNLPCategorizer.getTextForSubjectNew2(company, rssSites);

		Map<String, RssNews> mapCompanyRssNews = new HashMap<>();
		RssNews tempRssNews = null;
		for (RssNews rssNews : rssNewsList) {
			String rssCompany = rssNews.getRssCompany();
			if (mapCompanyRssNews.containsKey(rssCompany)) {
				tempRssNews = mapCompanyRssNews.get(rssCompany);
				if (rssNews.getRssStory().size() > 0) {
					tempRssNews.getRssStory().add(rssNews.getRssStory().get(0));
				}

			} else {
				tempRssNews = new RssNews();
				tempRssNews.setRssCompany(rssCompany);

				if (rssNews.getRssStory().size() > 0) {
					tempRssNews.getRssStory().add(rssNews.getRssStory().get(0));
				}

				mapCompanyRssNews.put(rssCompany, tempRssNews);
			}
		}

		if (!mapCompanyRssNews.isEmpty()) {
			int sentimentScore = 0;
			String story1 = null;
			Map<String, RssSentiment> mapRssCompanySentimentScore = new HashMap<>();

			for (Entry<String, RssNews> entry : mapCompanyRssNews.entrySet()) {

				RssNews rssNews = entry.getValue();
				String rssCompany = rssNews.getRssCompany();
				System.out.println("Rss Site: " + rssCompany);

				List<RssStory> storyList = rssNews.getRssStory();

				for (RssStory story : storyList) {

					logger.debug(story.getTitle());
					story1 = story.getStory();

					if (!story1.equals(NO_NEWS_FOUND)) {
						sentimentScore = openNLPCategorizer.classifyStorySentiment(story1);
						openNLPCategorizer.addToRssSentimentScores(rssCompany, sentimentScore,
								mapRssCompanySentimentScore);
					}
					logger.debug("Sentiment score: " + sentimentScore);
					System.out.println("Story title: " + story.getTitle());
					System.out.println("Sentiment score: " + sentimentScore);
				}
			}
			
			List<RssSentiment> rssSentimentList = new ArrayList<>();
			
			for (Entry<String, RssSentiment> entry : mapRssCompanySentimentScore.entrySet()) {
				String rssCompany = entry.getKey();
				RssSentiment rssSentiment = entry.getValue();
				
				rssSentimentList.add(rssSentiment);
				System.out.println("Rss Site: " + rssCompany);
				System.out.println("Overall sentiment score: " + rssSentiment.getAverageSentimentScore());
			}
			
		    String jsonInString = mapper.writeValueAsString(rssSentimentList);
			System.out.println(jsonInString);
		}

		// contents = openNLPCategorizer.getTextForSubjectNew(company,
		// rssSites);
		// if (!contents.isEmpty()) {
		//
		// String content = null;
		// for (int i = 0; i < contents.size(); i++) {
		// content = contents.get(i);
		//
		// logger.debug(content);
		// if (!StringUtils.isEmpty(content) && !content.equals(NO_NEWS_FOUND))
		// {
		// openNLPCategorizer.classifyNewTweet(content);
		// }
		// }
		// }

		// twitterCategorizer.DocumentCategorizer(content);
	}

	public void addToRssSentimentScores(String rssCompany, int sentimentScore,
			Map<String, RssSentiment> mapRssCompanySentimentScore) {
		if (mapRssCompanySentimentScore.containsKey(rssCompany)) {
			RssSentiment rssSentiment = mapRssCompanySentimentScore.get(rssCompany);
			rssSentiment.getSentimentScore().add(sentimentScore);
			
		} else {
			RssSentiment rssSentiment = new RssSentiment();
			rssSentiment.getSentimentScore().add(sentimentScore);
			mapRssCompanySentimentScore.put(rssCompany, rssSentiment);
		}
	}

	public List<RssSite> getRssSitesFromJsonFile() throws JsonParseException, JsonMappingException, IOException {
		List<RssSite> rssSitesList = new ArrayList<>();

		rssSitesList = mapper.readValue(new File("resources/rss_sites3.json"), new TypeReference<List<RssSite>>() {
		});

		return rssSitesList;
	}

	public List<String> getRssSites() {
		JSONParser parser = new JSONParser();
		List<String> siteList = new ArrayList<>();

		try {

			Object obj = parser.parse(new FileReader("resources/rss_sites.json"));

			JSONObject jsonObject = (JSONObject) obj;

			JSONArray companyList = (JSONArray) jsonObject.get("RSS List");

			System.out.println("\nSite List:");
			Iterator<String> iterator = companyList.iterator();
			while (iterator.hasNext()) {
				String site = iterator.next();
				siteList.add(site);
				System.out.println(site);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return siteList;
	}

	public void writeProperties() {
		Properties prop = new Properties();
		OutputStream output = null;

		try {

			output = new FileOutputStream("config.properties");

			// set the properties value
			prop.setProperty("database", "localhost");
			prop.setProperty("dbuser", "mkyong");
			prop.setProperty("dbpassword", "password");

			// save properties to project root folder
			prop.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public void loadProperties(String fileName) {
		Properties prop = new Properties();
		InputStream input = null;

		try {

			// input =
			// OpenNLPCategorizer.class.getClassLoader().getResourceAsStream(fileName);
			// // new
			input = new FileInputStream(fileName);

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			System.out.println(prop.getProperty("dbpassword"));

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public List<String> getTextForSubjectNew(String subject, List<RssSite> rssSites)
			throws IOException, InterruptedException, ExecutionException {
		List<String> results = new ArrayList<>();
		int rssSitesSize = rssSites.size();

		ExecutorService es = Executors.newFixedThreadPool(20);
		List<Callable<String>> todoList = new ArrayList<Callable<String>>(rssSitesSize);

		for (RssSite rssSite : rssSites) {
			List<String> feedList = rssSite.getRssFeeds();

			for (String feed : feedList) {
				todoList.add(new DoRSSRead(feed, subject));
			}
		}

		List<Future<String>> answers = es.invokeAll(todoList);

		for (Future<String> answer : answers) {
			results.add(answer.get());
		}

		es.shutdown();

		return results;
	}

	public List<RssNews> getTextForSubjectNew2(String subject, List<RssSite> rssSites)
			throws IOException, InterruptedException, ExecutionException {
		List<RssNews> results = new ArrayList<>();
		int rssSitesSize = rssSites.size();

		ExecutorService es = Executors.newFixedThreadPool(20);
		List<Callable<RssNews>> todoList = new ArrayList<Callable<RssNews>>(rssSitesSize);

		for (RssSite rssSite : rssSites) {
			List<String> feedList = rssSite.getRssFeeds();
			String rssCompany = rssSite.getCompany();

			for (String feed : feedList) {
				todoList.add(new DoRSSRead2(feed, rssCompany, subject));
			}
		}

		List<Future<RssNews>> answers = es.invokeAll(todoList);

		for (Future<RssNews> answer : answers) {
			results.add(answer.get());
		}

		es.shutdown();

		return results;
	}

	public List<String> getTextForSubject(String subject, List<String> rssSites) throws IOException {
		List<String> newsList = new ArrayList<>();
		String rssSite = null;
		String text = null;

		for (int i = 0; i < rssSites.size(); i++) {

			rssSite = rssSites.get(i);
			RSSFeedParser parser = new RSSFeedParser(rssSite);

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
					newsList.add(text);
				} else {
					System.out.println("No news found");
				}
			} catch (RuntimeException rex) {
				logger.error("No news found for: " + rssSite, rex);
			} catch (Exception ex) {
				logger.error("No news found for: " + rssSite, ex);
			}
		}

		return newsList;
	}

	@SuppressWarnings("rawtypes")
	public void trainModel() {
		InputStream dataIn = null;
		try {
			dataIn = new FileInputStream("input/traindata.txt");
			// dataIn = new FileInputStream("input/en-chunker.bin");
			ObjectStream lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
			ObjectStream sampleStream = new DocumentSampleStream(lineStream);
			// Specifies the minimum number of times a feature must be seen
			int cutoff = 2;
			int trainingIterations = 30;
			model = DocumentCategorizerME.train("en", sampleStream, cutoff, trainingIterations);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (dataIn != null) {
				try {
					dataIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void DocumentCategorizer(String text) throws IOException {

		File test = new File("input/en-token.bin");
		String classificationModelFilePath = test.getAbsolutePath();
		DocumentCategorizerME classificationME = new DocumentCategorizerME(
				new DoccatModel(new FileInputStream(classificationModelFilePath)));
		String documentContent = text;
		double[] classDistribution = classificationME.categorize(documentContent);

		String predictedCategory = classificationME.getBestCategory(classDistribution);
		System.out.println("Model prediction : " + predictedCategory);

	}

	public void classifyNewTweet(String tweet) {
		DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);
		double[] outcomes = myCategorizer.categorize(tweet);

		System.out.println("Before myCategorizer.getBestCategory");
		String category = myCategorizer.getBestCategory(outcomes);
		// String allResults = myCategorizer.

		System.out.println("After myCategorizer.getBestCategory");

		if (category.equalsIgnoreCase("1")) {
			System.out.println("The tweet is positive :) ");
		} else {
			System.out.println("The tweet is negative :( ");
		}
	}

	public int classifyStorySentiment(String tweet) {
		DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);
		double[] outcomes = myCategorizer.categorize(tweet);

		// System.out.println("Before myCategorizer.getBestCategory");
		String category = myCategorizer.getBestCategory(outcomes);
		// String allResults = myCategorizer.

		// System.out.println("After myCategorizer.getBestCategory");

		// if (category.equalsIgnoreCase("1")) {
		// System.out.println("The tweet is positive :) ");
		// } else {
		// System.out.println("The tweet is negative :( ");
		// }

		return Integer.parseInt(category);
	}

	protected String readFile(String fileName) {
		File file = new File(fileName);
		String str = null;
		try {
			str = FileUtils.readFileToString(file, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}
}