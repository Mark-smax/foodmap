package com.example.foodmap.controller;

import com.example.foodmap.member.domain.Member;
import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.service.RestaurantReviewService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    
    @PostMapping("/restaurant/review/delete")
    public String deleteReview(@RequestParam Long reviewId,
                               @RequestParam Long restaurantId,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入！");
            return "redirect:/restaurant-detail?id=" + restaurantId;
        }

        reviewService.deleteReviewByIdAndMemberId(reviewId, loginUser.getMemberId());
        return "redirect:/restaurant-detail?id=" + restaurantId;
    }

    @PostMapping("/restaurant/review/edit")
    public String editReview(@RequestParam Long reviewId,
                             @RequestParam int rating,
                             @RequestParam String comment,
                             @RequestParam Long restaurantId,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入！");
            return "redirect:/restaurant-detail?id=" + restaurantId;
        }

        reviewService.updateReview(reviewId, loginUser.getMemberId(), rating, comment);
        return "redirect:/restaurant-detail?id=" + restaurantId;
    }
}
