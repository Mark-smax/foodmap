// src/main/java/com/example/foodmap/foodmap/web/AdminModerationRestController.java
package com.example.foodmap.foodmap.web;

import java.time.OffsetDateTime;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.foodmap.foodmap.domain.ModerationStatus;
import com.example.foodmap.foodmap.domain.NotificationService;
import com.example.foodmap.foodmap.domain.Restaurant;
import com.example.foodmap.foodmap.domain.RestaurantRepository;

@RestController
@RequestMapping("/api/admin/moderation")
public class AdminModerationRestController {

    private final RestaurantRepository restaurantRepository;
    private final NotificationService notificationService;

    public AdminModerationRestController(RestaurantRepository restaurantRepository,
                                         NotificationService notificationService) {
        this.restaurantRepository = restaurantRepository;
        this.notificationService = notificationService;
    }

    /* ===== 共用：從 session 判斷角色/取得會員 ===== */
    private boolean hasRole(HttpSession session, String role) {
        Object roles = session.getAttribute("loginMemberRoles");
        return roles != null && roles.toString().contains(role);
    }

    private Long currentMemberId(HttpSession session) {
        Object obj = session.getAttribute("loginMemberId");
        if (obj == null) return null;
        if (obj instanceof Long l) return l;
        if (obj instanceof Integer i) return i.longValue();
        return Long.valueOf(obj.toString());
    }

    /* ===== 清單：預設 PENDING ===== */
    @GetMapping("/restaurants")
    public ResponseEntity<?> list(
            @RequestParam(name = "status", defaultValue = "PENDING") ModerationStatus status,
            @PageableDefault(size = 10) Pageable pageable,
            HttpSession session) {
        if (!hasRole(session, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "FORBIDDEN"));
        }
        Page<RestaurantModerationDto> page = restaurantRepository
                .findByStatus(status, pageable)
                .map(RestaurantModerationDto::from);
        return ResponseEntity.ok(page);
    }

    /* ===== 通過 ===== */
    @PostMapping("/restaurants/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, HttpSession session) {
        if (!hasRole(session, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "FORBIDDEN"));
        }

        Restaurant r = restaurantRepository.findById(id).orElse(null);
        if (r == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "NOT_FOUND", "id", id));
        }
        if (r.getStatus() == ModerationStatus.APPROVED) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "ALREADY_APPROVED", "id", id));
        }

        r.setStatus(ModerationStatus.APPROVED);
        r.setReviewedBy(currentMemberId(session));
        r.setReviewedAt(OffsetDateTime.now());
        r.setRejectReason(null);
        restaurantRepository.save(r);

        // 發通知給上傳者（可點進前端餐廳頁）
        if (r.getSubmittedBy() != null) {
            notificationService.notifyMember(
                    r.getSubmittedBy(),
                    "上架申請通過",
                    "你的餐廳「" + r.getName() + "」已通過審核並公開上架。",
                    "/restaurants/" + r.getId()   // 前端路由：/restaurants/:id
            );
        }

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "id", r.getId(),
                "status", r.getStatus().name()
        ));
    }

    /* ===== 退回 ===== */
    @PostMapping("/restaurants/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id,
                                    @RequestBody Map<String, String> body,
                                    HttpSession session) {
        if (!hasRole(session, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "FORBIDDEN"));
        }

        Restaurant r = restaurantRepository.findById(id).orElse(null);
        if (r == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "NOT_FOUND", "id", id));
        }
        if (r.getStatus() == ModerationStatus.REJECTED) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "ALREADY_REJECTED", "id", id));
        }

        String reason = (body.getOrDefault("reason", "")).trim();
        if (reason.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "REASON_REQUIRED"));
        }

        r.setStatus(ModerationStatus.REJECTED);
        r.setReviewedBy(currentMemberId(session));
        r.setReviewedAt(OffsetDateTime.now());
        r.setRejectReason(reason);
        restaurantRepository.save(r);

        // 發通知給上傳者（帶退回原因，導回商家中心修正）
        if (r.getSubmittedBy() != null) {
            notificationService.notifyMember(
                    r.getSubmittedBy(),
                    "上架申請未通過",
                    "你的餐廳「" + r.getName() + "」未通過審核。原因：" + reason,
                    "/merchant/restaurant/mine"
            );
        }

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "id", r.getId(),
                "status", r.getStatus().name()
        ));
    }

    /* ===== 傳給前端的精簡 DTO ===== */
    public static class RestaurantModerationDto {
        public Long id;
        public String name;
        public String county;
        public String type;
        public Long submittedBy;
        public String uploader; // 若你有上傳者暱稱可帶

        public static RestaurantModerationDto from(Restaurant r) {
            RestaurantModerationDto dto = new RestaurantModerationDto();
            dto.id = r.getId();
            dto.name = r.getName();
            dto.county = r.getCounty();
            dto.type = r.getType();
            try { dto.submittedBy = r.getSubmittedBy(); } catch (Exception ignore) {}
            try { // 若有 getUploaderNickname()
                dto.uploader = (String) r.getClass().getMethod("getUploaderNickname").invoke(r);
            } catch (Exception ignore) {}
            return dto;
        }
    }
}
