package com.example.foodmap.controller;


import org.springframework.web.bind.annotation.RestController;

import com.example.foodmap.model.LoginDAO;
import com.example.foodmap.model.Phone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;




@RestController
public class FirstController {
	
	@Autowired
	private LoginDAO loginDAO;
	
	@Autowired
	private Phone phone;
	
	@GetMapping("/test1")
	public String test1() {	
		return "test122";
	}
	
	@GetMapping("/test2")
	public boolean test2222() {
		
		return loginDAO.checkLogin("jerry", "pwd");
		
	}
	
	@GetMapping("/testPhone")
	public Phone test3() {	
		return phone;
	}
	


}
