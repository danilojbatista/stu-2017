package com.ibm.br.ltc.rte.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.ibm.br.ltc.rte.entity.FacebookPage;
import com.ibm.br.ltc.rte.entity.FacebookPost;
import com.ibm.br.ltc.rte.utils.FacebookAccessTokenUtil;
import com.ibm.br.ltc.rte.utils.FacebookConfig;
import com.ibm.br.ltc.rte.utils.FacebookInitializer;
import com.ibm.br.ltc.rte.utils.WebSocketUtil;
import com.ibm.br.ltc.rte.webservice.FacebookWebServiceCaller;
import com.ibm.br.ltc.rte.webservice.FacebookWsException;

import facebook4j.Account;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.ResponseList;
import facebook4j.auth.AccessToken;

@Singleton
@Startup
@LocalBean
public class FacebookSingleton {

	private FacebookAuthStatus authStatus;
	private AccessToken accessToken;
	private FacebookPage page;
	private List<FacebookPost> facebookPosts;
	private Session session = null;

	private long lastUpdateToFacebook;
	private static boolean facebookError = false;
	private static String facebookPageName;

	@Resource
	private TimerService timerService;

	private static final int POST_LIMIT = 30;

	@Inject
	private FacebookInitializer facebookInitializer;

	@Inject
	private Facebook facebook4j;

	@Inject
	private Logger logger;

	@Inject
	private FacebookWebServiceCaller facebookWsCaller;

	@Inject
	private Gson gson;

	/**
	 * Default constructor. Initializes {@link FacebookAuthStatus} instance
	 * variable.
	 */
	public FacebookSingleton() {
		this.authStatus = FacebookAuthStatus.NOT_INITILIAZED;
	}

	@PostConstruct
	public void init() {

		FacebookConfig facebookConfig = facebookInitializer.getFacebookConfig();
		if (facebookConfig == null) {
			this.logger.fatal("FACEBOOK COULD NOT BE CONFIGURED PROPERLY DUE TO FACEBOOK CONFIG IS NULL");
			return;
		}

		FacebookSingleton.facebookPageName = facebookConfig.getFacebookPageName();
		this.lastUpdateToFacebook = facebookConfig.getInitialTimeStamp();

		String accessTokenStr = facebookConfig.getStoredAccessToken();
		if (accessTokenStr != null) {
			this.accessToken = new AccessToken(accessTokenStr);
			this.facebook4j.setOAuthAccessToken(this.accessToken);
			this.setAuthStatusToAuthenticated();
			this.configureFacebookPage();
		}

		List<ScheduleExpression> facebookTimerExpresisons = facebookConfig.getFacebookTimerExpresisons();
		if (facebookTimerExpresisons != null) {
			this.configureFacebookTimers(facebookTimerExpresisons);
		}

	}

	private void configureFacebookTimers(List<ScheduleExpression> expressions) {
		for (ScheduleExpression expression : expressions) {
			TimerConfig timerConfig = new TimerConfig();
			timerConfig.setPersistent(false);

			this.timerService.createCalendarTimer(expression, timerConfig);
			this.logger.info("Timer successfuly created! It will start at " + expression.getStart()
					+ " and it will end at " + expression.getEnd());
		}
	}

	public void setAccessToken(String oauthcode) {
		try {
			this.facebook4j.getOAuthAccessToken(oauthcode);
			this.accessToken = this.facebook4j.extendTokenExpiration();
			this.setAuthStatusToAuthenticated();
			this.configureFacebookPage();

			FacebookAccessTokenUtil.saveAccessToken(this.accessToken);
		} catch (FacebookException e) {
			logger.error("Error during obtaining access token process", e);
			this.setAuthStatusToAuthFailed();
			
		}
	}

	private boolean configureFacebookPage() {
		ResponseList<Account> accounts;

		try {
			accounts = facebook4j.getAccounts();
		} catch (FacebookException e) {
			logger.error("Error during obtaining access token process", e);

			return false;
		}

		if (accounts == null) {
			IllegalStateException ex = new IllegalStateException("There is no page associated with the facebook");
			logger.error("There is no page associated with the facebook", ex);
			throw ex;
		}

		logger.debug("Accounts reference associated with facebook4j object is not null. Number of accounts: "
				+ accounts.size());

		for (Account pageAccount : accounts) {
			String currentPageName = pageAccount.getName();
			if (!FacebookSingleton.facebookPageName.equalsIgnoreCase(currentPageName)) {
				continue;
			}

			String pageAccessToken = pageAccount.getAccessToken();
			String pageId = pageAccount.getId();

			logger.debug("pageAccessToken = " + pageAccessToken);
			logger.debug("pageId = " + pageId);

			this.page = new FacebookPage();
			this.page.setAccessToken(pageAccessToken);
			this.page.setId(pageId);

			if (this.facebookPosts == null) {
				long callTime = System.currentTimeMillis();
				this.facebookPosts = this.retrievePosts();
				this.lastUpdateToFacebook = callTime;
			}
		}

		return true;
	}

