package com.firstpeak.json;

import java.util.ArrayList;
import java.util.List;

public class RssSentiment {
	String rssCompany;
	List<Integer> sentimentScore;
	double averageSentimentScore;

	public String getRssCompany() {
		return rssCompany;
	}

	public void setRssCompany(String rssCompany) {
		this.rssCompany = rssCompany;
	}

	public List<Integer> getSentimentScore() {
		if (sentimentScore == null) {
			sentimentScore = new ArrayList<>();
		}

		return sentimentScore;
	}

	public void setSentimentScore(List<Integer> sentimentScore) {
		this.sentimentScore = sentimentScore;
	}

	public double getAverageSentimentScore() {

		int sum = 0;
		int len = sentimentScore.size();
		for (int i = 0; i < len; i++) {
			sum = sum + sentimentScore.get(i);
		}
		// calculate average
		averageSentimentScore = sum / len;
		return averageSentimentScore;
	}
}
