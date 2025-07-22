package com.example.foodmap.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.foodmap.model.Cart;
import com.example.foodmap.model.CartRepository;
import com.example.foodmap.model.GoodPhoto;
import com.example.foodmap.model.GoodPhotoRepository;
import com.example.foodmap.model.Users;
import com.example.foodmap.model.UsersRepository;


@Service
public class CartService {


	@Autowired
	private CartRepository cartRepo;
	
	@Autowired
	private UsersRepository usersRepo;
	
	@Autowired
	private GoodPhotoRepository goodphotoRepo;
	
	@Transactional
	public Cart addToCart(UUID usersId, Integer goodphotoId) {
		
		Optional<Cart> cartOptional = cartRepo.findUsersAndGoodPhotos(usersId,goodphotoId);
		
		// cart 已存在 vol + 1
		if(cartOptional.isPresent() ) {
			Cart cart = cartOptional.get();
			cart.setVol(cart.getVol() + 1);
			return cart;
		}else {
			  // cart 不存在, new cart save ...
			Users users = usersRepo.findById(usersId).get();
			GoodPhoto goodphoto = goodphotoRepo.findById(goodphotoId).get();
			
			Cart newCart = new Cart();
			newCart.setUsers(users);
			newCart.setGoodphoto(goodphoto);
			newCart.setVol(1);
			
			return cartRepo.save(newCart);	
		}
		
	}
	
	@Transactional
	public Cart minusCartVol(UUID usersId, Integer goodphotoId) {
		Optional<Cart> cartOptional = cartRepo.findUsersAndGoodPhotos(usersId,goodphotoId);
		
		if(cartOptional.isPresent() ) {
			Cart cart = cartOptional.get();
			if( cart.getVol() == 1) {
				cartRepo.delete(cart);
			}else {
				cart.setVol(cart.getVol() - 1);
				return cart;
			}		
		}
		return null;
	}
	
	public List<Cart> showLoginUserCart(UUID usersId){
		return cartRepo.findByUsersId(usersId);
	}
	
	
}
