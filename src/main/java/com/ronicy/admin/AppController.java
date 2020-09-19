package com.ronicy.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {
	
	@GetMapping("/error")
	public String error() {
		return "error";
	}

	@GetMapping("/")
	public String app(Model model) {
		String[] urls = new String[10];
		urls[0] = "/algolia/start - to clear all records and add the records from the collection";
		urls[1] = "/algolia/listen - to start listening for collection Advertisment changes";
		
		urls[2] = "/refresh - to add auth expire time of 1 hour/ pass uid as a parameter";
		urls[3] = "/update - send uid/ or applies to all admins / update claims and time extends";
		urls[4] = "/revoke_claims -  send uid/ or applies to all admins / all administration claims are removed";
		
		model.addAttribute("urls", urls);
		
		return "index";
	}
	
}
