package com.ronicy.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.UpdateRequest;

@RestController
public class TokenController {

	@Autowired
	ObjectMapper oMapper;

	Administrators accessAdministrators = new Administrators();

	@GetMapping("/user/get_token")
	public String validateIDToken(@RequestParam(value = "tokenID", required = false) String tokenID) {
		String uid = null;
		try {
			FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(tokenID);
			uid = decodedToken.getUid();
			if (uid != null) {
				setCustomClaimsByUID(uid);
			}
		} catch (FirebaseAuthException e) {
			e.printStackTrace();
		}

		return "";
	}

	private void setCustomClaimsByUID(String uid) {
		Map<String, Object> customClaims = accessAdministrators.getClaimsForAdministratorByUID(uid);

		if (uid != null) {
			FirebaseAuth.getInstance().setCustomUserClaimsAsync(uid, customClaims);
			extendTimeForUser(uid);
		}
	}

	// extend time in the auth user token
	private void extendTimeForUser(String uid) {
		try {
			DocumentReference refStore = com.google.firebase.cloud.FirestoreClient.getFirestore().collection("metadata")
					.document(uid);
			
			DocumentSnapshot doc = refStore.get().get(10, TimeUnit.SECONDS);

			if (doc.exists()) {
				Map<String, Object> userData = new HashMap<>();
				userData.put("revokeTime", 10000);
				refStore.set(userData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// this will force a user to get re authenticated before accessing new content
	@GetMapping("/logout")
	private String forceLogout(@RequestParam(value = "uid", required = false) String uid) {
		List<String> uids = new ArrayList<>();

		if (uid != null)
			uids.add(uid);

		else {
			for (String id : accessAdministrators.getAllAdministrators()) {
				uids.add(id);
			}
		}

		for (String userID : uids) {
			try {
				DocumentReference refStore = com.google.firebase.cloud.FirestoreClient.getFirestore()
						.collection("metadata").document(userID);

				System.out.println("sending");

				DocumentSnapshot doc = refStore.get().get(10, TimeUnit.SECONDS);

				if (doc.exists()) {
					System.out.println("success");

					Map<String, Object> userData = new HashMap<>();
					userData.put("revokeTime", 0);
					refStore.set(userData);
					revokeAllClaimsFromUser(userID);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "if uid is present that user will not be able to update firestorecloud else all administrators/ no claims ";
	}

	// this will update the claims in a user and extends time
	@GetMapping("/update")
	private String updateAllClaims(@RequestParam(value = "uid", required = false) String uid) {
		if (uid != null) {
			setCustomClaimsByUID(uid);
		} else {
			for (String id : accessAdministrators.getAllAdministrators()) {
				setCustomClaimsByUID(id);
			}
		}
		return "if uid is present, the claims for that user will be updated else all administrators and time extended";
	}

	@GetMapping("/revoke_claims")
	public String revokeAllClaims(@RequestParam(value = "uid", required = false) String uid) {
		if (uid != null) {
			revokeAllClaimsFromUser(uid);
		} else {
			for (String id : accessAdministrators.getAllAdministrators()) {
				revokeAllClaimsFromUser(id);
			}
		}
		return "if uid is present, the claims for that user will be removed else all administrators and time not changed";
	}

	// revoke all claims from users
	private void revokeAllClaimsFromUser(String uid) {
		if (uid != null) {
			try {	
				UserRecord user = FirebaseAuth.getInstance().getUserAsync(uid).get();
				Map<String, Object> customClaims = user.getCustomClaims();
				customClaims.clear();
				
				UpdateRequest request = new UpdateRequest(uid);
				request.setCustomClaims(customClaims);
				FirebaseAuth.getInstance().updateUserAsync(request);
				
				FirebaseAuth.getInstance().setCustomUserClaimsAsync(uid, customClaims);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@GetMapping("/update_guest_admin_account")
	public String saveGuestAdminDetails() {
		UpdateRequest request = new UpdateRequest("rTUXfBIXT4hTZh8TiSoroptvAas1").setEmail("guest_admin@gmail.com")
				// .setPhoneNumber("+94721111456")
				.setEmailVerified(true).setPassword("12345678").setDisplayName("Guest Admin")
				.setPhotoUrl(
						"https://firebasestorage.googleapis.com/v0/b/ad-easy.appspot.com/o/Ronicy%2Fimages%2Favatar%2Bhuman%2Bmale%2Bman%2Bmen%2Bpeople%2Bperson%2Bprofile%2Buser%2Busers-1320196163635839021_512.png?alt=media&token=3237da5d-b44a-4321-9c11-978074346b53")
				.setDisabled(false);
		try {
			FirebaseAuth.getInstance().updateUser(request);
			return "Successfully updated guest user: new email is guest_admin@gmail.com and password is 12345678";
		} catch (FirebaseAuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Failed to updated guest user";
	}

	// others
	@GetMapping("/refresh")
	public String revokeRefreshTokens(@RequestParam(value = "uid", required = true) String uid) {
		try {
			FirebaseAuth.getInstance().revokeRefreshTokens(uid);
			UserRecord user = FirebaseAuth.getInstance().getUser(uid);
			// Convert to seconds as the auth_time in the token claims is in seconds too.
			long revocationSecond = user.getTokensValidAfterTimestamp() / 1000;
			System.out.println("Tokens revoked at: " + revocationSecond);

			DocumentReference refStore = com.google.firebase.cloud.FirestoreClient.getFirestore().collection("metadata")
					.document(uid);

			Map<String, Object> userData = new HashMap<>();
			userData.put("revokeTime", revocationSecond);
			refStore.set(userData);
		} catch (FirebaseAuthException e) {
			e.printStackTrace();
		}
		return "add a time of expire to auth user";
	}

}
