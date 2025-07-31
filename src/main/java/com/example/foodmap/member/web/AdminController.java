package com.example.foodmap.member.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.foodmap.member.domain.Member;
import com.example.foodmap.member.domain.MemberService;

@Controller
public class AdminController {

	@Autowired
	private MemberService memberService;

	// 管理者頁面
	@GetMapping("/admin")
	public String admin() {
		System.out.println("進入 admin controller");
		return "member/admin/adminView";
	}

	// 管理者-會員管理
	@GetMapping("/admin/members")
	public String adminMembers(@RequestParam(name = "p", defaultValue = "1") Integer pageNumber, Model model) {

		Page<Member> page = memberService.findAllByPage(pageNumber);
		if (page.getContent().size() == 0) {
			model.addAttribute("erroeMsg", "目前沒有資料");
			System.out.println("===========");
		}
		model.addAttribute("page", page);

		return "member/admin/membersView";
	}

	// 管理者通過，商家供應商的帳號申請
	@GetMapping("/admin/members/approve")
	public String approveMember(@RequestParam("id") Integer memberId) {

		memberService.approveOrRejectMember(memberId, "approve");

		return "redirect:/admin/members";
	}

	// 管理者拒絕，商家供應商的帳號申請
	@GetMapping("/admin/members/reject")
	public String rejectMember(@RequestParam("id") Integer memberId) {

		memberService.approveOrRejectMember(memberId, "reject");

		return "redirect:/admin/members";
	}

}
