package com.example.foodmap.foodmap.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/restaurant")
public class RestaurantPublicRedirectController {

    // 讓 /restaurant/{id} 轉址到既有的 /restaurant-detail?id=...
    @GetMapping("/{id}")
    public String redirectToDetail(@PathVariable Long id) {
        return "redirect:/restaurant-detail?id=" + id;
    }
}
