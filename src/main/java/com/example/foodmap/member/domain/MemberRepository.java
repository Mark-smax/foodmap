package com.example.foodmap.member.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {

	Member findByMemberEmail(String memberEmail);

	boolean existsByMemberEmail(String memberEmail);
}
