package com.ronicy.admin;

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

}
