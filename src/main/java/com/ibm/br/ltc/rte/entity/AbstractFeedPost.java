package com.ibm.br.ltc.rte.entity;

public abstract class AbstractFeedPost implements Comparable<AbstractFeedPost> {
	
	private String message;
	
	private final FeedType feedType;
	
	public enum FeedType{ FACEBOOK, TWITTER }
	
	public AbstractFeedPost(FeedType feedType){
		this.feedType = feedType;
	}

	public abstract Long getDate();
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public FeedType getFeedType(){
		return this.feedType;
	}
	
	@Override
	public int compareTo(AbstractFeedPost post) {
		if(post == null || post.getDate() == null){
			if(this.getDate() != null){
				return 1;
			}
			else{
				return 0;
			}
		}
		
		if(this.getDate() == null){
			return -1;
		}
		
		return (int)(this.getDate() - post.getDate());
	}
}
