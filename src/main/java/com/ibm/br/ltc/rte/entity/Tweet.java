package com.ibm.br.ltc.rte.entity;

/**
 * 
 * @author <a href="mailto:diogo.fabrile@sprint.com">Diogo Favero Fabrile</a>
 *
 * Representation of the Tweet
 *
 */
public class Tweet extends AbstractFeedPost{

	private String name;

	private String completeName;

	private String pictureUrl;
	
	private String media;
	
	private Long date;
	
	public Tweet(){
		super(FeedType.TWITTER);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCompleteName() {
		return completeName;
	}

	public void setCompleteName(String completeName) {
		this.completeName = completeName;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	public String getMedia() {
		return media;
	}

	public void setMedia(String media) {
		this.media = media;
	}
	
	@Override
	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.getMessage() == null) ? 0 : this.getMessage().hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tweet other = (Tweet) obj;
		if (this.getMessage() == null) {
			if (other.getMessage() != null)
				return false;
		} else if (!this.getMessage().equals(other.getMessage()))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
