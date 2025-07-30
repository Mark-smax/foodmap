package com.example.foodmap.member.web;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import com.example.foodmap.member.domain.Member;
import com.example.foodmap.member.domain.MemberService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MemberController {

	@Autowired
	private MemberService memberService;

	// 會玩註冊頁面
	@GetMapping("/member/add")
	public String registerMember() {
		return "member/registerView";
	}

	// 會員註冊
	// 註冊完成後重新導向至登入畫面
	// 註冊失敗(相同信箱)回註冊頁面
	@PostMapping("/member/addPost")
	public String registerMemberPost(Member member, MultipartFile photoFile, Model model) throws IOException {

		if (photoFile != null) {
			member.setMemberPhoto(photoFile.getBytes());
		}
		
		Member newMember = memberService.registerMember(member);

		if (newMember == null) {
			model.addAttribute("errorMsg", "該信箱已有人使用");
			model.addAttribute("registerMember", member);
			return "member/registerView";
		}

		return "redirect:/member/login";
	}

	// 會員登入頁面
	@GetMapping("/member/login")
	public String memberLogin(Member member, Model model) {
		return "member/loginView";
	}

	// 會員登入
	// 登入成功重新導向至特定頁面(看身分別)
	// 登入失敗回登入畫面
	@PostMapping("/member/loginPost")
	public String memberLoginPost(Member member, Model model, HttpSession httpSession) {

		String memberEmail = member.getMemberEmail();
		String memberPassword = member.getMemberPassword();

		// 依帳號密碼找會員
		Member result = memberService.checkLogin(memberEmail, memberPassword);

		// 如果帳密錯誤，回登入頁面
		if (result == null) {
			model.addAttribute("loginFirst", "帳號或密碼錯誤");
			model.addAttribute("loggingEmail", memberEmail);
			return "member/loginView";
		}

		// 檢查角色狀態
		switch (result.getMemberStatus()) {
		// 正常
		case ACTIVE: {
			break;
		}
		// 審核中
		case PENDING: {
			model.addAttribute("loginFirst", "該帳號仍在審核階段，如有需要請聯絡管理員");
			model.addAttribute("loggingEmail", memberEmail);
			return "member/loginView";
		}
		// 刪除
		// 停權
		default:
			model.addAttribute("loginFirst", "該帳號已被刪除或停用，如有需要請聯絡管理員");
			model.addAttribute("loggingEmail", memberEmail);
			return "member/loginView";
		}

		httpSession.setAttribute("loginMemberId", result.getMemberId());
		httpSession.setAttribute("loginMemberName", result.getMemberName());
		httpSession.setAttribute("loginMemberRoles", result.getMemberRole());

		// 檢查角色身分(管理員、一般、商家、供應商)
		switch (result.getMemberRole()) {
		// 管理員
		case ADMIN: {
			// 重新導向後台
			return "redirect:/admin";
//			return "redirect:/";
		}
		// 一般使用者
		case USER: {
			// 重新導向首頁
			return "redirect:/";
		}
		// 商家
		case MERCHANT: {
			// 重新導向商家XXX，暫時先至首頁
			return "redirect:/";
		}
		// 供應商
		case SUPPLIER: {
			// 重新導向供應商XXX，暫時先至首頁
			return "redirect:/";
		}
		default:
			return "redirect:/";
		}

	}

	
	// 會員登出
	@GetMapping("/logout")
	public String logout(HttpSession httpSession) {

		httpSession.removeAttribute("loginMemberId");
		httpSession.removeAttribute("loginMemberName");
		httpSession.removeAttribute("loginMemberRoles");
//		httpSession.invalidate();

		return "redirect:/";
	}
	
	// 商家/供應商會員申請頁面
	@GetMapping("/member/add/apply")
	public String applyMember() {
		return "member/registerApplyView";
	}
	
	// 商家/供應商會員申請
	// 申請完成後重新導向至登入畫面
	// 申請失敗(相同信箱)回申請頁面
	@PostMapping("/member/add/applyPost")
	public String applyMemberPost(Member member, MultipartFile photoFile, Model model) throws IOException {

		if (photoFile != null) {
			member.setMemberPhoto(photoFile.getBytes());
		}
		
		Member newMember = memberService.registerApplyMember(member);

		if (newMember == null) {
			model.addAttribute("errorMsg", "該信箱已有人使用");
			model.addAttribute("registerMember", member);
			return "member/registerApplyView";
		}

		return "redirect:/member/login";
	}
	

}