package com.ronicy.admin;

import java.util.HashMap;
import java.util.Map;

import com.ronicy.admin.model.CustomClaims;

public class Administrators {

	private static final String CUSTOM_CLAIMS_UID_MANUKA = "O9i9UFnGdJfmdI6cIqqLuvYbTpD3";
	private static final String CUSTOM_CLAIMS_UID_GUEST = "rTUXfBIXT4hTZh8TiSoroptvAas1";

	public String[] getAllAdministrators() {
		return new String[] { CUSTOM_CLAIMS_UID_MANUKA, CUSTOM_CLAIMS_UID_GUEST };
	}

	public Map<String, Boolean> getClaimsForAdministratorByUID(String uid) {
		Map<String, Boolean> claims = new HashMap<>();
		if (uid.equals(CUSTOM_CLAIMS_UID_MANUKA)) {

			// claims.put(CustomClaims.ADMIN, true);
			claims.put(CustomClaims.ADVERTISEMENT_MANAGER, true);
			claims.put(CustomClaims.ORDER_MANAGER, true);
			claims.put(CustomClaims.USER_MANAGER, true);
			// claims.put(CustomClaims.GUEST_ADMIN, true);

		} else if (uid.equals(CUSTOM_CLAIMS_UID_GUEST))
			claims.put(CustomClaims.GUEST_ADMIN, true);

		return claims;
	}

	public Map<String, Map<String, Boolean>> getClaimsForAllAdministrators() {
		Map<String, Map<String, Boolean>> claims = new HashMap<>();
		claims.put(CUSTOM_CLAIMS_UID_MANUKA, getClaimsForAdministratorByUID(CUSTOM_CLAIMS_UID_MANUKA));
		claims.put(CUSTOM_CLAIMS_UID_GUEST, getClaimsForAdministratorByUID(CUSTOM_CLAIMS_UID_GUEST));
		return claims;
	}

	public boolean validateAdministrators(String uid) {
		return (uid.equals(CUSTOM_CLAIMS_UID_MANUKA) || uid.equals(CUSTOM_CLAIMS_UID_GUEST));
	}

}
