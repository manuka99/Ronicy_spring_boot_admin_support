package com.ronicy.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

@RestController
public class TokenController {

	@GetMapping("/user/get_token")
	public String validateIDToken(@RequestParam(value = "tokenID", required = false) String tokenID) {
		String uid = null;
		try {
			FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(tokenID);
			uid = decodedToken.getUid();
			if (uid != null) {
				String customToken = getCustomClaimToken(uid);
				return customToken;
			}
		} catch (FirebaseAuthException e) {
			e.printStackTrace();
		}

		return null;
	}

	private String getCustomClaimToken(String uid) {
		Map<String, Object> additionalClaims = new HashMap<String, Object>();
		additionalClaims.put("premiumAccount", true);
		String customToken = null;

		try {
			customToken = FirebaseAuth.getInstance().createCustomToken(uid, additionalClaims);
		} catch (FirebaseAuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(customToken);

		return customToken;
	}

}
