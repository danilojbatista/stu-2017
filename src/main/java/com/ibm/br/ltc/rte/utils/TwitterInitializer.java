package com.ibm.br.ltc.rte.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.ibm.br.ltc.rte.entity.Tweet;
import twitter4j.FilterQuery;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * 
 * @author <a href="mailto:diogo.fabrile@sprint.com">Diogo Favero Fabrile</a>
 *
 *         This class create a connection stream to receive tweets
 */
public class TwitterInitializer {
	/**
	 * parameters needed to open the connection stream. Need to register your application in apps.twitter.com
	 */
	public static final String CONSUMER_KEY = "Fp2VmYwkfYDYGAcp0dy4JR70a";

	public static final String CONSUMER_SECRET = "dPuQI83F1sNrgvw34cM4JUCXD1YYT1rDpqrZbuZjG9WiMa8nVd";

	public static final String ACCESS_TOKEN = "3401727471-4jMMzLwywXhzxdWYpw6qq6C2SCGHl18EQgyFoHV";

	public static final String TOKEN_SECRET = "ppFTT7mhvIhn0e57UcKXgbBlx9xKLTAJTQWi4u28pvqeH";
	

	/**
	 * Start listening the stream.
	 * 
	 * @param args
	 *            Hashtags that will be listened
	 * @param listener
	 *            The listener that contains the business logic
	 */
	public void startListening(String[] args, StatusListener listener) {

		// Retrieving configuration.
		ConfigurationBuilder configurationBuilder = getConfiguration();

		TwitterStream twitterStream = new TwitterStreamFactory(
				configurationBuilder.build()).getInstance();
		twitterStream.addListener(listener);
		ArrayList<Long> follow = new ArrayList<Long>();
		ArrayList<String> track = new ArrayList<String>();
		for (String arg : args) {
			if (isNumericalArgument(arg)) {
				for (String id : arg.split(",")) {
					follow.add(Long.parseLong(id));
				}
			} else {
				track.addAll(Arrays.asList(arg.split(",")));
			}
		}
		long[] followArray = new long[follow.size()];
		for (int i = 0; i < follow.size(); i++) {
			followArray[i] = follow.get(i);
		}
		String[] trackArray = track.toArray(new String[track.size()]);

		// filter() method internally creates a thread which manipulates
		// TwitterStream and calls these adequate listener methods continuously.
		twitterStream.filter(new FilterQuery(0, followArray, trackArray));
	}

	/**
	 * Execute an initial query to get the last tweets.
	 * 
	 * @param hashtag
	 *            Hashtag that will be searched
	 * @param number
	 *            Quantity of tweets.
	 * @return
	 * @throws TwitterException
	 */
	public List<Tweet> getTweets(String hashtag, int number)
			throws TwitterException {

		// Get the configuration.
		ConfigurationBuilder configuration = getConfiguration();

		Twitter twitter = new TwitterFactory(configuration.build())
				.getInstance();

		// Create the query that will be executed.
		Query query = new Query( hashtag);
		query.setCount(30);
		
		// Search for the results.
		QueryResult search = twitter.search(query);

		// Get all tweets
		List<Status> tweets = search.getTweets();

		List<Tweet> rtweets = new ArrayList<Tweet>();

		// Iterate all tweets and parse to the application Tweet object.
		for (Status status : tweets) {
			rtweets.add(TwitterInitializer.convertToTweet(status));
		}

		Collections.reverse(rtweets);

		return rtweets;
	}
	
	/**
	 * Assembles a query for more than one hashtag
	 * @param hashtags Array list with more than one hashtag
	 * @param number max number of tweets  
	 * @return
	 * @throws TwitterException 
	 */
	public List<Tweet> getTweets(String[] hashtags, int number) throws TwitterException{		
		
		StringBuilder query = new StringBuilder();
		
		query.append("(").append(hashtags[0]).append(")");
				
		for (int i =1; i< hashtags.length;i++){			
			query.append(" OR ");		
			query.append("(").append(hashtags[i]).append(")");
		}		
		
		return getTweets(query.toString(), number);
		
	}
		
	
	/**
	 * 
	 * @return return Configuration that will be used to connection with the stream
	 */
	private ConfigurationBuilder getConfiguration() {
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setDebugEnabled(true)
				.setOAuthConsumerKey(TwitterInitializer.CONSUMER_KEY)
				.setOAuthConsumerSecret(TwitterInitializer.CONSUMER_SECRET)
				.setOAuthAccessToken(TwitterInitializer.ACCESS_TOKEN)
				.setOAuthAccessTokenSecret(TwitterInitializer.TOKEN_SECRET);
		return configurationBuilder;
	}
	
	
	/**
	 * Check if it's a numerical 
	 * @param argument
	 * @return
	 */
	private static boolean isNumericalArgument(String argument) {
		String args[] = argument.split(",");
		boolean isNumericalArgument = true;
		for (String arg : args) {
			try {
				Integer.parseInt(arg);
			} catch (NumberFormatException nfe) {
				isNumericalArgument = false;
				break;
			}
		}
		return isNumericalArgument;
	}
	
	/**
	 * Convert the Twitter4j object to 
	 * @param status
	 * @return
	 */
	public static Tweet convertToTweet(Status status) {

		Tweet tweet = new Tweet();
		tweet.setMessage(status.getText());
		tweet.setCompleteName(status.getUser().getName());
		tweet.setName(status.getUser().getScreenName());
		tweet.setPictureUrl(status.getUser().getBiggerProfileImageURL());
		tweet.setDate(status.getCreatedAt().getTime());
		
		if(status.getMediaEntities() != null && status.getMediaEntities().length > 0 ){
			tweet.setMedia(status.getMediaEntities()[0].getMediaURL());
		}
		return tweet;
	}

}
