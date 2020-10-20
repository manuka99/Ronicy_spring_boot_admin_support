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
		return "index";
	}
	
	@GetMapping("/privacy")
	public String privacy(Model model) {
		return "privacy";
	}
	
}
