package com.example.foodmap.service;


import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.foodmap.model.GoodPhoto;
import com.example.foodmap.model.GoodPhotoRepository;


@Service
public class GoodPhotoService {
	
	@Autowired
	private GoodPhotoRepository goodPhotoRepo;


	public GoodPhoto saveGoodPhoto(String photoName, byte[] photoFile) {
		GoodPhoto goodPhoto = new GoodPhoto();
		goodPhoto.setPhotoName(photoName);
		goodPhoto.setPhotoFile(photoFile);
		
		return goodPhotoRepo.save(goodPhoto);
	}
	
	public Optional<GoodPhoto> findGoodPhotoById(Integer id){
		return goodPhotoRepo.findById(id);
	}
	
	public List<GoodPhoto> findAllGoodPhoto(){
		return goodPhotoRepo.findAll();
	}
}
