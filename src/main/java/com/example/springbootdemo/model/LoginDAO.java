package com.example.springbootdemo.model;


import org.springframework.stereotype.Component;


@Component
public class LoginDAO {
	
	public boolean checkLogin(String username, String password) {
		
		if("jerry".equals(username) && "pwd".equals(password)) {
			return true;
		}
		
		return false;
	}


}
