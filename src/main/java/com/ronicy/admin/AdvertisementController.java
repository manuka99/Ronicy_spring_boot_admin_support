package com.ronicy.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.algolia.search.DefaultSearchClient;
import com.algolia.search.SearchClient;
import com.algolia.search.SearchIndex;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.ronicy.admin.model.Advertisement;
import com.ronicy.admin.model.FCM;

@RestController
public class AdvertisementController {

	private static final String ADVERTISEMENT = "Advertisement";

	@GetMapping("/algolia/start")
	public String addToIndexes() {
		SearchClient client = DefaultSearchClient.create("43D39I7H4Q", "06654db6a61b909c59fbdcaec99be259");
		SearchIndex<Advertisement> adIndex = client.initIndex(ADVERTISEMENT, Advertisement.class);
		try {
			List<Advertisement> ads = FirestoreClient.getFirestore().collection(ADVERTISEMENT)
					.whereEqualTo("approved", true).whereEqualTo("availability", true).get().get()
					.toObjects(Advertisement.class);
			adIndex.clearObjects();
			adIndex.saveObjectsAsync(ads, true);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Successfull : All data cleared and new data was added, as these functiona are async please wait for some time befour the next operation.";
	}

	@GetMapping("/algolia/listen")
	public String startListener() {
		SearchClient client = DefaultSearchClient.create("43D39I7H4Q", "06654db6a61b909c59fbdcaec99be259");

		SearchIndex<Advertisement> adIndex = client.initIndex(ADVERTISEMENT, Advertisement.class);

		FirestoreClient.getFirestore().collection(ADVERTISEMENT)
				.addSnapshotListener(new EventListener<QuerySnapshot>() {
					@Override
					public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirestoreException e) {
						if (e != null) {
							System.err.println("Listen failed: " + e);
							return;
						}
						for (DocumentChange dc : snapshots.getDocumentChanges()) {
							Advertisement ad = dc.getDocument().toObject(Advertisement.class);
							System.out.println("Object ID: " + ad.getObjectID() + " Type: " + dc.getType().name());
							if (ad.isApproved() && ad.isAvailability()) {
								switch (dc.getType()) {
								case ADDED:
									adIndex.partialUpdateObjectAsync(ad, true);
									break;
								case MODIFIED:
									adIndex.partialUpdateObjectAsync(ad, true);
									break;
								case REMOVED:
									adIndex.deleteObjectAsync(ad.getObjectID());
									break;
								default:
									break;
								}
							} else
								System.out.println("Not approved and available: Object ID: " + ad.getObjectID()
										+ " Type: " + dc.getType().name());
						}
					}
				});

		return "listening on collection Advertisement";
	}

	@GetMapping("/advertisement/listen")
	public String startAdvertisementApproveListener() {
		FirestoreClient.getFirestore().collection(ADVERTISEMENT)
				.addSnapshotListener(new EventListener<QuerySnapshot>() {
					@Override
					public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirestoreException e) {
						if (e != null) {
							System.err.println("Listen failed: " + e);
							return;
						}
						for (DocumentChange dc : snapshots.getDocumentChanges()) {
							Advertisement ad = dc.getDocument().toObject(Advertisement.class);
							System.out.println("Object ID: " + ad.getObjectID() + " Type: " + dc.getType().name());

							switch (dc.getType()) {

							case ADDED:

								break;
							case MODIFIED:
								onUpdateAd(ad);
								break;
							case REMOVED:

								break;
							default:
								break;

							}

						}
					}
				});

		return "listening on collection Advertisement";
	}

	private void onUpdateAd(Advertisement ad) {

		try {
			UserRecord user = FirebaseAuth.getInstance().getUser(ad.getUserID());

			List<String> registrationTokens = getApplicationTokenFromUid(ad.getUserID());

			if (ad.isReviewed() && ad.isApproved()) {

				Notification notification = Notification.builder().setTitle("Your Advertisement was approved!")
						.setBody(user.getDisplayName() + ", your advertisement " + ad.getTitle()
								+ " was approved by the ronicy team and it is live now!")
						.build();

				MulticastMessage message = MulticastMessage.builder().putData("intent", "ad")
						.putData("adCID", ad.getCategoryID()).putData("adID", ad.getId())
						.addAllTokens(registrationTokens).setNotification(notification).build();

				sendFCMForAd(notification, message, registrationTokens);

			} else if (ad.isReviewed() && !ad.isApproved()) {
				Notification notification = Notification.builder().setTitle("Your Advertisement was rejected!")
						.setBody(user.getDisplayName() + ", your advertisement " + ad.getTitle()
								+ " was rejected by the ronicy team , fix these issues and post again!")
						.build();

				MulticastMessage message = MulticastMessage.builder().putData("intent", "my_ads")
						.addAllTokens(registrationTokens).setNotification(notification).build();

				sendFCMForAd(notification, message, registrationTokens);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void sendFCMForAd(Notification notification, MulticastMessage message, List<String> registrationTokens)
			throws FirebaseMessagingException {

		BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);

		if (response.getFailureCount() > 0) {
			List<SendResponse> responses = response.getResponses();
			List<String> failedTokens = new ArrayList<>();
			for (int i = 0; i < responses.size(); i++) {
				if (!responses.get(i).isSuccessful()) {
					// The order of responses corresponds to the order of the registration tokens.
					failedTokens.add(registrationTokens.get(i));
				}
			}

			System.out.println("List of tokens that caused failures: " + failedTokens);
		}

	}

	private List<String> getApplicationTokenFromUid(String uid) {

		List<String> registrationTokens = new ArrayList<>();

		ApiFuture<QuerySnapshot> query = FirestoreClient.getFirestore().collection(CloudMessageController.COLLECTION)
				.whereEqualTo("uid", uid).get();

		try {
			QuerySnapshot snap = query.get();
			if (snap.size() > 0) {
				List<FCM> fcmList = snap.toObjects(FCM.class);
				for (FCM fcm : fcmList) {
					registrationTokens.add(fcm.getToken());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return registrationTokens;

	}

}
