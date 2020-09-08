package com.ronicy.admin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

@RestController
public class TokenController {
	
	@Autowired
	ObjectMapper oMapper;

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
		CustomClaims customClaims = new CustomClaims();

		//manuka yasas
		if (uid.equals("O9i9UFnGdJfmdI6cIqqLuvYbTpD3")) {
			customClaims.setAdvertisement_manager(false);
			customClaims.setOrder_manager(true);
		}
		
		String customToken = null;

		try {
			customToken = FirebaseAuth.getInstance().createCustomToken(uid, oMapper.convertValue(customClaims, new TypeReference<Map<String, Object>>() {
			}));
		} catch (FirebaseAuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(customToken);

		return customToken;
	}

}
