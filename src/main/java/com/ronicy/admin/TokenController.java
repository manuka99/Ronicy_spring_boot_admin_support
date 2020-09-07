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
		customClaims.setAdmin(true);
	
		String customToken = null;
		
		ObjectMapper oMapper = new ObjectMapper();
		 Map<String, Object> map = oMapper.convertValue(customClaims, new TypeReference<Map<String, Object>>() {});

		try {
			customToken = FirebaseAuth.getInstance().createCustomToken(uid, map);
		} catch (FirebaseAuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(customToken);

		return customToken;
	}


}
