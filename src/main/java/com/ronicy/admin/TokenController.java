package com.ronicy.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

@RestController
public class TokenController {

	@Autowired
	ObjectMapper oMapper;

	@Autowired
	Administrators accessAdministrators;

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
		CustomClaims customClaims = accessAdministrators.getClaimsForAdministratorByUID(uid);

		if (uid != null) {
			FirebaseAuth.getInstance().setCustomUserClaimsAsync(uid,
					oMapper.convertValue(customClaims, new TypeReference<Map<String, Object>>() {
					}));
			extendTimeForUser(uid);
		}
	}

	// extend time in the auth user token
	private void extendTimeForUser(String uid) {
		try {
			DocumentReference refStore = com.google.firebase.cloud.FirestoreClient.getFirestore().collection("metadata")
					.document(uid);

			if (refStore.get().isDone() && refStore.get().get().exists()) {
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

				if (refStore.get().isDone() && refStore.get().get().exists()) {
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
		CustomClaims customClaims = new CustomClaims();
		if (uid != null) {
			try {
				FirebaseAuth.getInstance().setCustomUserClaimsAsync(uid,
						oMapper.convertValue(customClaims, new TypeReference<Map<String, Object>>() {
						}));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
