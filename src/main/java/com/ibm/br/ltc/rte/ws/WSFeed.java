package com.ibm.br.ltc.rte.ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.ibm.br.ltc.rte.bean.FacebookSingleton;
import com.ibm.br.ltc.rte.bean.TwitterSingleton;
import com.ibm.br.ltc.rte.entity.AbstractFeedPost;
import com.ibm.br.ltc.rte.entity.FacebookPost;
import com.ibm.br.ltc.rte.entity.Tweet;

/**
 * 
 * @author <a href="mailto:daniloj@br.ibm.com">Danilo Jablonskis Batista</a>
 *
 *         WebSocket endpoint that send all messages for generic type
 *         {@link AbstractFeedPost}
 */
@ServerEndpoint("/wsfeed")
public class WSFeed {

	/**
	 * Constant indicating the feed post limit 
	 */
	private static final int FEED_POST_LIMIT = 30;

	@Inject
	private FacebookSingleton facebookSingleton;
	
	@Inject
	private TwitterSingleton twitterSingleton;

	@Inject
	private Logger logger;
	
	@Inject
	private Gson gson;
	
	@OnMessage
	public void onMessage(String message, Session session) throws IOException, InterruptedException {
		if (message.equals("Pong")) {
			logger.debug("Received Pong from client " + session.getId());
		} else if (message.equals("Received tweet")) {
			logger.debug("Received post " + session.getId());
		}
	}
	
	@OnOpen
	public void onOpen(Session session) {
		this.twitterSingleton.setSession(session);
		this.facebookSingleton.setSession(session);
		
		try {
			List<AbstractFeedPost> feedToBeSent = this.getCachedFeedPosts();
			Basic basicRemote = session.getBasicRemote();
			basicRemote.sendText(this.gson.toJson(feedToBeSent));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void onClose(Session session) {
		logger.info("Connection closed");
	}
	
	private List<AbstractFeedPost> getCachedFeedPosts() {
		NavigableSet<AbstractFeedPost> mixedFeed = new TreeSet<>();

		List<Tweet> cachedTweets = this.twitterSingleton.getCacheTweets();
		List<FacebookPost> cachedFacebookPosts = this.facebookSingleton.getCachedFacebookPosts();

		boolean facebookPostsEmpty = cachedFacebookPosts == null || cachedFacebookPosts.isEmpty();
		boolean tweetsEmpty = cachedTweets == null || cachedTweets.isEmpty();

		if (facebookPostsEmpty && tweetsEmpty) {
			return Collections.emptyList();
		}
		
		if(!facebookPostsEmpty){
			mixedFeed.addAll(cachedFacebookPosts);
		}
		
		if(!tweetsEmpty){
			mixedFeed.addAll(cachedTweets);
		}

		List<AbstractFeedPost> postsToBeSent = new ArrayList<>();
		Iterator<AbstractFeedPost> descendingIterator = mixedFeed.descendingIterator();

		int numFeedPostsAdded = 0;
		while (descendingIterator.hasNext() && numFeedPostsAdded < FEED_POST_LIMIT) {
			postsToBeSent.add(descendingIterator.next());
		}

		return postsToBeSent;
	}
}
