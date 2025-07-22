package com.example.foodmap.controller;


import org.springframework.web.bind.annotation.RestController;

import com.example.foodmap.model.Youtuber;
import com.example.foodmap.model.YoutuberDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;





@RestController
public class YoutuberController {
	
	@Autowired
	private YoutuberDAO youtuberDAO;
	
	@GetMapping("/ytr/add")
	public String insertYtr() {
		youtuberDAO.insert("金針菇", 1340000);
		
		return "新增OK";
	}
	
	@GetMapping("/ytr/read")
	public Youtuber readYtr() {
		
		Youtuber result = youtuberDAO.findYtrById(2);
		
		if(result != null) {
			return result;
		}
		
		return null;		
	}
		
	
	@GetMapping("/ytr/update")
	public Youtuber updatesub() {
		return youtuberDAO.updateCount(1);
	}

	@GetMapping("/ytr/delete")
	public String testDelete() {
		return youtuberDAO.deleteChannel(1);
	}
}
