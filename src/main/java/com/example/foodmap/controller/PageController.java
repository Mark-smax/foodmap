package com.example.foodmap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

	@GetMapping("/")
	public String home() {
		return "index.html";
	}
	
	@GetMapping("/about")
	public String about() {
		return "aboutPage.html";
	}
	
}
