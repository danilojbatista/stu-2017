package com.ibm.br.ltc.rte.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;

import com.google.gson.Gson;
import com.ibm.br.ltc.rte.entity.Tweet;
import com.ibm.br.ltc.rte.utils.TwitterInitializer;
import com.ibm.br.ltc.rte.utils.WebSocketUtil;

/**
 * 
 * @author <a href="mailto:diogo.fabrile@sprint.com">Diogo Favero Fabrile</a>
 *
 *         Singleton responsible for receive the tweets and pass to all the
 *         WebSocket clients connected.
 */
@Singleton
@Startup
@LocalBean
public class TwitterSingleton {
	public static final String HASH_TAGS_ENV_VAR = "HASH_TAGS";
	
	private static final String DEFAULT_HASH_TAG = "#ibmtechutest";
	
	private static String[] hashTagsArray;
	private static String hashTagsAsString;

	@Inject
	private TwitterInitializer twitterInitializer;

	@Inject
	private Logger logger;

	@Inject
	private Gson gson;

	private Session session = null;

	private List<Tweet> tweets = null;

	private Date lastDateTweet = null;

	/**
	 * Initialize the twitter stream that will receive all the tweets
	 * 
	 */
	@PostConstruct
	private void init() {
		final String[] hashTags = TwitterSingleton.getHashTagsArray();

		logger.info("Initializing TwitterSingleton");

		try {
			//TODO: Ver se eh possivel passar o array ao invez do parametro
			tweets = twitterInitializer.getTweets(hashTags, 30);
		} catch (TwitterException e) {
			logger.error("Error while execute the first 30 tweets query.", e);
		}

		// Creating the lasDateTweet that wil be used to execute the pingPong
		// method.
		lastDateTweet = new Date();

		// Creating the listener that receive the tweets
		StatusListener listener = new StatusListener() {

			public void onStatus(Status status) {

				
				// Creating and adding tweet in the Array
				addTweet(TwitterInitializer.convertToTweet(status));

				// Update to the actual date
				lastDateTweet = new Date();

				logger.info("@" + status.getUser().getScreenName() + " - "
						+ status.getText());
			}

			public void onDeletionNotice(
					StatusDeletionNotice statusDeletionNotice) {
				logger.info("Got a status deletion notice id:"
						+ statusDeletionNotice.getStatusId());
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				logger.info("Got track limitation notice:"
						+ numberOfLimitedStatuses);
			}

			public void onScrubGeo(long userId, long upToStatusId) {
				logger.info("Got scrub_geo event userId:" + userId
						+ " upToStatusId:" + upToStatusId);
			}

			public void onStallWarning(StallWarning warning) {
				logger.info("Got stall warning:" + warning);
			}

			public void onException(Exception ex) {
				logger.error(ex);
			}
		};

		// starting the listener
		twitterInitializer.startListening(hashTags, listener);
	}

	/**
	 * Send the message to all the WebSocket clients.
	 * 
	 * @param msg
	 *            Message to be delivered
	 */
	private void sendMessages(String msg) {
		logger.info("Sending message for all clients :" + msg);
		WebSocketUtil.sendMessages(msg, this.session);
	}

	/**
	 * Routine that execute the "Ping Pong".It's used to keep the WebSocket
	 * alive. alive.
	 */
	@Schedule(second = "*/20", minute = "*", hour = "*", persistent = false)
	public void pingPong() {

		logger.debug("Executing PING PONG");

		// Compare the date of the last tweet receive and take the seconds.
		Date actualDate = new Date();
		long seconds = (actualDate.getTime() - lastDateTweet.getTime()) / 1000;

		// If we didn't receive any tweets in the last 20 seconds, send a Ping
		// to all clients.
		logger.debug("Seconds without request :" + seconds);
		if (seconds >= 20) {
			logger.debug("sending Ping for all clients");
			sendMessages("Ping");
		}

	}

	/**
	 * Return the last 30 tweets
	 * 
	 * @return last 30 cached tweets
	 */
	public List<Tweet> getCacheTweets() {
		// Clone tweets to reverse
		ArrayList<Tweet> reverseTweets = new ArrayList<Tweet>(tweets);
		Collections.reverse(reverseTweets);

		return reverseTweets;

	}

	/**
	 * Set the endpoint session that will be used to send the tweets
	 * 
	 * @param session
	 *            receive the WebSocket session.
	 */
	public void setSession(Session session) {
		if (this.session == null) {
			this.session = session;
		}
	}

	/**
	 * Add a new Tweet and remove the oldest of the 30
	 * 
	 * @param tweet
	 */
	private void addTweet(Tweet tweet) {
		if (tweets.size() >= 30) {
			tweets.remove(0);
		}

		tweets.add(tweet);
		sendMessages(gson.toJson(tweet));
	}
	
	private static String[] getHashTagsArray() {
		if (hashTagsArray == null) {
			String hashTagsEnvVar = System.getenv(HASH_TAGS_ENV_VAR);
			if (hashTagsEnvVar == null) {
				System.err.println(HASH_TAGS_ENV_VAR
						+ " is not defined, returning " + DEFAULT_HASH_TAG);				
				hashTagsArray = new String[] { DEFAULT_HASH_TAG };
				
			} else {
				hashTagsArray = hashTagsEnvVar.split("\\s+");				
			}
			
		}

		return hashTagsArray;
	}
	
	public static String getHashTags() {
		if (hashTagsAsString == null) {
			StringBuilder sb = new StringBuilder();
			
			String[] hashTags = getHashTagsArray();
			if (hashTags != null) {
				for (int i = 0; i < hashTags.length; i++) {
					String ht = hashTags[i];
					if (i > 0) {
						sb.append(" ");
					}
					
					sb.append(ht);
				}

				hashTagsAsString = sb.toString();
			}
		}		
		
		return hashTagsAsString;
	}
}
