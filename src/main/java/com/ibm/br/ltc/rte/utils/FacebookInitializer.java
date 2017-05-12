package com.ibm.br.ltc.rte.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.ejb.ScheduleExpression;
import javax.inject.Inject;

import org.apache.log4j.Logger;

public class FacebookInitializer {

	private static final String FACEBOOK_PAGE_PROPERTY = "FACEBOOK_PAGE";
	private static final String TIMER_PREFIX_PROPERTY = "TIMER_";
	private static final String TIMER_START_PROPERTY = TIMER_PREFIX_PROPERTY + "START_";
	private static final String TIMER_END_PROPERTY = TIMER_PREFIX_PROPERTY + "END_";
	private static final String TIMER_SEC_INTERVAL_PROPERTY = TIMER_PREFIX_PROPERTY + "SEC_INTERVAL_";
	private static final String ACCESS_TOKEN_PROPERTY = "FB_ACCESS_TOKEN";
	private static final String INITIAL_TIMESTAMP_PROPERTY = "INITIAL_TIME_STAMP";

	@Inject
	private Logger logger;
	
	public FacebookConfig getFacebookConfig() {
		String facebookPageName = System.getenv(FACEBOOK_PAGE_PROPERTY);
		String accessToken = System.getenv(ACCESS_TOKEN_PROPERTY);
		String initialTimeStampStr = System.getenv(INITIAL_TIMESTAMP_PROPERTY);

		if (facebookPageName == null || facebookPageName.trim().isEmpty()) {
			logger.error("There is no env property for Facebook Page!", new IllegalStateException());
			return null;
		}

		long initialTimeStamp = 0;

		if (initialTimeStampStr != null) {
			initialTimeStamp = Long.parseLong(initialTimeStampStr);
		}

		FacebookConfig facebookConfig = new FacebookConfig();
		facebookConfig.facebookPageName(facebookPageName).initialTimeStamp(initialTimeStamp)
				.storedAccessToken(accessToken);
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("Brazil/East"));
		
		String currentStartTime;
		int i = 1;
		while((currentStartTime = System.getenv(TIMER_START_PROPERTY + i)) != null){
			try {
				
				String currentEndTime = System.getenv(TIMER_END_PROPERTY + i);
				String secIntervalStr = System.getenv(TIMER_SEC_INTERVAL_PROPERTY + i);
				
				if(currentEndTime == null){
					throw new IllegalStateException("There is no end time for the timer number " + i);
				}
				
				if(secIntervalStr == null){
					throw new IllegalStateException("The sec interval must be determined for each timer");
				}
				
				Date start = sdf.parse(currentStartTime);
				Date end = sdf.parse(currentEndTime);
				int secInterval = Integer.parseInt(secIntervalStr);
				
				ScheduleExpression expression = new ScheduleExpression();
				expression.start(start).end(end);
				expression.second("*/" + secInterval).minute("*").hour("*");
				
				facebookConfig.addFacebookTimerExpression(expression);
				i++;
				
			} catch (ParseException | IllegalStateException e) {
				this.logger.error("An error occurred while creating Facebook Timers", e);
				break;
			}
		}
		
		return facebookConfig;
	}

}
