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
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.ronicy.admin.model.Advertisement;

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

		List<String> registrationTokens = Arrays.asList(
				"fgiMLjqUQMSMfIcAm2HrPS:APA91bE8AVkdYEYjvFlI_GJgmb185uZgzEWHNX0VobfWXXqtQCiOVlaYpkGs0SIHqYU4F42bI7-mh3ake7e8TrUaIpn0HB1mTN85ayY-VK8XDaeMwNbL6jUt7knmv6cNg6Xp52n5sLul",
				"dSFNaJA6SM2ypcuTLva-kS:APA91bFeWjlvRzRvhWDNdnZc19yVKPnx5FBXhk0EjZEm9CZoty4PXogAph-fumUHvfs80aI0rAVlxS_Oalm_r-6dP1F9NiBAqMGcgffwoRCNfg7-AXsaPWp3jFeJgImK-sWCYX_on3-t");

		Notification notification = Notification.builder().setTitle("$GOOG up 1.43% on the day")
				.setBody("$GOOG gained 11.80 points to close at 835.67, up 1.43% on the day.").build();

		MulticastMessage message = MulticastMessage.builder().putData("intent", "dialog").putData("header", "2:45")
				.putData("body", "2:45").addAllTokens(registrationTokens).setNotification(notification).build();

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

}
