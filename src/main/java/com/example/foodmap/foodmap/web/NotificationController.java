package com.example.foodmap.foodmap.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.foodmap.foodmap.domain.NotificationService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private Long currentMemberId(HttpSession session) {
        Object obj = session.getAttribute("loginMemberId");
        if (obj == null) return null;
        if (obj instanceof Long l) return l;
        if (obj instanceof Integer i) return i.longValue();
        return Long.valueOf(obj.toString());
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        Long memberId = currentMemberId(session);
        if (memberId == null) {
            return "redirect:/member/login";
        }
        model.addAttribute("items", notificationService.listFor(memberId));
        return "member/notifications"; // Step 5 會提供模板
    }

    @PostMapping("/{id}/read")
    @ResponseBody
    public void markRead(@PathVariable Long id, HttpSession session) {
        Long memberId = currentMemberId(session);
        if (memberId != null) {
            notificationService.markAsRead(id, memberId);
        }
    }

    // 提供未讀數（給 Navbar 輪詢用）
    @GetMapping("/count")
    @ResponseBody
    public long unreadCount(HttpSession session) {
        Long memberId = currentMemberId(session);
        return (memberId == null) ? 0L : notificationService.unreadCount(memberId);
    }
}
