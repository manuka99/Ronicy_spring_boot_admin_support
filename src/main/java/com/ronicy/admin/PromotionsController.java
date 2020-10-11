package com.ronicy.admin;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.ronicy.admin.model.ApprovedPromotions;
import com.ronicy.admin.model.Promotion;

@RestController
public class PromotionsController {

	private static final String PROMOTIONS = "Promotions";
	private static final String APPROVED_PROMOTIONS = "ApprovedPromotions";
	private static final String ADVERTISEMENT = "Advertisement";

	@GetMapping("/promotions/update")
	public void updatePromotions() {

		System.out.println("updating...");

		FirestoreClient.getFirestore().collection(PROMOTIONS).whereEqualTo("approved", true)
				.whereEqualTo("reviewed", true).whereEqualTo("activated", false)
				.addSnapshotListener(new EventListener<QuerySnapshot>() {

					@Override
					public void onEvent(QuerySnapshot querySnapshot, FirestoreException error) {
						if (error != null) {
							error.printStackTrace();
						} else {
							List<Promotion> promotions = querySnapshot.toObjects(Promotion.class);
							for (Promotion promotion : promotions) {

								String adID = promotion.getAdvertisementID();

								FirestoreClient.getFirestore().collection(APPROVED_PROMOTIONS).document(adID)
										.addSnapshotListener(new EventListener<DocumentSnapshot>() {

											@Override
											public void onEvent(DocumentSnapshot documentSnapshot,
													FirestoreException error) {
												if (error != null) {
													error.printStackTrace();
												} else if (documentSnapshot.exists()) {

													ApprovedPromotions approvedPromotions = getApplyPromotionsExpiredTime(
															documentSnapshot.toObject(ApprovedPromotions.class),
															promotion);

													FirestoreClient.getFirestore().collection(APPROVED_PROMOTIONS)
															.document(adID).set(approvedPromotions, SetOptions.merge());

												} else {
													ApprovedPromotions approvedPromotions = getApplyPromotionsExpiredTime(
															new ApprovedPromotions(adID, false), promotion);

													FirestoreClient.getFirestore().collection(APPROVED_PROMOTIONS)
															.document(adID).set(approvedPromotions, SetOptions.merge());
												}
											}

										});

							}
						}
					}

				});

	}

