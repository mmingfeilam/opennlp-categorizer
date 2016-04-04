package com.firstpeak.json;

import java.util.ArrayList;
import java.util.List;

public class RssSite {
	String company;
	List<String> rssFeeds;
	
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public List<String> getRssFeeds() {
		if(rssFeeds == null) {
			rssFeeds = new ArrayList<>();
		}
		
		return rssFeeds;
	}
	public void setRssFeeds(List<String> rssFeeds) {
		this.rssFeeds = rssFeeds;
	}
}
