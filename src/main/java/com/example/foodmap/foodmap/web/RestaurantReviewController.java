package com.example.foodmap.foodmap.web;

import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.service.RestaurantReviewService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    
   
}
