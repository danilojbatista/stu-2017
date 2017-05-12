package com.ibm.br.ltc.rte.producers;

import java.lang.reflect.Type;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.ibm.br.ltc.rte.entity.FacebookPost;
import com.ibm.br.ltc.rte.entity.FacebookUser;

/**
 * 
 * @author <a href="mailto:diogo.fabrile@sprint.com">Diogo Favero Fabrile</a>
 *
 *         Producer for the gson object
 */
public class GsonProducer {

	@Produces
	public Gson gsonProducer(InjectionPoint injectionPoint) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder = gsonBuilder.registerTypeAdapter(FacebookPost.class, new FacebookPostSerializer());
		gsonBuilder = gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return gsonBuilder.create();
	}
}

class FacebookPostSerializer implements JsonSerializer<FacebookPost> {

	@Override
	public JsonElement serialize(FacebookPost post, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject root = new JsonObject();
		root.addProperty("id", post.getId());
		root.addProperty("permalink_url", strValue(post.getPermanentLink()));
		root.addProperty("date", post.getDate());
		root.addProperty("type", strValue(post.getType()));
		root.addProperty("message", post.getMessage());
		root.addProperty("feedType", strValue(post.getFeedType().toString()));
		root.addProperty("media", strValue(post.getPicture()));
		
		FacebookUser userFrom = post.getUserFrom();
		if(userFrom != null){
			root.addProperty("name", userFrom.getName());
			root.addProperty("pictureUrl", strValue(userFrom.getProfilePicture()));
		}

		return root;
	}

	private static String strValue(Object obj){
		if(obj == null){
			return "";
		}
		
		return obj.toString();
	}
}