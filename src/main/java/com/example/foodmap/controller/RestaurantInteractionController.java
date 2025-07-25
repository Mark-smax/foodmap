package com.example.foodmap.controller;

import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.model.RestaurantFavorite;
import com.example.foodmap.member.domain.Member;
import com.example.foodmap.service.RestaurantService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/restaurant")
public class RestaurantInteractionController {

    private final RestaurantService restaurantService;

    public RestaurantInteractionController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    // ⭐ 評論提交
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

    // ❤️ 收藏 / 取消
    @PostMapping("/favorite")
    public String toggleFavorite(@RequestParam Long restaurantId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入才能收藏！");
            return "redirect:/restaurant-detail?id=" + restaurantId;
        }

        Long memberId = loginUser.getMemberId().longValue();

        restaurantService.toggleFavorite(restaurantId, memberId);

        return "redirect:/restaurant-detail?id=" + restaurantId;
    }
    
    
}
