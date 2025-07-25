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

        Integer loginMemberIdInt = (Integer) session.getAttribute("loginMemberId");
        Long loginMemberId = loginMemberIdInt.longValue();

        Member loginUser = (Member) session.getAttribute("loginUser"); // ⭐ 加這行
        model.addAttribute("loginUser", loginUser); // ⭐ 加這行

        RestaurantDetailsDTO dto = restaurantService.getRestaurantDetails(id, loginMemberId);

        List<RestaurantReview> reviews = dto.getReviews();
        System.out.println("取得評論數量：" + reviews.size());
        for (RestaurantReview r : reviews) {
            System.out.println("⭐ " + r.getRating() + " 星，評論：" + r.getComment());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<String> reviewTimes = reviews.stream()
                .map(r -> r.getCreatedTime().format(formatter))
                .collect(Collectors.toList());

        model.addAttribute("restaurant", dto.getRestaurant());
        model.addAttribute("photos", dto.getPhotoBase64List());
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewTimes", reviewTimes);
        model.addAttribute("favorite", dto.isFavorite());

        return "restaurant-detail";
    }

}
