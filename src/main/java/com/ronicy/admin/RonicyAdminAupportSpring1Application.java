package com.ronicy.admin;

import java.io.FileInputStream;
import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@SpringBootApplication
public class RonicyAdminAupportSpring1Application {

	public static void main(String[] args) {
		SpringApplication.run(RonicyAdminAupportSpring1Application.class, args);
		try {
			FileInputStream refreshToken = new FileInputStream("src/main/resources/ad-easy-firebase-adminsdk.json");

			FirebaseOptions options = FirebaseOptions.builder()
			    .setCredentials(GoogleCredentials.fromStream(refreshToken))
			    .setDatabaseUrl("https://ad-easy.firebaseio.com")
			    .build();

			FirebaseApp.initializeApp(options);
		} catch (IOException e) {
			System.out.println("json not found");
			e.printStackTrace();
		}		
	}

}
