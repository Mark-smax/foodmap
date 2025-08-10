package com.example.foodmap.foodmap.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.foodmap.foodmap.domain.NotificationService;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalModelAttributes {

    private final NotificationService notificationService;

    public GlobalModelAttributes(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute("unreadCount")
    public Long unreadCount(HttpSession session) {
        try {
            Object obj = session.getAttribute("loginMemberId");
            if (obj == null) return 0L;

            Long memberId = null;
            if (obj instanceof Long l) {
                memberId = l;
            } else if (obj instanceof Integer i) {
                memberId = i.longValue();
            } else {
                String s = obj.toString();
                if (s == null || s.isBlank()) return 0L;
                memberId = Long.parseLong(s); // 這行可能丟 NFE，所以包在 try/catch
            }

            if (memberId == null) return 0L;
            return notificationService.unreadCount(memberId);
        } catch (Exception e) {
            // 任意錯誤都不要影響頁面渲染，直接顯示 0
            return 0L;
        }
    }
}
