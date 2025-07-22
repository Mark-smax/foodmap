package com.example.foodmap.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.foodmap.model.Phone;


@Configuration
public class AppConfig {
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
		
	}
	
	@Bean
	Phone phone() {
		Phone phone = new Phone();
		phone.setId(101);
		phone.setName("iPhone 17");
		phone.setBrand("Apple");
		return phone;
	}


}
