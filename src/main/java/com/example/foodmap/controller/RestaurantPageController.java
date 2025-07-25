package com.example.foodmap.controller;

import com.example.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.member.domain.Member;
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

        // 從 session 中取得登入的會員（若未登入則為 null）
    	Integer loginMemberIdInt = (Integer) session.getAttribute("loginMemberId");
    	
    	Long loginMemberId = loginMemberIdInt.longValue();

        // 從 Service 層取得詳細資料
        RestaurantDetailsDTO dto = restaurantService.getRestaurantDetails(id, loginMemberId);

        // ✅ 印出評論資訊方便除錯
        List<RestaurantReview> reviews = dto.getReviews();
        System.out.println("取得評論數量：" + reviews.size());
        for (RestaurantReview r : reviews) {
            System.out.println("⭐ " + r.getRating() + " 星，評論：" + r.getComment());
        }

        // 格式化評論時間（yyyy-MM-dd HH:mm）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<String> reviewTimes = reviews.stream()
                .map(r -> r.getCreatedTime().format(formatter))
                .collect(Collectors.toList());

        // 傳資料給 view
        model.addAttribute("restaurant", dto.getRestaurant());
        model.addAttribute("photos", dto.getPhotoBase64List());
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewTimes", reviewTimes);
        model.addAttribute("favorite", dto.isFavorite());

        return "restaurant-detail"; // 對應 resources/templates/restaurant-detail.html
    }
}
