package com.ronicy.admin.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.cloud.firestore.annotation.Exclude;

public class Advertisement {

    private String id;
	private String objectID;
	private String title;
	private String condition;
	private String description;
	private double price;
	private Date placedDate;
	private boolean availability;
	private boolean approved;
	private boolean buynow;
	private String categoryID;
	private String userID;
	private List<String> imageUrls;
	private List<Integer> numbers;
	private String location;
	private String unapprovedReason;
	private boolean reviewed;
    private Map<String, Date> promotions;

	public Advertisement() {
		this.placedDate = new Date();
		this.availability = true;
		this.approved = false;
		this.buynow = false;
		this.reviewed = false;
		this.imageUrls = new ArrayList<>();
		this.numbers = new ArrayList<>();
		this.unapprovedReason = "This ad is currently being reviewed. You will recieve a notification when we have reviewed this ad. It usually takes about 4 working hours (Office hours 07 AM - 09 PM)";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
		this.objectID = id;
	}

	public String getObjectID() {
		return objectID;
	}

	@Exclude
	public void setObjectID(String objectID) {
		this.objectID = objectID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setAvailability(boolean availability) {
		this.availability = availability;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public boolean isAvailability() {
		return availability;
	}

	public boolean isApproved() {
		return approved;
	}

	public String getCategoryID() {
		return categoryID;
	}

	public void setCategoryID(String categoryID) {
		this.categoryID = categoryID;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public Date getPlacedDate() {
		return placedDate;
	}

	public void setPlacedDate(Date placedDate) {
		this.placedDate = placedDate;
	}

	public boolean isBuynow() {
		return buynow;
	}

	public void setBuynow(boolean buynow) {
		this.buynow = buynow;
	}

	public List<String> getImageUrls() {
		return imageUrls;
	}

	public void setImageUrls(List<String> imageUrls) {
		this.imageUrls = imageUrls;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public List<Integer> getNumbers() {
		return numbers;
	}

	public void setNumbers(List<Integer> numbers) {
		this.numbers = numbers;
	}

	public String getUnapprovedReason() {
		return unapprovedReason;
	}

	public void setUnapprovedReason(String unapprovedReason) {
		this.unapprovedReason = unapprovedReason;
	}

	public boolean isReviewed() {
		return reviewed;
	}

	public void setReviewed(boolean reviewed) {
		this.reviewed = reviewed;
	}

	public Map<String, Date> getPromotions() {
		return promotions;
	}

	public void setPromotions(Map<String, Date> promotions) {
		this.promotions = promotions;
	}

}