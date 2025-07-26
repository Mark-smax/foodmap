package com.example.foodmap.controller;

import com.example.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.service.RestaurantService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RestaurantPageController {

    private final RestaurantService restaurantService;

    public RestaurantPageController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    /**
     * 顯示餐廳詳細頁面（含評論、圖片、是否已收藏）
     * URL: /restaurant-detail?id=1
     */
    @GetMapping("/restaurant-detail")
    public String showDetailPage(@RequestParam("id") Long id,
                                 HttpSession session,
                                 Model model) {

        // 取得登入會員 ID（若未登入則為 null）
        Integer loginMemberIdInt = (Integer) session.getAttribute("loginMemberId");
        Long loginMemberId = (loginMemberIdInt != null) ? loginMemberIdInt.longValue() : null;

        // ✅ 傳 loginMemberId 給 Thymeleaf 用來比對留言者是否是本人
        model.addAttribute("loginMemberId", loginMemberId);

        // 取得餐廳詳細資料（含照片、評論、是否收藏）
        RestaurantDetailsDTO dto = restaurantService.getRestaurantDetails(id, loginMemberId);

        // 取得評論並格式化時間
        List<RestaurantReview> reviews = dto.getReviews();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<String> reviewTimes = reviews.stream()
                .map(r -> r.getCreatedTime().format(formatter))
                .collect(Collectors.toList());

        // 傳資料給 View
        model.addAttribute("restaurant", dto.getRestaurant());
        model.addAttribute("photos", dto.getPhotoBase64List());
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewTimes", reviewTimes);
        model.addAttribute("favorite", dto.isFavorite());

        return "restaurant-detail"; // 對應 Thymeleaf 的 restaurant-detail.html
    }
}
