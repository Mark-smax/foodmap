package com.example.springbootdemo.controller;


import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


import com.example.springbootdemo.model.Users;
import com.example.springbootdemo.model.UsersDetail;
import com.example.springbootdemo.service.UsersService;


import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;








@Controller
public class UsersController {
	
	@Autowired
	private UsersService usersService;


	@GetMapping("/users/add")
	public String register() {
		return "users/registerView";
	}
	
	@PostMapping("/users/addPost")
	public String addUserPost(String username, String pwd,Model model) {
	
		Users dbUser = usersService.findUsersByName(username);
		
		if(dbUser != null) {
			model.addAttribute("errorMsg", "此帳號已經被使用");
		}else {
			usersService.registerUser(username, pwd);
			model.addAttribute("registerOK", "註冊成功");
		}	
		
		return "users/registerView";
	}
	
	
	@GetMapping("/users/login")
	public String usersLogin() {
		return "users/loginView";
	}
	
	@PostMapping("/users/loginPost")
	public String loginPost(String username, String pwd, HttpSession httpSession,Model model) {
			
		// check login
		Users result = usersService.checkLogin(username, pwd);
		// 成功，把 user id 和 username 放進 HttpSession, 回傳成功訊息
		if(result != null) {
			httpSession.setAttribute("loginUserId", result.getId());
			httpSession.setAttribute("loginUsername", result.getUsername());
			model.addAttribute("okMsg", "登入成功");
		}else {
			// 失敗，回傳錯誤訊息
			model.addAttribute("errMsg", "帳號密碼錯誤");
		}
		
		return "users/loginView";
	}
	
	@GetMapping("/logout")
	public String logout(HttpSession httpSession) {
		
		httpSession.removeAttribute("loginUserId");
		httpSession.removeAttribute("loginUsername");
		
		
		return "redirect:/";
	}
	
	
	@GetMapping("/users/detail")
	public String usersDetial(HttpSession httpSession, Model model) {
		
		String loginUsername = (String) httpSession.getAttribute("loginUsername");
		
		if(loginUsername == null) {
			return "users/loginView";
		}	
		
		Users loginUsers = usersService.findUsersByName(loginUsername);
		
		UsersDetail usersDetail = loginUsers.getUsersDetail();
		
		if(usersDetail != null) {
			model.addAttribute("usersDetail", usersDetail);
		}
		
		return "users/detailView";
	}
	
	@PostMapping("users/detailPost")
	public String detailPost(
			String address, 
			String phone, 
			String cardNumber,
			HttpSession httpSession,
			Model model) {
		
		UUID loginUserId = (UUID) httpSession.getAttribute("loginUserId");
		
		UsersDetail usersDetail = usersService.saveOrUpdateUsersDetail(loginUserId, address, phone, cardNumber);
		
		model.addAttribute("okMsg","修改OK");
		
		model.addAttribute("usersDetail", usersDetail);
		
		return "users/detailView";
	}
	
}
