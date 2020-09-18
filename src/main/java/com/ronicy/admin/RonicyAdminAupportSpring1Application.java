package com.ronicy.admin;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.algolia.search.DefaultSearchClient;
import com.algolia.search.SearchClient;
import com.algolia.search.SearchIndex;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.annotations.Nullable;
import com.ronicy.admin.config.AlgoliaConfig;
import com.ronicy.admin.model.Advertisement;

@SpringBootApplication
public class RonicyAdminAupportSpring1Application {
	
	private static final String ADVERTISEMENT = "Advertisement";
	
	@Autowired
	private AlgoliaConfig algoliaConfig;

	public static void main(String[] args) {
		SpringApplication.run(RonicyAdminAupportSpring1Application.class, args);
		try {
			FileInputStream refreshToken = new FileInputStream("src/main/resources/ad-easy-firebase-adminsdk.json");

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(refreshToken))
					.setDatabaseUrl("https://ad-easy.firebaseio.com").build();

			FirebaseApp.initializeApp(options);
		} catch (IOException e) {
			System.out.println("json not found");
			e.printStackTrace();
		}

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
							ad.setObjectID(ad.getId());
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
						}
					}
				});

	}

}
