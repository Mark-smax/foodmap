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
        Object obj = session.getAttribute("loginMemberId");
        if (obj == null) return 0L;
        Long memberId = (obj instanceof Long l) ? l : Long.valueOf(obj.toString());
        return notificationService.unreadCount(memberId);
    }
}
