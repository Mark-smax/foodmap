package com.example.foodmap.foodmap.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.foodmap.foodmap.domain.RestaurantReview;
import com.example.foodmap.foodmap.domain.RestaurantReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = "*") // 前端可能會跨域呼叫
public class RestaurantReviewController {

    @Autowired
    private RestaurantReviewService reviewService;

    // POST 新增評論
    @PostMapping
    public ResponseEntity<RestaurantReview> createReview(@RequestBody RestaurantReview review) {
        RestaurantReview savedReview = reviewService.insertReview(review);
        return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
    }

    // GET 取得特定餐廳的所有評論
    @GetMapping("/{restaurantId}")
    public List<RestaurantReview> getReviews(@PathVariable Long restaurantId) {
        return reviewService.getReviewsByRestaurantId(restaurantId);
    }

    // 🗑️ 刪除評論（只能本人或管理員）
    @PostMapping("/delete")
    public ResponseEntity<String> deleteReview(@RequestParam Long reviewId,
                                               @RequestParam Long restaurantId,
                                               @RequestParam Long memberId) {
        boolean success = reviewService.deleteReviewByIdAndMemberId(reviewId, memberId);
        if (success) {
            return new ResponseEntity<>("評論刪除成功", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("刪除失敗，您沒有權限", HttpStatus.FORBIDDEN);
        }
    }

    // ✏️ 編輯評論（只能本人）
    @PostMapping("/edit")
    public ResponseEntity<String> editReview(@RequestParam Long reviewId,
                                             @RequestParam int rating,
                                             @RequestParam String comment,
                                             @RequestParam Long memberId) {
        boolean success = reviewService.updateReview(reviewId, memberId, rating, comment);
        if (success) {
            return new ResponseEntity<>("評論更新成功", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("更新失敗，您沒有權限或資料錯誤", HttpStatus.FORBIDDEN);
        }
    }

    // ✅ 管理員隱藏留言
    @PostMapping("/hide")
    public ResponseEntity<String> hideReview(@RequestParam Long reviewId,
                                             @RequestParam Long restaurantId,
                                             @RequestParam String role) {
        if ("admin".equals(role)) {
            reviewService.setReviewHidden(reviewId, true); // 將評論設為隱藏
            return new ResponseEntity<>("評論已被隱藏", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("您沒有權限隱藏評論", HttpStatus.FORBIDDEN);
        }
    }
}
