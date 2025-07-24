package com.example.foodmap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

	@GetMapping("/")
	public String home() {
		return "index.html";
	}
	
	
	@GetMapping("/foodmap")
	public String foodmap() {
		return "foodmap.html";
	}
	
//	  @GetMapping("/restaurant-detail")
//	    public String showRestaurantDetailPage(@RequestParam("id") Long id) {
//	        // 你可以在這裡把 id 加到 model 中給 Thymeleaf 使用
//	        return "restaurant-detail"; // 不用加 .html，Spring Boot 會找 templates/ 下的檔案
//	    }
//	
}
