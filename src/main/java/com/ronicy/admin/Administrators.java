package com.ronicy.admin;

import java.util.HashMap;
import java.util.Map;

public class Administrators {

	private static final String CUSTOM_CLAIMS_UID_MANUKA = "O9i9UFnGdJfmdI6cIqqLuvYbTpD3";
	private static final String CUSTOM_CLAIMS_UID_GUEST = "rTUXfBIXT4hTZh8TiSoroptvAas1";
	
	public String[] getAllAdministrators() {
		return new String[] {CUSTOM_CLAIMS_UID_MANUKA, CUSTOM_CLAIMS_UID_GUEST};
	}
	
	public CustomClaims getClaimsForAdministratorByUID(String uid) {
		CustomClaims customClaims = new CustomClaims();
		if (uid.equals(CUSTOM_CLAIMS_UID_MANUKA)) {
			/// customClaims.setAdmin(true);
			customClaims.setAdvertisement_manager(true);
			customClaims.setOrder_manager(true);
			// customClaims.setGuest_admin(true);
			customClaims.setUser_manager(true);
		} else if (uid.equals(CUSTOM_CLAIMS_UID_GUEST))
			customClaims.setGuest_admin(true);

		return customClaims;
	}
	
	public Map<String, CustomClaims> getClaimsForAllAdministrators(){
		 Map<String, CustomClaims> claims = new HashMap<>();
		 claims.put(CUSTOM_CLAIMS_UID_MANUKA, getClaimsForAdministratorByUID(CUSTOM_CLAIMS_UID_MANUKA));
		 claims.put(CUSTOM_CLAIMS_UID_GUEST, getClaimsForAdministratorByUID(CUSTOM_CLAIMS_UID_GUEST));
		 return claims;
	}

}
