package com.ronicy.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.v1.FirestoreClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

@RestController
public class TokenController {

	@Autowired
	ObjectMapper oMapper;

	private static final String CUSTOM_CLAIMS_UID_MANUKA = "O9i9UFnGdJfmdI6cIqqLuvYbTpD3";

	@GetMapping("/user/get_token")
	public String validateIDToken(@RequestParam(value = "tokenID", required = false) String tokenID) {
		String uid = null;
		try {
			FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(tokenID);
			uid = decodedToken.getUid();
			if (uid != null) {
				// returnCustomClaimsAddedTokenToClient(uid);
				setCustomClaimToken(uid);
				extendTimeForUser(uid);
			}
		} catch (FirebaseAuthException e) {
			e.printStackTrace();
		}

		return "";
	}

	public String returnCustomClaimsAddedTokenToClient(String uid) {
		if (uid != null) {
			String customToken = getCustomClaimToken(uid);
			return customToken;
		}
		return null;
	}

	private String getCustomClaimToken(String uid) {
		String customToken = null;
		CustomClaims customClaims = getClaimsForUID(uid);
		if (customClaims != null) {
			try {
				customToken = FirebaseAuth.getInstance().createCustomToken(uid,
						oMapper.convertValue(customClaims, new TypeReference<Map<String, Object>>() {
						}));
			} catch (FirebaseAuthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println(customToken);
		}
		return customToken;
	}

	private void setCustomClaimToken(String uid) {
		CustomClaims customClaims = getClaimsForUID(uid);

		if (customClaims != null) {
			FirebaseAuth.getInstance().setCustomUserClaimsAsync(uid,
					oMapper.convertValue(customClaims, new TypeReference<Map<String, Object>>() {
					}));

		}
	}

	private CustomClaims getClaimsForUID(String uid) {
		CustomClaims customClaims = new CustomClaims();
		if (uid.equals(CUSTOM_CLAIMS_UID_MANUKA)) {
			/// customClaims.setAdmin(true);
			customClaims.setAdvertisement_manager(true);
			customClaims.setOrder_manager(true);
			// customClaims.setGuest_admin(true);
			// customClaims.setUser_manager(true);
		}
		return customClaims;
	}

	@GetMapping("/refresh")
	public void revokeRefreshTokens(@RequestParam(value = "uid", required = false) String uid) {
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
	}

	private void extendTimeForUser(String uid) {
		DocumentReference refStore = com.google.firebase.cloud.FirestoreClient.getFirestore().collection("metadata")
				.document(uid);

		Map<String, Object> userData = new HashMap<>();
		userData.put("revokeTime", 10000);
		refStore.set(userData);

	}

	@GetMapping("/logout")
	private void forceLogout(@RequestParam(value = "uid", required = false) String uid) {
		DocumentReference refStore = com.google.firebase.cloud.FirestoreClient.getFirestore().collection("metadata")
				.document(uid);
		Map<String, Object> userData = new HashMap<>();
		userData.put("revokeTime", 0);
		refStore.set(userData);
	}

	@GetMapping("/update")
	private void updateAllClaims(@RequestParam(value = "uid", required = false) String uid) {
		if (uid != null) {
			setCustomClaimToken(uid);
			forceLogout(uid);
		} else {
			setCustomClaimToken(CUSTOM_CLAIMS_UID_MANUKA);
			forceLogout(CUSTOM_CLAIMS_UID_MANUKA);
		}
	}

}
