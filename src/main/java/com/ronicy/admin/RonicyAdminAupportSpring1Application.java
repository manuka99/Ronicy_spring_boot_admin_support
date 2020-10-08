package com.ronicy.admin;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.ronicy.admin.config.AlgoliaConfig;

@SpringBootApplication
public class RonicyAdminAupportSpring1Application {

	@Autowired
	private AlgoliaConfig algoliaConfig;

	public static void main(String[] args) {
		SpringApplication.run(RonicyAdminAupportSpring1Application.class, args);
		try {
			FileInputStream refreshToken = new FileInputStream("src/main/resources/ad-easy-firebase-adminsdk.json");

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(refreshToken))
					.setDatabaseUrl("https://ad-easy.firebaseio.com").build();

			FirebaseApp.initializeApp(options);
		} catch (IOException e) {
			System.out.println("json not found");
			e.printStackTrace();
		}
	}

    @Configuration
    public class WebConfig implements WebMvcConfigurer {      
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/","classpath:/image/")
            .setCachePeriod(0);
        }
    }
    
}
