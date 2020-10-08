package com.ronicy.admin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FCM implements Serializable{

	private String token;
	private Date updatedDate;
	private List<String> subscribedTopics;
	private String uid;

	public FCM() {

	}

	public FCM(String token, String[] topics) {
		this.token = token;
		this.updatedDate = new Date();
		this.subscribedTopics = new ArrayList<>();
		for(String topic: topics) {
			this.subscribedTopics.add(topic);
		}
	}

	public FCM(String token, String[] topics, String uid) {
		this.token = token;
		this.updatedDate = new Date();
		this.subscribedTopics = new ArrayList<>();
		for(String topic: topics) {
			this.subscribedTopics.add(topic);
		}
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

	public List<String>  getSubscribedTopics() {
		return subscribedTopics;
	}

	public void setSubscribedTopics(List<String>  subscribedTopics) {
		this.subscribedTopics = subscribedTopics;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

}
