package com.example.foodmap.foodmap.web;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.foodmap.foodmap.domain.Notification;
import com.example.foodmap.foodmap.domain.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationRestController {

    private final NotificationService notificationService;

    public NotificationRestController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private Long currentMemberId(HttpSession session) {
        Object obj = session.getAttribute("loginMemberId");
        if (obj == null) return null;
        if (obj instanceof Long l) return l;
        if (obj instanceof Integer i) return i.longValue();
        return Long.valueOf(obj.toString());
    }

    /** 列表（前端頁 /notifications 使用） */
    @GetMapping
    public List<NotificationDto> list(HttpSession session) {
        Long memberId = currentMemberId(session);
        if (memberId == null) return List.of();
        return notificationService.listFor(memberId).stream()
                .map(NotificationDto::from)
                .toList();
    }

    /** 未讀數（Navbar 紅點用） */
    @GetMapping("/count")
    public long count(HttpSession session) {
        Long memberId = currentMemberId(session);
        return (memberId == null) ? 0L : notificationService.unreadCount(memberId);
    }

    /** 設為已讀 */
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id, HttpSession session) {
        Long memberId = currentMemberId(session);
        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.markAsRead(id, memberId);
        return ResponseEntity.ok().build();
    }

    /* ==== 傳給前端的 DTO（對齊你的欄位：link / createdAt） ==== */
    public static class NotificationDto {
        public Long id;
        public String title;
        public String message;
        public boolean read;
        public String link;       // 注意：你的實體叫 link，不是 linkUrl
        public java.time.OffsetDateTime createdAt;

        public static NotificationDto from(Notification n) {
            NotificationDto dto = new NotificationDto();
            dto.id = n.getId();
            dto.title = n.getTitle();
            dto.message = n.getMessage();
            dto.read = n.isRead();
            dto.link = n.getLink();
            dto.createdAt = n.getCreatedAt();
            return dto;
        }
    }
}
