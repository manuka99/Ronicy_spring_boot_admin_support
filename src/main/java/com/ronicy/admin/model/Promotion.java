package com.ronicy.admin.model;

import java.util.Date;
import java.util.Map;

public class Promotion {
	
	private String promoID;
	private String advertisementID;
	private Date placeDate;
	private boolean approved;
	private boolean activated;
	private boolean reviewed;
	private Map<String, Integer> promos;

	public static final int DAILY_BUMP_AD = 1;
	public static final int TOP_AD = 2;
	public static final int URGENT_AD = 3;
	public static final int SPOTLIGHT_AD = 4;
	public static final int BUNDLE_AD = 5;

	public Promotion() {

	}

	public String getPromoID() {
		return promoID;
	}

	public void setPromoID(String promoID) {
		this.promoID = promoID;
	}

	public String getAdvertisementID() {
		return advertisementID;
	}

	public void setAdvertisementID(String advertisementID) {
		this.advertisementID = advertisementID;
	}
	
	public Date getPlaceDate() {
		return placeDate;
	}

	public void setPlaceDate(Date placeDate) {
		this.placeDate = placeDate;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public boolean isReviewed() {
		return reviewed;
	}

	public void setReviewed(boolean reviewed) {
		this.reviewed = reviewed;
	}

	public static int getDailyBumpAd() {
		return DAILY_BUMP_AD;
	}

	public static int getTopAd() {
		return TOP_AD;
	}

	public static int getUrgentAd() {
		return URGENT_AD;
	}

	public static int getSpotlightAd() {
		return SPOTLIGHT_AD;
	}

	public Map<String, Integer> getPromos() {
		return promos;
	}

	public void setPromos(Map<String, Integer> promos) {
		this.promos = promos;
	}

}
