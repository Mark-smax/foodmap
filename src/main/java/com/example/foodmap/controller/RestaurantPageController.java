package com.example.foodmap.controller;

import com.example.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.service.RestaurantService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RestaurantPageController {

    private final RestaurantService restaurantService;

    public RestaurantPageController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/restaurant-detail")
    public String showDetailPage(@RequestParam Long id,
                                 @RequestParam(required = false) Long memberId,
                                 Model model) {

        RestaurantDetailsDTO dto = restaurantService.getRestaurantDetails(id, memberId);

        // 將 Review 中的 createdTime 格式化為 yyyy-MM-dd HH:mm
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<String> reviewTimes = dto.getReviews().stream()
            .map(r -> r.getCreatedTime().format(formatter))
            .collect(Collectors.toList());

        model.addAttribute("restaurant", dto.getRestaurant());
        model.addAttribute("photos", dto.getPhotoBase64List());
        model.addAttribute("reviews", dto.getReviews());
        model.addAttribute("reviewTimes", reviewTimes); // 傳給 HTML 顯示時間
        model.addAttribute("favorite", dto.isFavorite());

        return "restaurant-detail"; // templates/restaurant-detail.html
    }
}
