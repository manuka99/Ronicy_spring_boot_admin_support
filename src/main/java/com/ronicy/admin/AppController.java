package com.ronicy.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {
	
	@GetMapping("/error")
	public String error() {
		return "error";
	}

	@GetMapping("/")
	public String app() {
		return "index";
	}
	
}
