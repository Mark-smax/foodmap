package com.example.foodmap.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.foodmap.member.domain.Member;
import com.example.foodmap.member.domain.MemberRepository;
import com.example.foodmap.member.domain.enums.MemberRole;
import com.example.foodmap.member.domain.enums.MemberStatus;

/**
 * Seeds four test accounts if they do not already exist.
 * Passwords are stored in plaintext to match the current MemberService.checkLogin logic.
 * 
 * Accounts (password all "123456"):
 * - admin@example.com -> ADMIN
 * - user1@example.com -> USER
 * - merchant1@example.com -> MERCHANT
 * - supplier1@example.com -> SUPPLIER
 */
@Configuration
public class TestUserSeeder {

    @Bean
    CommandLineRunner seedMembers(MemberRepository repo) {
        return args -> {
            createIfMissing(repo, "admin@example.com", "管理員", MemberRole.ADMIN);
            createIfMissing(repo, "user1@example.com", "一般會員", MemberRole.USER);
            createIfMissing(repo, "merchant1@example.com", "商家一號", MemberRole.MERCHANT);
            createIfMissing(repo, "supplier1@example.com", "供應商一號", MemberRole.SUPPLIER);
        };
    }

    private void createIfMissing(MemberRepository repo, String email, String name, MemberRole role) {
        if (repo.existsByMemberEmail(email)) return;
        Member m = new Member();
        m.setMemberEmail(email);
        m.setMemberPassword("123456"); // plaintext to match existing login logic
        m.setMemberName(name);
        m.setMemberRole(role);
        m.setMemberStatus(MemberStatus.ACTIVE);
        repo.save(m);
    }
}
