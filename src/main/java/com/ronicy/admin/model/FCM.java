package com.ronicy.admin.model;

import java.util.Date;

public class FCM {

	private String token;
	private Date updatedDate;
	private String[] subscribedTopics;
	private String uid;

	public FCM() {

	}

	public FCM(String token, String[] topics) {
		this.token = token;
		this.updatedDate = new Date();
		this.subscribedTopics = topics;
	}

	public FCM(String token, String[] topics, String uid) {
		this.token = token;
		this.updatedDate = new Date();
		this.subscribedTopics = topics;
		this.uid = uid;
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

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

}