	@GetMapping("/promotions/update-promos")
	public void updatePromotionsToApproved() {

		System.out.println("updating...");

		ApiFuture<QuerySnapshot> query = FirestoreClient.getFirestore().collection(PROMOTIONS)
				.whereEqualTo("approved", true).whereEqualTo("reviewed", true).whereEqualTo("activated", false).get();

		try {
			QuerySnapshot querySnapshot = query.get();
			List<Promotion> promotions = querySnapshot.toObjects(Promotion.class);
			for (Promotion promotion : promotions) {

				System.out.println("prrrrr..." + promotion.getAdvertisementID());

				String adID = promotion.getAdvertisementID();

				ApiFuture<DocumentSnapshot> documentQuery = FirestoreClient.getFirestore()
						.collection(APPROVED_PROMOTIONS).document(adID).get();

				DocumentSnapshot document = documentQuery.get();

				if (document.exists()) {

					ApprovedPromotions approvedPromotions = document.toObject(ApprovedPromotions.class);

					// if promo ids != null and doesnt have this promo id

					if (approvedPromotions.getPromoIDs() == null
							|| !approvedPromotions.getPromoIDs().contains(promotion.getPromoID())) {

						WriteResult result = FirestoreClient.getFirestore().collection(APPROVED_PROMOTIONS)
								.document(adID)
								.set(getApplyPromotionsExpiredTime(approvedPromotions, promotion), SetOptions.merge())
								.get();

						// after promo was applied update the promotion
						if (result.getUpdateTime().getSeconds() > 0)
							upDatePromoAsActivated(promotion.getPromoID());

					} else
						upDatePromoAsActivated(promotion.getPromoID());

				} else {
					ApprovedPromotions approvedPromotions = getApplyPromotionsExpiredTime(
							new ApprovedPromotions(adID, false), promotion);

					WriteResult result = FirestoreClient.getFirestore().collection(APPROVED_PROMOTIONS).document(adID)
							.set(approvedPromotions, SetOptions.merge()).get();

					// after promo was applied update the promotion
					if (result.getUpdateTime().getSeconds() > 0)
						upDatePromoAsActivated(promotion.getPromoID());
				}
			}

			System.out.println("done...");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private ApprovedPromotions getApplyPromotionsExpiredTime(ApprovedPromotions approvedPromotions,
			Promotion promotion) {

		// get the promotion type and its dates

		for (String promoType : promotion.getPromos().keySet()) {

			// check the promo type

			// daily bump
			if (promoType.equals(String.valueOf(Promotion.DAILY_BUMP_AD))) {

				// check the time of every of the approved promotion

				if (approvedPromotions.getDailyPromoPromoExpireTime() != null
						&& approvedPromotions.getDailyPromoPromoExpireTime().after(new Date())) {
					// extend time from the last expire time
					Date pDate = approvedPromotions.getDailyPromoPromoExpireTime();
					Calendar c = Calendar.getInstance();
					c.setTime(pDate);
					c.add(Calendar.DATE, promotion.getPromos().get(promoType));
					approvedPromotions.setDailyPromoPromoExpireTime(c.getTime());

					approvedPromotions.getNotes()
							.add("promotion ID = " + promotion.getPromoID() + " Updated on " + new Date()
									+ " Daily bump time extended. old time is " + pDate + " added "
									+ promotion.getPromos().get(promoType) + " days");

				} else {
					// extend time from the current time
					Date pDate = new Date();
					Calendar c = Calendar.getInstance();
					c.setTime(pDate);
					c.add(Calendar.DATE, promotion.getPromos().get(promoType));
					approvedPromotions.setDailyPromoPromoExpireTime(c.getTime());

					approvedPromotions.getNotes()
							.add("promotion ID = " + promotion.getPromoID() + " Updated on " + new Date()
									+ " Daily bump time updated by current time. "
									+ promotion.getPromos().get(promoType) + " days. Last checked at "
									+ approvedPromotions.getDailyPromoPromoExpireTime());

				}
			}

			// urgent
			if (promoType.equals(String.valueOf(Promotion.URGENT_AD))) {

				// check the time of every of the approved promotion

				if (approvedPromotions.getUrgentPromoExpireTime() != null
						&& approvedPromotions.getUrgentPromoExpireTime().after(new Date())) {
					// extend time from the last expire time
					Date pDate = approvedPromotions.getUrgentPromoExpireTime();
					Calendar c = Calendar.getInstance();
					c.setTime(pDate);
					c.add(Calendar.DATE, promotion.getPromos().get(promoType));

					approvedPromotions.setUrgentPromoExpireTime(c.getTime());

					approvedPromotions.getNotes()
							.add("promotion ID = " + promotion.getPromoID() + " Updated on " + new Date()
									+ " urgent time extended. old time is " + pDate + " added "
									+ promotion.getPromos().get(promoType) + " days");

				} else {
					// extend time from the current time
					Date pDate = new Date();
					Calendar c = Calendar.getInstance();
					c.setTime(pDate);
					c.add(Calendar.DATE, promotion.getPromos().get(promoType));

					approvedPromotions.setUrgentPromoExpireTime(c.getTime());

					approvedPromotions.getNotes()
							.add("promotion ID = " + promotion.getPromoID() + " Updated on " + new Date()
									+ " urgent time updated by current time. " + promotion.getPromos().get(promoType)
									+ " days. Last checked at " + approvedPromotions.getUrgentPromoExpireTime());

				}
			}

			// spotLight
			if (promoType.equals(String.valueOf(Promotion.SPOTLIGHT_AD))) {

				// check the time of every of the approved promotion

				if (approvedPromotions.getSpotLightPromoExpireTime() != null
						&& approvedPromotions.getSpotLightPromoExpireTime().after(new Date())) {
					// extend time from the last expire time
					Date pDate = approvedPromotions.getSpotLightPromoExpireTime();
					Calendar c = Calendar.getInstance();
					c.setTime(pDate);
					c.add(Calendar.DATE, promotion.getPromos().get(promoType));

					approvedPromotions.setSpotLightPromoExpireTime(c.getTime());

					approvedPromotions.getNotes()
							.add("promotion ID = " + promotion.getPromoID() + " Updated on " + new Date()
									+ " spotLight time extended. old time is " + pDate + " added "
									+ promotion.getPromos().get(promoType) + " days");

				} else {
					// extend time from the current time
					Date pDate = new Date();
					Calendar c = Calendar.getInstance();
					c.setTime(pDate);
					c.add(Calendar.DATE, promotion.getPromos().get(promoType));

					approvedPromotions.setSpotLightPromoExpireTime(c.getTime());

					approvedPromotions.getNotes()
							.add("promotion ID = " + promotion.getPromoID() + " Updated on " + new Date()
									+ " spotLight time updated by current time. " + promotion.getPromos().get(promoType)
									+ " days. Last checked at " + approvedPromotions.getSpotLightPromoExpireTime());

				}
			}

			// top ad
			if (promoType.equals(String.valueOf(Promotion.TOP_AD))) {

				// check the time of every of the approved promotion

				if (approvedPromotions.getTopAdPromoExpireTime() != null
						&& approvedPromotions.getTopAdPromoExpireTime().after(new Date())) {
					// extend time from the last expire time
					Date pDate = approvedPromotions.getTopAdPromoExpireTime();
					Calendar c = Calendar.getInstance();
					c.setTime(pDate);
					c.add(Calendar.DATE, promotion.getPromos().get(promoType));

					approvedPromotions.setTopAdPromoExpireTime(c.getTime());

					approvedPromotions.getNotes()
							.add("promotion ID = " + promotion.getPromoID() + " Updated on " + new Date()
									+ " top ad time extended. old time is " + pDate + " added "
									+ promotion.getPromos().get(promoType) + " days");

				} else {
					// extend time from the current time
					Date pDate = new Date();
					Calendar c = Calendar.getInstance();
					c.setTime(pDate);
					c.add(Calendar.DATE, promotion.getPromos().get(promoType));

					approvedPromotions.setTopAdPromoExpireTime(c.getTime());

					approvedPromotions.getNotes()
							.add("promotion ID = " + promotion.getPromoID() + " Updated on " + new Date()
									+ " top ad time updated by current time. " + promotion.getPromos().get(promoType)
									+ " days. Last checked at " + approvedPromotions.getTopAdPromoExpireTime());

				}
			}

		}

		return approvedPromotions;
	}

	// if an promotion was applied then save that promotion as activated
	private void upDatePromoAsActivated(String promoID) {
		Map<String, Boolean> map = new HashMap<>();
		map.put("activated", true);

		try {
			FirestoreClient.getFirestore().collection(PROMOTIONS).document(promoID).set(map, SetOptions.merge()).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@GetMapping("/promotions/daily")
	private void updateDailyBumpAds() {
		ApiFuture<QuerySnapshot> query = FirestoreClient.getFirestore().collection(APPROVED_PROMOTIONS)
				.whereEqualTo("stopPromotions", false).whereGreaterThan("dailyPromoPromoExpireTime", new Date())
				.orderBy("dailyPromoPromoExpireTime", Direction.ASCENDING).get();

		try {
			QuerySnapshot snapShot = query.get();

			for (ApprovedPromotions approvedPromotions : snapShot.toObjects(ApprovedPromotions.class)) {
				Map<String, Object> map = new HashMap<>();
				map.put("placedDate", new Date());

				Map<String, Date> promotions = new HashMap<>();
				promotions.put(String.valueOf(Promotion.DAILY_BUMP_AD), approvedPromotions.getDailyPromoPromoExpireTime());

				map.put("promotions", promotions);
				
				try {
					FirestoreClient.getFirestore().collection(ADVERTISEMENT)
							.document(approvedPromotions.getAdvertismentID()).set(map, SetOptions.merge()).get();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@GetMapping("/promotions/urgent")
	private void updateUrgentAds() {
		ApiFuture<QuerySnapshot> query = FirestoreClient.getFirestore().collection(APPROVED_PROMOTIONS)
				.whereEqualTo("stopPromotions", false).whereGreaterThan("urgentPromoExpireTime", new Date())
				.orderBy("urgentPromoExpireTime", Direction.ASCENDING).get();

		try {
			QuerySnapshot snapShot = query.get();

			for (ApprovedPromotions approvedPromotions : snapShot.toObjects(ApprovedPromotions.class)) {
				Map<String, Object> map = new HashMap<>();

				Map<String, Date> promotions = new HashMap<>();
				promotions.put(String.valueOf(Promotion.URGENT_AD), approvedPromotions.getUrgentPromoExpireTime());

				map.put("promotions", promotions);
				
				try {
					FirestoreClient.getFirestore().collection(ADVERTISEMENT)
							.document(approvedPromotions.getAdvertismentID()).set(map, SetOptions.merge()).get();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
