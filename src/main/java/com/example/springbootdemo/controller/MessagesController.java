package com.example.springbootdemo.controller;


import java.util.Optional;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import com.example.springbootdemo.model.Messages;
import com.example.springbootdemo.model.Users;
import com.example.springbootdemo.service.MessagesService;


import jakarta.servlet.http.HttpSession;






@Controller
public class MessagesController {
	
	@Autowired
	private MessagesService msgService;
	
	@GetMapping("/msg/add")
	public String addMsg(Model model) {
		
		Messages lastestMsg = msgService.findLatestMessages();
		
		model.addAttribute("lastestMsg", lastestMsg);
		
		return "messages/addMsgView";
	}
	
	// 送出的 Controller , Post Request
	@PostMapping("/msg/addPost")
	public String addPost(@RequestParam String text, Model model,HttpSession httpSession) {
		
		UUID loginUserId = (UUID) httpSession.getAttribute("loginUserId");
		
		if(loginUserId == null) {
			msgService.addMessages(text);
		}else {
			msgService.addMessagesWithUsers(loginUserId, text);
		}
		
		Messages lastestMsg = msgService.findLatestMessages();
		
		model.addAttribute("okMsg", "新增成功");
		model.addAttribute("lastestMsg", lastestMsg);
		
		return "messages/addMsgView";
	}
	
	@GetMapping("/msg/list")
	public String listMsg(@RequestParam(name="p",defaultValue = "1") Integer pageNumber, Model model) {
		
		Page<Messages> page = msgService.findMessagesByPage(pageNumber);
		
		model.addAttribute("page", page);
		
		return "messages/listMsgView";
	}
	
	@GetMapping("/msg/edit")
	public String editMessages(@RequestParam Integer id, Model model, HttpSession httpSession) {
		
		
		Optional<Messages> msgOptional = msgService.findMessagesById(id);
		
		// 沒有這筆 id 
		if(msgOptional.isEmpty()) {
			return "view401";
		}


		Messages msg = msgOptional.get();
		Users msgOwner = msg.getUsers();
		
		// 這筆沒有 User
		if(msgOwner == null) {
			return "view401";
		}
		
		// 確認訊息擁有者 == 登入者 才給訊息
		// 若不符合 -> view401
		UUID loginUserId = (UUID) httpSession.getAttribute("loginUserId");
		UUID msgUserId = msgOwner.getId();
		
		if(! loginUserId.equals(msgUserId)) {
			return "view401";
		}
		
		httpSession.setAttribute("editingMsgId", msg.getId());
		
		model.addAttribute("msgOptional", msgOptional);
		
		
		return "messages/editMsgView";
	}
	
	@PostMapping("/msg/editPost")
	public String editPostMsg(
			@RequestParam String text, 
			Model model,
			HttpSession httpSession) {
		
		Integer editingMsgId = (Integer) httpSession.getAttribute("editingMsgId");
		
		if(editingMsgId == null) {
			return "view401";
		}
		
		Optional<Messages> msgOptional = msgService.updateTextById(editingMsgId, text);
		
		if(msgOptional.isEmpty()) {
			model.addAttribute("errorMsg", "沒有這筆資料");
		}else {	
			model.addAttribute("updateSuccessMsg", "修改完成");
			model.addAttribute("msgOptional", msgOptional);
			httpSession.removeAttribute("editingMsgId");
		}

		
		return "messages/editMsgView";
	}
	

	@GetMapping("/msg/delete")
	public String deleteMsgById(Integer id, RedirectAttributes ra, HttpSession httpSession  ) {
		
		UUID loginUserId = (UUID) httpSession.getAttribute("loginUserId");
		
		Optional<Messages> msgOptional = msgService.findMessagesById(id);
		
		if(msgOptional.isEmpty()) {
			return "view401";
		}
		
		Messages messages = msgOptional.get();
		
		Users msgUser = messages.getUsers();
		
		if(msgUser == null || ! loginUserId.equals(msgUser.getId())) {
			return "view401";
		}
			
		msgService.deleteMessagesById(id);
		
		
		// 因 redirect 不會記得 Controller 的 Model ,
		// 因此需使用 RedirectAttributes.addFlashAttribute 傳值
		ra.addFlashAttribute("deleteOK", true);
		
		return "redirect:/msg/list";
	}




}
