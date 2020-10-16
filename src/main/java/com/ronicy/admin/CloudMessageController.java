package com.ronicy.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.ronicy.admin.model.FCM;

@RestController
public class CloudMessageController {

	private static final String SUBSCRIBED_TOPICS_ARRAY_NAME = "subscribedTopics";
	private static final String SUBSCRIBED_TOPICS_DATE_NAME = "updatedDate";
	private static final String SUBSCRIBED_TOPICS_UID = "uid";
	public static final String COLLECTION = "fcm";
	private static final String SUBSCRIBED_TOPICS_PUBLIC = "public";
	private static final String SUBSCRIBED_TOPICS_ADMIN = "admin";
	private static final String SUBSCRIBED_TOPICS_TOPIC1 = "topic1";
	private static final String SUBSCRIBED_TOPICS_TOPIC2 = "topic1";

	Administrators accessAdministrators = new Administrators();

	@GetMapping("/fcm/save")
	public void saveAppToken(@RequestParam(value = "token", required = true) String token,
			@RequestParam(value = "uid", required = false) String uid) {
		try {
			// save in fcm
			List<String> users = new ArrayList<String>();
			users.add(token);

			FirebaseMessaging.getInstance().subscribeToTopic(users, SUBSCRIBED_TOPICS_PUBLIC);

			// save in database
			DocumentReference refStore = FirestoreClient.getFirestore().collection(COLLECTION).document(token);
			DocumentSnapshot doc = refStore.get().get(20, TimeUnit.SECONDS);

			if (doc.exists()) {
				Map<String, Object> map = new HashMap<>();
				map.put(SUBSCRIBED_TOPICS_DATE_NAME, new Date());

				FirestoreClient.getFirestore().collection(COLLECTION).document(token)
				.update(SUBSCRIBED_TOPICS_ARRAY_NAME, FieldValue.arrayUnion(SUBSCRIBED_TOPICS_PUBLIC));
				
				if (uid != null) {
					map.put("uid", uid);
				}

				FirestoreClient.getFirestore().collection(COLLECTION).document(token).set(map, SetOptions.merge());
			} else if (!doc.exists()) {
				if (uid != null) {
					FirestoreClient.getFirestore().collection(COLLECTION).document(token)
							.set(new FCM(token, new String[] { SUBSCRIBED_TOPICS_PUBLIC }), SetOptions.merge());
				} else {
					FirestoreClient.getFirestore().collection(COLLECTION).document(token)
							.set(new FCM(token, new String[] { SUBSCRIBED_TOPICS_PUBLIC }, uid), SetOptions.merge());
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@GetMapping("/fcm/save-admin")
	public void saveAppTokenAdmin(@RequestParam(value = "token", required = true) String token,
			@RequestParam(value = "uid", required = true) String uid) {
		try {
			if (accessAdministrators.validateAdministrators(uid)) {

				// save in fcm
				List<String> users = new ArrayList<String>();
				users.add(token);

				FirebaseMessaging.getInstance().subscribeToTopic(users, SUBSCRIBED_TOPICS_ADMIN);

				// save in database
				DocumentReference refStore = FirestoreClient.getFirestore().collection(COLLECTION).document(token);
				DocumentSnapshot doc = refStore.get().get(10, TimeUnit.SECONDS);

				if (doc.exists()) {
					Map<String, Object> map = new HashMap<>();
					map.put(SUBSCRIBED_TOPICS_DATE_NAME, new Date());
					map.put(SUBSCRIBED_TOPICS_UID, uid);

					FirestoreClient.getFirestore().collection(COLLECTION).document(token)
							.update(SUBSCRIBED_TOPICS_ARRAY_NAME, FieldValue.arrayUnion(SUBSCRIBED_TOPICS_ADMIN));

					FirestoreClient.getFirestore().collection(COLLECTION).document(token).set(map, SetOptions.merge());
				} else if (!doc.exists()) {
					FirestoreClient.getFirestore().collection(COLLECTION).document(token)
							.set(new FCM(token, new String[] { SUBSCRIBED_TOPICS_ADMIN }, uid), SetOptions.merge());
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@GetMapping("/fcm/remove")
	public void sendFCM(@RequestParam(value = "token", required = false) String token) {
		try {
			List<String> users = new ArrayList<String>();
			users.add(token);
			try {
				FirebaseMessaging.getInstance().unsubscribeFromTopic(users, SUBSCRIBED_TOPICS_PUBLIC);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@GetMapping("/fcm/remove-admin")
	public void removeTopicAdmin(@RequestParam(value = "token", required = true) String token) {
		List<String> users = new ArrayList<String>();
		users.add(token);
		try {
			FirebaseMessaging.getInstance().unsubscribeFromTopic(users, SUBSCRIBED_TOPICS_ADMIN);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@GetMapping("/fcm/send-admin")
	public void sendFCMAdmin(@RequestParam(value = "token", required = false) String token) {
		try {
			List<Message> messages = Arrays.asList(
					Message.builder()
							.setNotification(Notification.builder().setTitle("Price drop")
									.setBody("5% off all electronics").build())
							.setTopic(SUBSCRIBED_TOPICS_ADMIN).build(),
					// ...
					Message.builder()
							.setNotification(
									Notification.builder().setTitle("Price drop").setBody("2% off all books").build())
							.setTopic(SUBSCRIBED_TOPICS_ADMIN).build());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@GetMapping("/fcm/ad")
	public void sendFCMForAd() throws FirebaseMessagingException {
		List<String> registrationTokens = Arrays.asList(
				"fgiMLjqUQMSMfIcAm2HrPS:APA91bE8AVkdYEYjvFlI_GJgmb185uZgzEWHNX0VobfWXXqtQCiOVlaYpkGs0SIHqYU4F42bI7-mh3ake7e8TrUaIpn0HB1mTN85ayY-VK8XDaeMwNbL6jUt7knmv6cNg6Xp52n5sLul",
				"dSFNaJA6SM2ypcuTLva-kS:APA91bFeWjlvRzRvhWDNdnZc19yVKPnx5FBXhk0EjZEm9CZoty4PXogAph-fumUHvfs80aI0rAVlxS_Oalm_r-6dP1F9NiBAqMGcgffwoRCNfg7-AXsaPWp3jFeJgImK-sWCYX_on3-t");

		Notification notification = Notification.builder().setTitle("$GOOG up 1.43% on the day")
				.setBody("$GOOG gained 11.80 points to close at 835.67, up 1.43% on the day.").build();

		MulticastMessage message = MulticastMessage.builder().putData("intent", "dialog").putData("header", "2:45")
				.putData("body", "2:45").addAllTokens(registrationTokens).setNotification(notification).build();

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
