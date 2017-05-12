package com.ibm.br.ltc.rte.entity;

import java.net.URL;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class FacebookPost extends AbstractFeedPost {

	private String id;

	@SerializedName("full_picture")
	private URL picture;

	@SerializedName("permalink_url")
	private URL permanentLink;

	@SerializedName("created_time")
	private Date createdTime;

	private PostType type;

	@SerializedName("from")
	private FacebookUser userFrom;
	
	public FacebookPost(){
		super(FeedType.FACEBOOK);
	}

	public URL getPermanentLink() {
		return permanentLink;
	}

	public void setPermanentLink(URL permanentLink) {
		this.permanentLink = permanentLink;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public PostType getType() {
		return type;
	}

	public void setType(PostType type) {
		this.type = type;
	}

	public URL getPicture() {
		return picture;
	}

	public void setPicture(URL picture) {
		this.picture = picture;
	}

	public FacebookUser getUserFrom() {
		return userFrom;
	}

	public void setUserFrom(FacebookUser userFrom) {
		this.userFrom = userFrom;
	}
	
	@Override
	public Long getDate() {
		return this.createdTime != null ? this.createdTime.getTime() : null;
	}

	public enum PostType {
		link, status, photo, video, offer
	};
}
