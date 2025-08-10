package com.example.foodmap.foodmap.web;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.foodmap.foodmap.domain.RestaurantReview;
import com.example.foodmap.foodmap.domain.RestaurantReviewRepository;
import com.example.foodmap.foodmap.domain.RestaurantService;
import com.example.foodmap.member.domain.Member;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/restaurant")
public class RestaurantInteractionController {

    private final RestaurantService restaurantService;
    private final RestaurantReviewRepository reviewRepository;

    public RestaurantInteractionController(RestaurantService restaurantService,
                                           RestaurantReviewRepository reviewRepository) {
        this.restaurantService = restaurantService;
        this.reviewRepository = reviewRepository;
    }

    // ⭐ 發表評論（/restaurant/review）
    @PostMapping("/review")
    public String postReview(@RequestParam Long restaurantId,
                             @RequestParam int rating,
                             @RequestParam String comment,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        // session 內存的是 Integer（對應 members.member_id:int）
        Integer loginMemberId = (Integer) session.getAttribute("loginMemberId");
        if (loginMemberId == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入再評論！");
            return "redirect:/restaurant-detail?id=" + restaurantId;
        }

        if (rating < 1 || rating > 5 || comment == null || comment.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "評論內容與星等必填！");
            return "redirect:/restaurant-detail?id=" + restaurantId;
        }

        // 建立評論
        RestaurantReview review = new RestaurantReview();
        review.setRestaurantId(restaurantId);       // Long
        review.setMemberId(loginMemberId);          // ✅ Integer（與 entity / DB 一致）
        review.setRating(rating);
        review.setComment(comment.trim());
        review.setCreatedTime(LocalDateTime.now());
        review.setIsHidden(false);

        restaurantService.saveReview(review);

        redirectAttributes.addFlashAttribute("success", "已送出評論！");
        return "redirect:/restaurant-detail?id=" + restaurantId;
    }

    // ❤️ 收藏 / 取消收藏（/restaurant/favorite）
    @PostMapping("/favorite")
    public String toggleFavorite(@RequestParam Long restaurantId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Integer loginMemberId = (Integer) session.getAttribute("loginMemberId");
        if (loginMemberId == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入才能收藏！");
            return "redirect:/restaurant-detail?id=" + restaurantId;
        }

        // 收藏那邊的 service 若吃 Long，就轉成 Long；不影響評論那邊的 Integer
        Long memberIdForFavorite = loginMemberId.longValue();
        restaurantService.toggleFavorite(restaurantId, memberIdForFavorite);

        return "redirect:/restaurant-detail?id=" + restaurantId;
    }

    // ✏️ 編輯評論（/restaurant/review/edit）
    @PostMapping("/review/edit")
    public String editReview(@RequestParam Long reviewId,
                             @RequestParam int rating,
                             @RequestParam String comment,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入才能編輯評論！");
            return "redirect:/";
        }

        // Member.getMemberId() 應為 Integer（對應 DB int）
        Integer memberId = loginUser.getMemberId();

        // Repository 方法請確認簽名為：findByIdAndMemberId(Long reviewId, Integer memberId)
        RestaurantReview review = reviewRepository.findByIdAndMemberId(reviewId, memberId);
        if (review == null) {
            redirectAttributes.addFlashAttribute("error", "無權編輯此評論！");
            return "redirect:/";
        }

        if (rating < 1 || rating > 5 || comment == null || comment.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "評論內容與星等必填！");
            return "redirect:/restaurant-detail?id=" + review.getRestaurantId();
        }

        review.setRating(rating);
        review.setComment(comment.trim());
        review.setCreatedTime(LocalDateTime.now());

        reviewRepository.save(review);

        redirectAttributes.addFlashAttribute("success", "已更新評論！");
        return "redirect:/restaurant-detail?id=" + review.getRestaurantId();
    }

    // 🗑️ 刪除評論（/restaurant/review/delete）
    @PostMapping("/review/delete")
    public String deleteReview(@RequestParam Long reviewId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入才能刪除評論！");
            return "redirect:/";
        }

        Integer memberId = loginUser.getMemberId();

        // Repository 方法請確認簽名為：findByIdAndMemberId(Long reviewId, Integer memberId)
        RestaurantReview review = reviewRepository.findByIdAndMemberId(reviewId, memberId);
        if (review == null) {
            redirectAttributes.addFlashAttribute("error", "無權刪除此評論！");
            return "redirect:/";
        }

        Long restaurantId = review.getRestaurantId();
        reviewRepository.deleteById(reviewId);

        redirectAttributes.addFlashAttribute("success", "已刪除評論！");
        return "redirect:/restaurant-detail?id=" + restaurantId;
    }
}
