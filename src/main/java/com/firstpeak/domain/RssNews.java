package com.firstpeak.domain;

import java.util.ArrayList;
import java.util.List;

public class RssNews {
	String rssCompany;
	String subject;
	List<RssStory> rssStory;
	
	public String getRssCompany() {
		return rssCompany;
	}
	public void setRssCompany(String rssCompany) {
		this.rssCompany = rssCompany;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public List<RssStory> getRssStory() {
		if(rssStory == null) {
			rssStory = new ArrayList<>();
		}
		
		return rssStory;
	}
	public void setRssStory(List<RssStory> rssStory) {
		this.rssStory = rssStory;
	}
}
