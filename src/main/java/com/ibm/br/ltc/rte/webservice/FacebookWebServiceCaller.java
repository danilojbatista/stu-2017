package com.ibm.br.ltc.rte.webservice;

import java.net.MalformedURLException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.ibm.br.ltc.rte.entity.FacebookPage;
import com.ibm.br.ltc.rte.entity.FacebookPost;
import com.ibm.br.ltc.rte.entity.FacebookUser;

/**
 * 
 * @author <a href="mailto:daniloj@br.ibm.com">Danilo Jablonskis Batista</a>
 *
 *         Web Service Caller implementation for Facebook Web Service
 */
@Stateless
@LocalBean
public class FacebookWebServiceCaller extends AbstractWebServiceCaller {

	private static final String FACEBOOK_API_URL = "https://graph.facebook.com/v2.7/";

	@Inject
	private Logger logger;

	@Inject
	private Gson gson;

	/**
	 * 
	 * @param postList
	 */
	public void populateProfilePictureForPostList(List<FacebookPost> postList) {
		if (postList == null) {
			return;
		}

		List<FacebookUser> userList = new ArrayList<>(postList.size());
		for (FacebookPost post : postList) {
			FacebookUser userFrom = post.getUserFrom();
			if (userFrom != null) {
				userList.add(userFrom);
			}
		}

		this.populateProfilePicture(userList);
	}

	public void populateProfilePicture(List<FacebookUser> userList) {
		if (userList == null) {
			return;
		}

		for (FacebookUser user : userList) {
			this.populateProfilePicture(user);
		}
	}

	public void populateProfilePicture(FacebookUser user) {
		Client client = this.getClient();
		String path = String.format("%s/picture", user.getId());
		WebTarget target = client.target(FACEBOOK_API_URL).path(path).queryParam("redirect", false);

		Response responseWS = this.doGetAPICall(target);
		String jsonAsString = responseWS.readEntity(String.class);

		JsonElement element = new JsonParser().parse(jsonAsString);
		JsonObject jsonObject = element.getAsJsonObject();

		if (jsonObject == null)
			return;

		JsonElement dataElement = jsonObject.get("data");
		if (dataElement == null)
			return;

		JsonElement urlElement = dataElement.getAsJsonObject().get("url");
		if (urlElement == null)
			return;

		String pictureURL = urlElement.getAsString();

		try {
			user.setProfilePicture(new URL(pictureURL));
		} catch (MalformedURLException e) {
			logger.error("Exception during the conversion of profile picture url", e);
		}
	}

	public List<FacebookPost> retrievePageTaggedPosts(FacebookPage page) throws FacebookWsException {
		return this.retrievePageTaggedPosts(page, 0);
	}

	public List<FacebookPost> retrievePageTaggedPosts(FacebookPage page, int postLimit) throws FacebookWsException {
		return this.retrievePageTaggedPosts(page, postLimit, 0);
	}

	public List<FacebookPost> retrievePageTaggedPosts(FacebookPage page, int postLimit, long lastTimeInSeconds) throws FacebookWsException {
		Client client = this.getClient();
		WebTarget target = client.target(FACEBOOK_API_URL).path(page.getId() + "/tagged")
				.queryParam("access_token", page.getPageAccessToken())
				.queryParam("fields", "permalink_url,created_time,from,full_picture,message,type");

		if (postLimit != 0) {
			target = target.queryParam("limit", postLimit);
		}

		if (lastTimeInSeconds != 0) {
			target = target.queryParam("since", lastTimeInSeconds);
		}

		Response responseWS = this.doGetAPICall(target);
		MultivaluedMap<String, Object> headers = responseWS.getHeaders();

		if (headers != null && headers.containsKey("x-page-usage")) {
			List<Object> xPageUsage = headers.get("x-page-usage");
			this.logger.debug("x-page-usage information: " + xPageUsage);
		}

		String respJson = responseWS.readEntity(String.class);
		
		if (respJson != null && respJson.contains("error")) {
			FacebookErrorResponse errorResponse = this.getFacebookErrorResponse(respJson);
			
			String exceptionMessage = "An error occurred while calling Facebook API. The message is: " + errorResponse.getMessage();
			
			FacebookWsException facebookWsEx = new FacebookWsException(exceptionMessage);
			this.logger.error(exceptionMessage, facebookWsEx);
			throw facebookWsEx;
		}

		FacebookPostResponse response = this.gson.fromJson(respJson, FacebookPostResponse.class);
		
		if (response == null || response.getPosts() == null || response.getPosts().isEmpty())
			return null;
		
		List<FacebookPost> posts = response.getPosts();
		this.populateProfilePictureForPostList(posts);

		return posts;
	}

	private FacebookErrorResponse getFacebookErrorResponse(String respJson) {
		JsonParser parser = new JsonParser();
		JsonObject obj = parser.parse(respJson).getAsJsonObject();
		String errorObjAsStr = obj.get("error").toString();

		return this.gson.fromJson(errorObjAsStr, FacebookErrorResponse.class);
	}

}

class FacebookPostResponse {

	@SerializedName("data")
	private List<FacebookPost> posts;

	public List<FacebookPost> getPosts() {
		return posts;
	}

	public void setPosts(List<FacebookPost> posts) {
		this.posts = posts;
	}

}
