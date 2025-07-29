package com.example.foodmap.foodmap.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.foodmap.foodmap.domain.RestaurantReview;
import com.example.foodmap.foodmap.domain.RestaurantReviewService;

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
