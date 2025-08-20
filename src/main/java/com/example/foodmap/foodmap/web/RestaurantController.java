// src/main/java/com/example/foodmap/foodmap/web/RestaurantController.java
package com.example.foodmap.foodmap.web;

import java.util.Map;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.foodmap.foodmap.domain.RestaurantPhoto;
import com.example.foodmap.foodmap.domain.RestaurantService;
import com.example.foodmap.foodmap.domain.NotificationService;
import com.example.foodmap.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.foodmap.dto.RestaurantDto;
import com.example.foodmap.foodmap.dto.ReviewCreateRequest;
import com.example.foodmap.foodmap.dto.ReviewDto;
import com.example.foodmap.foodmap.dto.ReviewUpdateRequest;
import com.example.foodmap.member.domain.enums.MemberRole;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService service;
    private final NotificationService notificationService;

    public RestaurantController(RestaurantService service,
                                NotificationService notificationService) {
        this.service = service;
        this.notificationService = notificationService;
    }

    /* ============================= 搜尋列表 ============================= */
    @GetMapping
    public Page<RestaurantDto> search(
            @RequestParam(required = false) String county,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") Double minRating,
            @RequestParam(defaultValue = "") String type,
            @RequestParam(required = false) String memberId
    ) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Long memberIdLong = null;
        try {
            if (memberId != null && !memberId.isBlank()) {
                memberIdLong = Long.parseLong(memberId);
            }
        } catch (NumberFormatException ignored) {}

        if (keyword != null && !keyword.trim().isEmpty()) {
            return service.searchByKeyword(keyword.trim(), pageRequest, memberIdLong);
        }
        if (county != null && !county.trim().isEmpty()
                && type != null && !type.trim().isEmpty()
                && (minRating == null || minRating == 0)) {
            return service.searchByCountyAndType(county.trim(), type.trim(), pageRequest, memberIdLong);
        }
        return service.searchRestaurants(county, minRating, type, pageRequest, memberIdLong);
    }

    /* ============================= 詳細資訊 ============================= */
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getRestaurantDetails(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "memberId", required = false) String memberIdStr) {

        Long memberId = null;
        if (memberIdStr != null && !memberIdStr.isBlank()) {
            try { memberId = Long.parseLong(memberIdStr); } catch (NumberFormatException ignored) {}
        }

        try {
            RestaurantDetailsDTO dto = service.getRestaurantDetails(restaurantId, memberId);
            if (dto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error","NOT_FOUND","id", restaurantId));
            }
            return ResponseEntity.ok(dto);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error","FORBIDDEN","message","No permission to view this restaurant"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error","INTERNAL_ERROR","message", e.getMessage()));
        }
    }

    /* ============================= 照片串流 ============================= */
    @GetMapping("/photos/{photoId}")
    public ResponseEntity<byte[]> photo(@PathVariable Long photoId) {
        RestaurantPhoto p = service.findPhotoById(photoId);
        if (p == null || p.getImage() == null || p.getImage().length == 0) {
            return ResponseEntity.notFound().build();
        }
        String ct = service.guessImageContentType(p.getImage());
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                .header(HttpHeaders.CONTENT_TYPE, ct)
                .body(p.getImage());
    }

    /* ============================= 收藏切換 ============================= */
    @PostMapping("/{id}/favorite/toggle")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable("id") Long restaurantId,
            @RequestParam("memberId") Long memberId) {

        boolean favorite = service.toggleFavorite(restaurantId, memberId);
        return ResponseEntity.ok(Map.of("favorite", favorite));
    }

    /* ============================= 評論：查詢（預設不含隱藏） ============================= */
    /* ============================= 評論：查詢（含隱藏，未授權者看遮蔽字） ============================= */
    @GetMapping("/{id}/reviews")
    public Page<ReviewDto> getReviews(
            @PathVariable("id") Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            HttpSession session
    ) {
        // 決定觀看者身份：作者/管理員可見內容，其他人看遮蔽字串
        Long viewerId = currentMemberId(session);
        boolean admin = isAdmin(session);
        return service.getReviewPageForViewer(restaurantId, page, size, sortBy, order, viewerId, admin);
    }

    /* ============================= 評論：新增（JSON） ============================= */
    @PostMapping(value = "/{id}/reviews", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createReviewJson(
            @PathVariable("id") Long restaurantId,
            @Valid @RequestBody ReviewCreateRequest req
    ) {
        try {
            if (req.getMemberId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "BAD_REQUEST", "message", "缺少 memberId"));
            }
            ReviewDto dto = service.createReview(
                    restaurantId,
                    req.getMemberId().longValue(),
                    req.getRating(),
                    req.getComment()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "BAD_REQUEST", "message", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "CONFLICT", "message", ex.getMessage()));
        }
    }

    /* ============================= 評論：新增（Form） ============================= */
    @PostMapping(value = "/{id}/reviews", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> createReviewForm(
            @PathVariable("id") Long restaurantId,
            @Valid ReviewCreateRequest req
    ) {
        try {
            if (req.getMemberId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "BAD_REQUEST", "message", "缺少 memberId"));
            }
            ReviewDto dto = service.createReview(
                    restaurantId,
                    req.getMemberId().longValue(),
                    req.getRating(),
                    req.getComment()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "BAD_REQUEST", "message", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "CONFLICT", "message", ex.getMessage()));
        }
    }

    /* ============================= 評論：編輯（本人）— JSON 版本 ============================= */
    @PutMapping(value = "/{id}/reviews/{reviewId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateReviewJson(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long reviewId,
            @RequestParam(value = "memberId", required = false) Long memberId,
            @Valid @RequestBody ReviewUpdateRequest req,
            HttpSession session
    ) {
        service.getRestaurantById(restaurantId);

        Long actorId = memberId != null ? memberId : currentMemberId(session);
        if (actorId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "UNAUTHORIZED", "message", "未登入"));
        }

        boolean ok = service.updateReview(reviewId, actorId, req.getRating(), req.getComment());
        return ok ? ResponseEntity.ok(Map.of("ok", true))
                  : ResponseEntity.status(HttpStatus.FORBIDDEN)
                                  .body(Map.of("error", "FORBIDDEN", "message", "只能編輯自己的評論"));
    }

    /* ============================= 評論：編輯（本人）— Form 版本 ============================= */
    @PutMapping(value = "/{id}/reviews/{reviewId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> updateReviewForm(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long reviewId,
            @RequestParam(value = "memberId", required = false) Long memberId,
            @Valid ReviewUpdateRequest req,
            HttpSession session
    ) {
        service.getRestaurantById(restaurantId);

        Long actorId = memberId != null ? memberId : currentMemberId(session);
        if (actorId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "UNAUTHORIZED", "message", "未登入"));
        }

        boolean ok = service.updateReview(reviewId, actorId, req.getRating(), req.getComment());
        return ok ? ResponseEntity.ok(Map.of("ok", true))
                  : ResponseEntity.status(HttpStatus.FORBIDDEN)
                                  .body(Map.of("error", "FORBIDDEN", "message", "只能編輯自己的評論"));
    }

    /* ============================= 評論：刪除（本人） ============================= */
    @DeleteMapping("/{id}/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long reviewId,
            @RequestParam(value = "memberId", required = false) Long memberId,
            HttpSession session
    ) {
        service.getRestaurantById(restaurantId);

        Long actorId = memberId != null ? memberId : currentMemberId(session);
        if (actorId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "UNAUTHORIZED", "message", "未登入"));
        }

        boolean ok = service.deleteReviewByIdAndMemberId(reviewId, actorId);
        return ok ? ResponseEntity.ok(Map.of("ok", true))
                  : ResponseEntity.status(HttpStatus.FORBIDDEN)
                                  .body(Map.of("error", "FORBIDDEN", "message", "只能刪除自己的評論"));
    }

    /* ============================= 評論：隱藏/取消隱藏（只限管理員） ============================= */
    @PatchMapping("/{id}/reviews/{reviewId}/hidden")
    public ResponseEntity<?> setHidden(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long reviewId,
            @RequestParam("hidden") boolean hidden,
            HttpSession session
    ) {
        service.getRestaurantById(restaurantId);

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "FORBIDDEN", "message", "只有管理員可以隱藏/取消隱藏評論"));
        }
        service.setReviewHidden(reviewId, hidden);
        // 如需在隱藏後通知作者，可在 service 增加查作者的方法，然後呼叫 notificationService.notifyMemberReviewHidden(...)
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /* ============================= 評論：檢舉（會通知管理員） ============================= */
    public static class ReportReq {
        public Long memberId;   // 檢舉者（可省略，則由 session 取）
        public String reason;   // 檢舉理由（可為 null/空）
    }

    @PostMapping("/{id}/reviews/{reviewId}/report")
    public ResponseEntity<?> reportReview(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long reviewId,
            @RequestBody(required = false) ReportReq body,
            HttpSession session
    ) {
        Long reporterId = (body != null && body.memberId != null)
                ? body.memberId
                : currentMemberId(session);
        String reason = (body == null) ? null : body.reason;

        // ✅ 發通知給所有管理員：連結直接指向餐廳詳情頁，並帶 reviewId
        notificationService.notifyAdminsReviewReported(restaurantId, reviewId, reporterId, reason);

        // 被檢舉者先不通知；等管理員真的隱藏時再通知
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /* ============================= Helpers ============================= */
    private Long currentMemberId(HttpSession session) {
        Object obj = session == null ? null : session.getAttribute("loginMemberId");
        if (obj == null) return null;
        if (obj instanceof Long l) return l;
        if (obj instanceof Integer i) return i.longValue();
        try { return Long.valueOf(obj.toString()); } catch (Exception e) { return null; }
    }

    private boolean isAdmin(HttpSession session) {
        Object rolesObj = session == null ? null : session.getAttribute("loginMemberRoles");
        if (rolesObj instanceof MemberRole role) {
            return role == MemberRole.ADMIN;
        }
        String roles = rolesObj == null ? "" : rolesObj.toString();
        return roles.contains("ADMIN") || roles.contains("管理員");
    }
    @GetMapping("/{id}/reviews/{reviewId}")
    public ResponseEntity<?> getOneReview(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long reviewId,
            HttpSession session
    ) {
        // 確保餐廳存在（避免亂 ID）
        service.getRestaurantById(restaurantId);

        ReviewDto dto = service.getReviewDtoById(reviewId);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "NOT_FOUND", "message", "找不到這則評論"));
        }

        // 你的 DTO 欄位是 isHidden ⇒ 用 getIsHidden()
        boolean hidden = Boolean.TRUE.equals(dto.isHidden());
        if (hidden && !isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "FORBIDDEN", "message", "沒有權限檢視此評論"));
        }
        return ResponseEntity.ok(dto);
    }
}
