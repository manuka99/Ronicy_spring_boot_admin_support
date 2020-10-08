package com.ronicy.admin.model;

import java.util.Date;


public class FCM {
	
	private String token;
	private Date updatedDate;
	private String[] subscribedTopics;
	
	public FCM() {
		
	}
	
	public FCM(String token, String[] topics) {
		this.token = token;
		this.updatedDate = new Date();
		this.subscribedTopics = topics;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public Date getUpdatedDate() {
		return updatedDate;
	}
	
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	
	public String[] getSubscribedTopics() {
		return subscribedTopics;
	}
	
	public void setSubscribedTopics(String[] subscribedTopics) {
		this.subscribedTopics = subscribedTopics;
	}

}