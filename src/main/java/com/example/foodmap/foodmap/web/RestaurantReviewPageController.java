package com.example.foodmap.foodmap.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.foodmap.foodmap.domain.RestaurantReviewService;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

/**
 * Legacy MVC controller for old /review/** routes.
 * 已停用，避免與新的 /api/restaurants/{id}/reviews… 衝突。
 * 若仍需舊頁面，改回 @RequestMapping("/review") 並啟用下方註解的實作。
 */
@RestController
@RequestMapping("/legacy/review")
@Deprecated
public class RestaurantReviewPageController {

    private final RestaurantReviewService reviewService;

    public RestaurantReviewPageController(RestaurantReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // 統一回應：告知端點已下線，請改用新的 REST API
    private ResponseEntity<Map<String, Object>> gone() {
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of(
                "error", "LEGACY_ENDPOINT_DISABLED",
                "message", "This legacy endpoint is disabled. Use /api/restaurants/{id}/reviews…",
                "docs", "/api/restaurants/{id}/reviews (GET/POST/PUT/DELETE/PATCH)"
        ));
    }

    @PostMapping("/hide")
    public ResponseEntity<Map<String, Object>> hideReview(
            @RequestParam Long reviewId,
            @RequestParam Long restaurantId,
            HttpSession session) {
        return gone();
    }

    @PostMapping("/unhide")
    public ResponseEntity<Map<String, Object>> unhideReview(
            @RequestParam Long reviewId,
            @RequestParam Long restaurantId,
            HttpSession session) {
        return gone();
    }

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteReview(
            @RequestParam Long reviewId,
            @RequestParam Long restaurantId,
            HttpSession session) {
        return gone();
    }

    @PostMapping("/edit")
    public ResponseEntity<Map<String, Object>> editReview(
            @RequestParam Long reviewId,
            @RequestParam int rating,
            @RequestParam String comment,
            @RequestParam Long restaurantId,
            HttpSession session) {
        return gone();
    }

    /* ------------------------------------------------------------
     * 若你「真的需要」繼續支援舊頁面：
     * 1) 把類別上的 @RequestMapping 改回 "/review"
     * 2) 把上面四個方法改為回傳 redirect:String 或 ResponseEntity
     * 3) 下面示範用法可參考（保留於註解，不參與編譯）
     * ------------------------------------------------------------
     *
     * private Long loginId(HttpSession s) {
     *     Object o = s.getAttribute("loginMemberId");
     *     if (o == null) return null;
     *     if (o instanceof Integer i) return i.longValue();
     *     if (o instanceof Long l) return l;
     *     return Long.valueOf(o.toString());
     * }
     *
     * private boolean isAdmin(HttpSession s) {
     *     Object roles = s.getAttribute("loginMemberRoles");
     *     String r = (roles == null ? "" : roles.toString());
     *     return r.contains("ADMIN") || r.contains("管理員");
     * }
     *
     * @PostMapping("/hide")
     * public String legacyHide(@RequestParam Long reviewId, @RequestParam Long restaurantId, HttpSession s) {
     *     if (!isAdmin(s)) return "redirect:/restaurant-detail?id=" + restaurantId + "&err=forbidden";
     *     reviewService.setReviewHidden(reviewId, true);
     *     return "redirect:/restaurant-detail?id=" + restaurantId;
     * }
     *
     * @PostMapping("/unhide")
     * public String legacyUnhide(@RequestParam Long reviewId, @RequestParam Long restaurantId, HttpSession s) {
     *     if (!isAdmin(s)) return "redirect:/restaurant-detail?id=" + restaurantId + "&err=forbidden";
     *     reviewService.setReviewHidden(reviewId, false);
     *     return "redirect:/restaurant-detail?id=" + restaurantId;
     * }
     *
     * @PostMapping("/delete")
     * public String legacyDelete(@RequestParam Long reviewId, @RequestParam Long restaurantId, HttpSession s) {
     *     Long me = loginId(s);
     *     if (me == null) return "redirect:/restaurant-detail?id=" + restaurantId + "&err=login";
     *     if (!reviewService.deleteReviewByIdAndMemberId(reviewId, me))
     *         return "redirect:/restaurant-detail?id=" + restaurantId + "&err=forbidden";
     *     return "redirect:/restaurant-detail?id=" + restaurantId;
     * }
     *
     * @PostMapping("/edit")
     * public String legacyEdit(@RequestParam Long reviewId, @RequestParam int rating, @RequestParam String comment,
     *                          @RequestParam Long restaurantId, HttpSession s) {
     *     Long me = loginId(s);
     *     if (me == null) return "redirect:/restaurant-detail?id=" + restaurantId + "&err=login";
     *     if (!reviewService.updateReview(reviewId, me, rating, comment))
     *         return "redirect:/restaurant-detail?id=" + restaurantId + "&err=forbidden";
     *     return "redirect:/restaurant-detail?id=" + restaurantId;
     * }
     */
}