	public Facebook getFacebook4j() {
		return facebook4j;
	}

	/**
	 * Set the endpoint session that will be used to send the posts
	 * 
	 * @param session
	 *            receive the WebSocket session.
	 */
	public void setSession(Session session) {
		if (this.session == null) {
			this.session = session;
		}
	}

	@Timeout
	public void retrieveNewPost() {
		if (FacebookSingleton.facebookError) {
			this.logger.debug("Occurred an error on Facebook side, so call will not be performed");
			return;
		}

		if (!FacebookSingleton.facebookError && this.isAuthenticated()) {
			logger.debug("Retrieving facebook Posts ");
			List<FacebookPost> newPosts = this.retrievePosts();
			if (newPosts != null && !newPosts.isEmpty()) {
				logger.debug("New facebook posts retrieved: # of posts " + newPosts.size());
				for (FacebookPost newPost : newPosts) {
					this.addFacebookPost(newPost);
					if (newPost.getDate() > this.lastUpdateToFacebook) {
						this.lastUpdateToFacebook = newPost.getDate();
					}
				}
			}
		}
	}

	private List<FacebookPost> retrievePosts() {
		long lastUpdateFromFacebookInSeconds = this.lastUpdateToFacebook / 1000L;
		List<FacebookPost> retrievedPosts = null;
		try {
			retrievedPosts = this.facebookWsCaller.retrievePageTaggedPosts(this.page, POST_LIMIT,
					lastUpdateFromFacebookInSeconds);
		} catch (FacebookWsException e) {
			this.logger.error(e.getMessage(), e);
			FacebookSingleton.facebookError = true;
			
			WebSocketUtil.sendMessages("facebookError", this.session);
		}

		return retrievedPosts;
	}

	/**
	 * Add a new Tweet and remove the oldest of the 30
	 * 
	 * @param tweet
	 */
	private void addFacebookPost(FacebookPost newPost) {
		if (this.facebookPosts == null) {
			this.facebookPosts = new ArrayList<>();
		}

		if (this.facebookPosts.size() >= 30) {
			this.facebookPosts.remove(0);
		}

		this.facebookPosts.add(newPost);
		String msg = gson.toJson(newPost);

		logger.debug("Sending message for all clients from FacebookSingleton :" + msg);
		WebSocketUtil.sendMessages(msg, this.session);

	}

	public List<FacebookPost> getCachedFacebookPosts() {
		if (!this.isAuthenticated()) {
			IllegalStateException ex = new IllegalStateException(
					"The application tried to retrieve facebook posts while it was not authenticated");
			logger.error("Facebook authentication process was not successfuly completed yet.", ex);

			throw ex;
		}

		return this.facebookPosts;
	}

	public boolean isAuthenticated() {
		boolean isStatusAuthenticated = this.authStatus == FacebookAuthStatus.AUTHENTICATED;
		boolean isTokenValid = !this.isAuthTokenExpired();

		return isStatusAuthenticated && isTokenValid;
	}

	public boolean isAuthTokenExpired() {
		// this.accessToken.getParameter(parameter)

		if (this.accessToken == null)
			return false;

		Long tokenExpires = this.accessToken.getExpires();
		if (tokenExpires == null) {
			return false;
		}
		return this.accessToken != null && tokenExpires <= System.currentTimeMillis();
	}

	public static String getFacebookPageName(){
		return !facebookError ? "@" + FacebookSingleton.facebookPageName : "";
	}
	
	public void setAuthStatusToAuthenticated() {
		this.authStatus = FacebookAuthStatus.AUTHENTICATED;
	}

	public void setAuthStatusToRequestSent() {
		this.authStatus = FacebookAuthStatus.REQUEST_SENT_FACEBOOK;
	}

	public void setAuthStatusToAuthFailed() {
		this.authStatus = FacebookAuthStatus.AUTH_FAILED;
	}

	public boolean hasAuthenticationFailed() {
		return this.authStatus == FacebookAuthStatus.AUTH_FAILED;
	}

	public boolean hasNotBeingInitialized() {
		return this.authStatus == FacebookAuthStatus.NOT_INITILIAZED;
	}

	public FacebookAuthStatus getAuthStatus() {
		return authStatus;
	}

	public int getPostLimit() {
		return FacebookSingleton.POST_LIMIT;
	}

	private enum FacebookAuthStatus {
		NOT_INITILIAZED, REQUEST_SENT_FACEBOOK, AUTHENTICATED, AUTH_FAILED;
	};
}
