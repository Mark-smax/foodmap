package com.example.foodmap.member.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

	@Autowired
	private MemberRepository memberRepo;

	public Member registerMember(Member member) {

		if (memberRepo.existsByMemberEmail(member.getMemberEmail())) {
			return null;
		}

		member.setMemberRole("一般");
		member.setMemberStatus("正常");

		return memberRepo.save(member);
	}

	public Member checkLogin(String memberEmail, String memberPassword) {
		if (!memberRepo.existsByMemberEmail(memberEmail)) {
			return null;
		}

		Member dbMember = memberRepo.findByMemberEmail(memberEmail);

		if (dbMember.getMemberPassword().equals(memberPassword)) {
			return dbMember;
		}

		return null;
	}
}
