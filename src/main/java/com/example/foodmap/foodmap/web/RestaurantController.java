package com.example.foodmap.foodmap.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import com.example.foodmap.foodmap.domain.RestaurantService;
import com.example.foodmap.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.foodmap.dto.RestaurantDto;
import com.example.foodmap.foodmap.dto.ReviewDto;
import com.example.foodmap.foodmap.dto.ReviewCreateRequest;
import com.example.foodmap.foodmap.dto.ReviewUpdateRequest;
import com.example.foodmap.foodmap.domain.RestaurantPhoto;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {

    private final RestaurantService service;

    public RestaurantController(RestaurantService service) {
        this.service = service;
    }

    // ───────────────────────────────── search 列表（保留你的原本邏輯）
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
        } catch (NumberFormatException e) {
            memberIdLong = null;
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            return service.searchByKeyword(keyword.trim(), pageRequest, memberIdLong);
        }

        if (county != null && !county.trim().isEmpty() &&
            type != null && !type.trim().isEmpty() &&
            (minRating == null || minRating == 0)) {
            return service.searchByCountyAndType(county.trim(), type.trim(), pageRequest, memberIdLong);
        }

        return service.searchRestaurants(county, minRating, type, pageRequest, memberIdLong);
    }

    // ───────────────────────────────── details（包成 ResponseEntity，錯誤時回 JSON）
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getRestaurantDetails(
            @PathVariable("id") Long restaurantId,
            @RequestParam(value = "memberId", required = false) String memberIdStr) {

        Long memberId = null;
        if (memberIdStr != null && !memberIdStr.isBlank()) {
            try { memberId = Long.parseLong(memberIdStr); } catch (NumberFormatException ignore) {}
        }

        try {
            RestaurantDetailsDTO dto = service.getRestaurantDetails(restaurantId, memberId);
            if (dto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error","NOT_FOUND","id", restaurantId));
            }
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error","INTERNAL_ERROR","message", e.getMessage()));
        }
    }

    // ───────────────────────────────── 分頁取得評論
    @GetMapping("/{id}/reviews")
    public Page<ReviewDto> getReviews(
            @PathVariable("id") Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "false") boolean includeHidden
    ) {
        return service.getReviewPage(restaurantId, page, size, sortBy, order, includeHidden);
    }

    // ───────────────────────────────── 新增評論（JSON）
    @PostMapping(value = "/{id}/reviews", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createReviewJson(
            @PathVariable("id") Long restaurantId,
            @Valid @RequestBody ReviewCreateRequest req
    ) {
        try {
            ReviewDto dto = service.createReview(
                    restaurantId,
                    req.getMemberId() == null ? null : req.getMemberId().longValue(),
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

    // ───────────────────────────────── 新增評論（Form）
    @PostMapping(value = "/{id}/reviews", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> createReviewForm(
            @PathVariable("id") Long restaurantId,
            @Valid ReviewCreateRequest req
    ) {
        try {
            ReviewDto dto = service.createReview(
                    restaurantId,
                    req.getMemberId() == null ? null : req.getMemberId().longValue(),
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

    // ───────────────────────────────── 編輯評論（JSON / Form 都支援）
    @PutMapping("/{id}/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long reviewId,
            @RequestParam("memberId") Long memberId,
            @Valid @RequestBody(required = false) ReviewUpdateRequest body, // JSON
            @Valid ReviewUpdateRequest form                                  // FORM
    ) {
        // 用一下 restaurantId（避免 IDE 提醒未使用；也順便做存在性檢查）
        service.getRestaurantById(restaurantId);

        ReviewUpdateRequest req = (body != null ? body : form);
        if (req == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "BAD_REQUEST", "message", "缺少內容"));
        }
        boolean ok = service.updateReview(reviewId, memberId, req.getRating(), req.getComment());
        return ok ? ResponseEntity.ok(Map.of("ok", true))
                  : ResponseEntity.status(HttpStatus.FORBIDDEN)
                                  .body(Map.of("error", "FORBIDDEN", "message", "只能編輯自己的評論"));
    }

    // ───────────────────────────────── 刪除自己的評論
    @DeleteMapping("/{id}/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long reviewId,
            @RequestParam("memberId") Long memberId
    ) {
        // 用一下 restaurantId（避免 IDE 提醒未使用；也順便做存在性檢查）
        service.getRestaurantById(restaurantId);

        boolean ok = service.deleteReviewByIdAndMemberId(reviewId, memberId);
        return ok ? ResponseEntity.ok(Map.of("ok", true))
                  : ResponseEntity.status(HttpStatus.FORBIDDEN)
                                  .body(Map.of("error", "FORBIDDEN", "message", "只能刪除自己的評論"));
    }

    // ───────────────────────────────── （管理員）隱藏/取消隱藏評論
    @PatchMapping("/{id}/reviews/{reviewId}/hidden")
    public ResponseEntity<?> setHidden(
            @PathVariable("id") Long restaurantId,
            @PathVariable Long reviewId,
            @RequestParam("hidden") boolean hidden
    ) {
        // 用一下 restaurantId（避免 IDE 提醒未使用；也順便做存在性檢查）
        service.getRestaurantById(restaurantId);

        // 這裡用舊的管理端方法（不做作者檢查）；若要做權限，可改用 setReviewHidden(reviewId, actorId, hidden, isAdmin)
        service.setReviewHidden(reviewId, hidden);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ───────────────────────────────── 照片串流端點（依位元判斷 Content-Type）
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

    // ───────────────────────────────── 收藏切換（回傳目前是否已收藏）
    @PostMapping("/{id}/favorite/toggle")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable("id") Long restaurantId,
            @RequestParam("memberId") Long memberId) {

        boolean favorite = service.toggleFavorite(restaurantId, memberId);
        return ResponseEntity.ok(Map.of("favorite", favorite));
    }
}
