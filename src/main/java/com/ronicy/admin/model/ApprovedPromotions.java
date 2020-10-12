package com.ronicy.admin.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ApprovedPromotions {

	private String advertismentID;
	private Date urgentPromoExpireTime;
	private Date dailyPromoPromoExpireTime;
	private Date spotLightPromoExpireTime;
	private Date topAdPromoExpireTime;
	private Date bundleAdPromoExpireTime;
	private Boolean stopPromotions;
	List<String> notes;
	List<String> promoIDs;

	public ApprovedPromotions() {

	}

	public ApprovedPromotions(String advertismentID, Boolean stopPromotions) {
		super();
		this.advertismentID = advertismentID;
		this.stopPromotions = stopPromotions;
		this.notes = new ArrayList();
		this.promoIDs = new ArrayList();
	}

	public String getAdvertismentID() {
		return advertismentID;
	}

	public void setAdvertismentID(String advertismentID) {
		this.advertismentID = advertismentID;
	}

	public Date getUrgentPromoExpireTime() {
		return urgentPromoExpireTime;
	}

	public void setUrgentPromoExpireTime(Date urgentPromoExpireTime) {
		this.urgentPromoExpireTime = urgentPromoExpireTime;
	}

	public Date getDailyPromoPromoExpireTime() {
		return dailyPromoPromoExpireTime;
	}

	public void setDailyPromoPromoExpireTime(Date dailyPromoPromoExpireTime) {
		this.dailyPromoPromoExpireTime = dailyPromoPromoExpireTime;
	}

	public Date getSpotLightPromoExpireTime() {
		return spotLightPromoExpireTime;
	}

	public void setSpotLightPromoExpireTime(Date spotLightPromoExpireTime) {
		this.spotLightPromoExpireTime = spotLightPromoExpireTime;
	}

	public Date getTopAdPromoExpireTime() {
		return topAdPromoExpireTime;
	}

	public void setTopAdPromoExpireTime(Date topAdPromoExpireTime) {
		this.topAdPromoExpireTime = topAdPromoExpireTime;
	}

	public Boolean getStopPromotions() {
		return stopPromotions;
	}

	public void setStopPromotions(Boolean stopPromotions) {
		this.stopPromotions = stopPromotions;
	}

	public List<String> getNotes() {
		return notes;
	}

	public void setNotes(List<String> notes) {
		this.notes = notes;
	}

	public List<String> getPromoIDs() {
		return promoIDs;
	}

	public void setPromoIDs(List<String> promoIDs) {
		this.promoIDs = promoIDs;
	}

	public Date getBundleAdPromoExpireTime() {
		return bundleAdPromoExpireTime;
	}

	public void setBundleAdPromoExpireTime(Date bundleAdPromoExpireTime) {
		this.bundleAdPromoExpireTime = bundleAdPromoExpireTime;
	}

}
