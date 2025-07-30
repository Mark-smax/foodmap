package com.example.foodmap.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.foodmap.member.domain.Member;
import com.example.foodmap.member.domain.MemberRepository;
import com.example.foodmap.member.domain.enums.MemberRole;
import com.example.foodmap.member.domain.enums.MemberStatus;

import jakarta.annotation.PostConstruct;


@Configuration
public class AppConfig {
	@Autowired
    private MemberRepository memberRepo;
	
	@PostConstruct
	public  void viod() {
		if (!memberRepo.existsByMemberEmail("admin@mail")) {
			Member member = new Member();
			member.setMemberName("admin1");
			member.setMemberEmail("admin@mail");
			member.setMemberPassword("admin");
			member.setMemberRole(MemberRole.ADMIN);
			member.setMemberStatus(MemberStatus.ACTIVE);
			memberRepo.save(member);
		}
	} 
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
		
	}
	


}
