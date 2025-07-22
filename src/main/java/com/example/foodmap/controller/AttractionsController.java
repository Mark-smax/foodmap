package com.example.foodmap.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.foodmap.model.Attractions;
import com.example.foodmap.model.AttractionsPhoto;
import com.example.foodmap.model.AttractionsPhotoRepository;
import com.example.foodmap.model.AttractionsRepository;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;






@Controller
public class AttractionsController {
	
	@Autowired
	private AttractionsRepository attrRepo;
	
	@Autowired
	private AttractionsPhotoRepository attrPhotoRepo;
	
	@GetMapping("/attr/add")
	public String addAttr() {
		return "attractions/uploadAttrView";
	}
	
	@PostMapping("/attr/addPost")
	public String addAttrPost(String name, Integer star, MultipartFile[] files,Model model) throws IOException {
	
		// 先有一 , save
		Attractions attr1 = new Attractions();
		attr1.setName(name);
		attr1.setStar(star);
		
		attrRepo.save(attr1);
				
		// 多的物件 , 多 set 1 , save
		for(MultipartFile oneFile : files) {
			AttractionsPhoto photo = new AttractionsPhoto();
			photo.setPhotoFile(oneFile.getBytes());
			photo.setAttractions(attr1); // 多 set 1
			attrPhotoRepo.save(photo);
		}
		
		// 回傳成功訊息
		model.addAttribute("okMsg", "上傳成功!");
		
		return "attractions/uploadAttrView";
	}
	
	@GetMapping("/attr/show")
	public String showAttr(@RequestParam(required = false) Integer attrId  , Model model) {
		
		List<Attractions> attrList = attrRepo.findAll();
		
		model.addAttribute("attrList", attrList);
		
		if(attrId != null) {
			List<Integer> attrPhotoIds = attrPhotoRepo.findAttrPhotosByAttrId(attrId);
			model.addAttribute("attrPhotoIds", attrPhotoIds);
		}
			
		return "attractions/showAttrView";	
	}
	
	@GetMapping("/attr/photo")
	public ResponseEntity<byte[]> downloadAttrPhoto(@RequestParam Integer id) {
		Optional<AttractionsPhoto> op = attrPhotoRepo.findById(id);
		
		if(op.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		AttractionsPhoto attractionsPhoto = op.get();
		byte[] attrphotosFile = attractionsPhoto.getPhotoFile();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);
		
		return new ResponseEntity<byte[]>(attrphotosFile, headers, HttpStatus.OK);	
	}
	
//	
//	@ResponseBody
//	@GetMapping("/attr/photo/ids")
//	public List<Integer> findAttrPhotoIds(@RequestParam Integer attrId) {
//		return attrPhotoRepo.findAttrPhotosByAttrId(attrId);
//	}
//	
	




}
