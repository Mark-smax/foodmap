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

    // ⭐ 發表評論
    @PostMapping("/review")
    public String postReview(@RequestParam Long restaurantId,
                             @RequestParam int rating,
                             @RequestParam String comment,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        Integer loginMemberIdInt = (Integer) session.getAttribute("loginMemberId");
        if (loginMemberIdInt == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入再評論！");
            return "redirect:/restaurant-detail?id=" + restaurantId;
        }

        Long loginMemberId = loginMemberIdInt.longValue();

        if (rating < 1 || rating > 5 || comment.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "評論內容與星等必填！");
            return "redirect:/restaurant-detail?id=" + restaurantId;
        }

        System.out.println("收到評論 => 餐廳ID: " + restaurantId + ", 會員ID: " + loginMemberId + ", 星等: " + rating + ", 內容: " + comment);

        RestaurantReview review = new RestaurantReview();
        review.setRestaurantId(restaurantId);
        review.setMemberId(loginMemberId);
        review.setRating(rating);
        review.setComment(comment.trim());
        review.setCreatedTime(LocalDateTime.now());

        restaurantService.saveReview(review);

        return "redirect:/restaurant-detail?id=" + restaurantId;
    }

    // ❤️ 收藏 / 取消收藏
    @PostMapping("/favorite")
    public String toggleFavorite(@RequestParam Long restaurantId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Integer loginMemberIdInt = (Integer) session.getAttribute("loginMemberId");
        if (loginMemberIdInt == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入才能收藏！");
            return "redirect:/restaurant-detail?id=" + restaurantId;
        }

        Long memberId = loginMemberIdInt.longValue();
        restaurantService.toggleFavorite(restaurantId, memberId);

        return "redirect:/restaurant-detail?id=" + restaurantId;
    }


    // ✏️ 編輯評論
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

        Long memberId = loginUser.getMemberId().longValue();
        RestaurantReview review = reviewRepository.findByIdAndMemberId(reviewId, memberId);

        if (review == null) {
            redirectAttributes.addFlashAttribute("error", "無權編輯此評論！");
            return "redirect:/";
        }

        review.setRating(rating);
        review.setComment(comment.trim());
        review.setCreatedTime(LocalDateTime.now());

        reviewRepository.save(review);

        return "redirect:/restaurant-detail?id=" + review.getRestaurantId();
    }

    // 🗑️ 刪除評論
    @PostMapping("/review/delete")
    public String deleteReview(@RequestParam Long reviewId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入才能刪除評論！");
            return "redirect:/";
        }

        Long memberId = loginUser.getMemberId().longValue();
        RestaurantReview review = reviewRepository.findByIdAndMemberId(reviewId, memberId);

        if (review == null) {
            redirectAttributes.addFlashAttribute("error", "無權刪除此評論！");
            return "redirect:/";
        }

        reviewRepository.deleteById(reviewId);
        return "redirect:/restaurant-detail?id=" + review.getRestaurantId();
    }
}
