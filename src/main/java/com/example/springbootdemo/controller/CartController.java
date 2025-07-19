package com.example.springbootdemo.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import com.example.springbootdemo.model.Cart;
import com.example.springbootdemo.model.GoodPhoto;
import com.example.springbootdemo.service.CartService;
import com.example.springbootdemo.service.GoodPhotoService;


import jakarta.servlet.http.HttpSession;


@Controller
public class CartController {
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private GoodPhotoService gpService;
	
	@GetMapping("/cart/add")
	public String addCart(
			@RequestParam Integer goodphotoId, 
			HttpSession httpSession,
			Model model) {
		
		UUID loginUserId =  (UUID) httpSession.getAttribute("loginUserId");
		
		if(loginUserId == null) {
			model.addAttribute("loginFirst", "請先登入，再購買");
			return "users/loginView";
		}
		
		cartService.addToCart(loginUserId, goodphotoId);
		model.addAttribute("addOk", "新增購物車成功!");
		
		List<GoodPhoto> goodPhotoList = gpService.findAllGoodPhoto();
		model.addAttribute("goodPhotoList", goodPhotoList);
		
		return "goodphoto/showPhotosView";
	}
	
	@GetMapping("/cart/add2")
	public String getMethodName(@RequestParam Integer goodphotoId, 
			HttpSession httpSession,
			Model model) {
		
		UUID loginUserId =  (UUID) httpSession.getAttribute("loginUserId");
		
		cartService.addToCart(loginUserId, goodphotoId);	
		
		List<Cart> cartList  = cartService.showLoginUserCart(loginUserId);
		
		model.addAttribute("cartList", cartList);
		model.addAttribute("addOk", "新增購物車成功!");
		
		return "cartView";
	}
	
	@GetMapping("/cart/minus")
	public String minusVol(@RequestParam Integer goodphotoId, 
			HttpSession httpSession,
			Model model) {
		
		UUID loginUserId =  (UUID) httpSession.getAttribute("loginUserId");
		
		cartService.minusCartVol(loginUserId, goodphotoId);
		
		List<Cart> cartList  = cartService.showLoginUserCart(loginUserId);
		
		model.addAttribute("minusOk", "減數量OK");
		
		if(cartList.size() == 0) {
			model.addAttribute("emptyMsg", "購物車還沒有東西唷，趕緊去挑吧!!");
		}else {
			model.addAttribute("cartList", cartList);
		}
		
		return "cartView";
	}
	
	
	
	@GetMapping("/carts")
	public String checkCarts(HttpSession httpSession, Model model) {
		
		UUID loginUserId =  (UUID) httpSession.getAttribute("loginUserId");
		
		// 沒登入 --> 登入畫面
		if(loginUserId == null) {
			return "users/loginView";
		}
			
		// 有登入 --> 顯示購物車內容
		List<Cart> cartList  = cartService.showLoginUserCart(loginUserId);
		
		if(cartList.size() == 0) {
			model.addAttribute("emptyMsg", "購物車還沒有東西唷，趕緊去挑吧!!");
		}else {
			model.addAttribute("cartList", cartList);
		}
		
		return "cartView";
	}
	


}
