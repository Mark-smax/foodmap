package com.example.foodmap.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/restaurant")
public class AdminRestaurantController {

    @GetMapping("/create")
    public String showCreateForm(HttpSession session, RedirectAttributes redirectAttrs, Model model) {
        Object roleObj = session.getAttribute("loginMemberRoles");

        if (roleObj == null) {
            redirectAttrs.addFlashAttribute("error", "請先登入！");
            return "redirect:/login"; // 導向登入頁
        }

        if (!"admin".equals(roleObj.toString())) {
            redirectAttrs.addFlashAttribute("error", "您沒有權限進入此頁面！");
            return "redirect:/"; // 回首頁
        }

        return "restaurant-create"; // templates/restaurant-create.html
    }

    // 未來可新增 @PostMapping("/create") 方法來處理表單送出
}
