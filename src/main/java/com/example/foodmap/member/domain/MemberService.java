package com.example.foodmap.member.domain;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.foodmap.member.domain.enums.MemberRole;
import com.example.foodmap.member.domain.enums.MemberStatus;

@Service
public class MemberService {

	@Autowired
	private MemberRepository memberRepo;

	// 一般用戶註冊
	public Member registerMember(Member member) {

		if (memberRepo.existsByMemberEmail(member.getMemberEmail())) {
			return null;
		}

		member.setMemberRole(MemberRole.USER);
		member.setMemberStatus(MemberStatus.ACTIVE);

		return memberRepo.save(member);
	}

	// 登入
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

	// 商家/供應商會員申請
	public Member registerApplyMember(Member member) {
		
		if (memberRepo.existsByMemberEmail(member.getMemberEmail())) {
			return null;
		}

		switch (member.getMemberRole()) {
		// 商家
		case MERCHANT: {
			member.setMemberRole(MemberRole.MERCHANT);
			break;
		}
		// 供應商
		case SUPPLIER: {
			member.setMemberRole(MemberRole.SUPPLIER);
			break;
		}
		default:
			return null;
		}
		
		member.setMemberStatus(MemberStatus.PENDING);

		return memberRepo.save(member);
	}
	
	// 尋找所有會員
	public Page<Member> findAllByPage(Integer pageNumber){
		Pageable pgb = PageRequest.of(pageNumber - 1, 3, Sort.Direction.ASC, "memberId");
		
		Page<Member> page = memberRepo.findAll(pgb);
		
		return page;
	}
	
	// 通過 或 拒絕 商家/供應商會員申請
	@Transactional
	public void approveOrRejectMember(Integer memberId, String action) {
		
		Optional<Member> op = memberRepo.findById(memberId);
		
		if (op.isEmpty()) {
			return;
		}
		
		Member member = op.get();
		
		switch (action) {
		case "approve": {
			System.out.println("========================");
			member.setMemberStatus(MemberStatus.ACTIVE);
			System.out.println("========================");
			return;
		}
		case "reject": {
			member.setMemberStatus(MemberStatus.REJECTED);
			return;
		}
		default:
			return;
		}
		
	}
	
}
