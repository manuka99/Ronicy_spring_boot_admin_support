package com.ronicy.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.ronicy.admin.model.FCM;

@RestController
public class CloudMessageController {

	private static final String SUBSCRIBED_TOPICS_ARRAY_NAME = "subscribedTopics";
	private static final String SUBSCRIBED_TOPICS_PUBLIC = "public";
	private static final String SUBSCRIBED_TOPICS_ADMIN = "admin";
	private static final String SUBSCRIBED_TOPICS_TOPIC1 = "topic1";
	private static final String SUBSCRIBED_TOPICS_TOPIC2 = "topic1";

	Administrators accessAdministrators = new Administrators();

	@GetMapping("/fcm/save")
	public void saveAppToken(@RequestParam(value = "token", required = true) String token) {
		try {
			ApiFuture<DocumentSnapshot> task = FirestoreClient.getFirestore().collection("fcm").document(token).get();
			if (task.isDone() && task.get().exists()) {
				FirestoreClient.getFirestore().collection("fcm").document(token).update(SUBSCRIBED_TOPICS_ARRAY_NAME,
						FieldValue.arrayUnion(SUBSCRIBED_TOPICS_PUBLIC));
			} else if (task.isDone() && !task.get().exists()) {
				FirestoreClient.getFirestore().collection("fcm").document(token)
						.set(new FCM(token, new String[] { SUBSCRIBED_TOPICS_PUBLIC }), SetOptions.merge());
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
				ApiFuture<DocumentSnapshot> task = FirestoreClient.getFirestore().collection("fcm").document(token)
						.get();
				if (task.isDone() && task.get().exists()) {
					FirestoreClient.getFirestore().collection("fcm").document(token)
							.update(SUBSCRIBED_TOPICS_ARRAY_NAME, FieldValue.arrayUnion(SUBSCRIBED_TOPICS_ADMIN));
				} else if (task.isDone() && !task.get().exists()) {
					FirestoreClient.getFirestore().collection("fcm").document(token)
							.set(new FCM(token, new String[] { SUBSCRIBED_TOPICS_ADMIN }, uid), SetOptions.merge());
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@GetMapping("/fcm/send")
	public void sendFCM(@RequestParam(value = "token", required = false) String token) {
		try {
			ApiFuture<DocumentSnapshot> task = FirestoreClient.getFirestore().collection("fcm").document(token).get();
			if (task.isDone() && task.get().exists()) {
				FirestoreClient.getFirestore().collection("fcm").document(token).update(SUBSCRIBED_TOPICS_ARRAY_NAME,
						FieldValue.arrayUnion(SUBSCRIBED_TOPICS_ADMIN));
			} else if (task.isDone() && !task.get().exists()) {
				FirestoreClient.getFirestore().collection("fcm").document(token)
						.set(new FCM(token, new String[] { SUBSCRIBED_TOPICS_ADMIN }), SetOptions.merge());
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

}
