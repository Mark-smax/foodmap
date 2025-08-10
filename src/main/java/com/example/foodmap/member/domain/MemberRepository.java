package com.example.foodmap.member.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.foodmap.member.domain.enums.MemberRole;

public interface MemberRepository extends JpaRepository<Member, Integer> {

	Member findByMemberEmail(String memberEmail);

	boolean existsByMemberEmail(String memberEmail);
	
    List<Member> findByMemberRole(MemberRole memberRole);

}
