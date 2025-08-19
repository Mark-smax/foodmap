// src/main/java/com/example/foodmap/foodmap/web/RestaurantReviewApiController.java
package com.example.foodmap.foodmap.web;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.foodmap.foodmap.domain.RestaurantService;
import com.example.foodmap.foodmap.dto.ReviewDto;
import com.example.foodmap.member.domain.enums.MemberRole;

@RestController
// 👇 改成 legacy 基底路徑，避免與 RestaurantController 重疊
@RequestMapping("/api/legacy/reviews")
public class RestaurantReviewApiController {

    private final RestaurantService restaurantService;

    public RestaurantReviewApiController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    /* ======================== Session helpers ======================== */
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
        String s = rolesObj == null ? "" : rolesObj.toString();
        return s.contains("ADMIN") || s.contains("管理員");
    }

    /* ======================== 查詢（僅管理員可看隱藏） ======================== */
    // GET /api/legacy/reviews/{restaurantId}?includeHidden=false
    @GetMapping("/{restaurantId}")
    public ResponseEntity<Page<ReviewDto>> list(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "false") boolean includeHidden,
            HttpSession session) {

        boolean canSeeHidden = includeHidden && isAdmin(session);
        Page<ReviewDto> data = restaurantService.getReviewPage(
                restaurantId, page, size, sortBy, order, canSeeHidden);
        return ResponseEntity.ok(data);
    }

    /* ======================== 新增（作者 = Session；不可自行隱藏） ======================== */
    public static class CreateReq {
        @Min(1) @Max(5) public Integer rating;
        public String comment;
        // 前端若送 hidden 也會被忽略
        public Boolean hidden;
    }

    // POST /api/legacy/reviews/{restaurantId}
    @PostMapping("/{restaurantId}")
    public ResponseEntity<?> create(
            @PathVariable Long restaurantId,
            @RequestBody CreateReq req,
            HttpSession session) {

        Long mid = currentMemberId(session);
        if (mid == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登入");

        try {
            int rating = req == null || req.rating == null ? 0 : req.rating;
            String comment = req == null || req.comment == null ? "" : req.comment;

            // 一般使用者不可自行隱藏 → 強制 false
            ReviewDto dto = restaurantService.createReview(
                    restaurantId, mid.intValue(), rating, comment, false);

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /* ======================== 編輯（只能作者本人） ======================== */
    public static class UpdateReq {
        @Min(1) @Max(5) public Integer rating;
        public String comment;
    }

    // PUT /api/legacy/reviews/{restaurantId}/{reviewId}
    @PutMapping("/{restaurantId}/{reviewId}")
    public ResponseEntity<?> update(
            @PathVariable Long restaurantId,
            @PathVariable Long reviewId,
            @RequestBody UpdateReq req,
            HttpSession session) {

        Long mid = currentMemberId(session);
        if (mid == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登入");

        try {
            boolean ok = restaurantService.updateReview(
                    reviewId, mid,
                    req == null ? null : req.rating,
                    req == null ? null : req.comment);

            return ok ? ResponseEntity.ok().build()
                      : ResponseEntity.status(HttpStatus.FORBIDDEN).body("無權限或資料不存在");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /* ======================== 刪除（只能作者本人） ======================== */
    // DELETE /api/legacy/reviews/{restaurantId}/{reviewId}
    @DeleteMapping("/{restaurantId}/{reviewId}")
    public ResponseEntity<?> delete(
            @PathVariable Long restaurantId,
            @PathVariable Long reviewId,
            HttpSession session) {

        Long mid = currentMemberId(session);
        if (mid == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未登入");

        boolean ok = restaurantService.deleteReviewByIdAndMemberId(reviewId, mid);
        return ok ? ResponseEntity.ok().build()
                  : ResponseEntity.status(HttpStatus.FORBIDDEN).body("無權限或資料不存在");
    }

    /* ======================== 隱藏 / 取消隱藏（僅管理員） ======================== */
    // PATCH /api/legacy/reviews/{restaurantId}/{reviewId}/hidden?hidden=true
    @PatchMapping("/{restaurantId}/{reviewId}/hidden")
    public ResponseEntity<?> toggleHidden(
            @PathVariable Long restaurantId,
            @PathVariable Long reviewId,
            @RequestParam(defaultValue = "true") boolean hidden,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("只有管理員可操作");
        }
        restaurantService.setReviewHidden(reviewId, hidden);
        return ResponseEntity.ok().build();
    }
}
