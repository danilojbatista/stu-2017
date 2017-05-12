package com.ibm.br.ltc.rte.entity;

import java.net.URL;

public class FacebookUser {

	private String id;
	private String name;
	private URL profilePicture;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public URL getProfilePicture() {
		return profilePicture;
	}

	public void setProfilePicture(URL profilePicture) {
		this.profilePicture = profilePicture;
	}

	@Override
	public String toString() {
		return String.format("Facebook user: id = %s, name = %s, profilePictureURL = %s", this.id, this.name,
				this.profilePicture);
	}

}
