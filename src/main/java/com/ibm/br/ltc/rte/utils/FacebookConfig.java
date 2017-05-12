package com.ibm.br.ltc.rte.utils;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.ScheduleExpression;

public class FacebookConfig {

	private long initialTimeStamp;
	private String facebookPageName;
	private String storedAccessToken;
	private List<ScheduleExpression> facebookTimerExpressions;
	
	public long getInitialTimeStamp() {
		return initialTimeStamp;
	}
	
	public FacebookConfig initialTimeStamp(long initialTimeStamp) {
		this.initialTimeStamp = initialTimeStamp;
		
		return this;
	}
	
	public void addFacebookTimerExpression(ScheduleExpression expression){
		if(this.facebookTimerExpressions == null){
			this.facebookTimerExpressions = new ArrayList<>();
		}
		
		this.facebookTimerExpressions.add(expression);
	}
	
	public List<ScheduleExpression> getFacebookTimerExpresisons() {
		return this.facebookTimerExpressions;
	}
	
	public String getFacebookPageName() {
		return facebookPageName;
	}
	
	public FacebookConfig facebookPageName(String facebookPageName) {
		this.facebookPageName = facebookPageName;
		return this;
	}
	
	public String getStoredAccessToken() {
		return storedAccessToken;
	}
	
	public FacebookConfig storedAccessToken(String storedAccessToken) {
		this.storedAccessToken = storedAccessToken;
		return this;
	}
}
