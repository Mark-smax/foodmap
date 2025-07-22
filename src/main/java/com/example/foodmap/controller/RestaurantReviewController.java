package com.example.foodmap.controller;

import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.service.RestaurantReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class RestaurantReviewController {

    @Autowired
    private RestaurantReviewService reviewService;

    @PostMapping
    public ResponseEntity<RestaurantReview> createReview(@RequestBody RestaurantReview review) {
        RestaurantReview savedReview = reviewService.insertReview(review);
        return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
    }

    @GetMapping("/{restaurantId}")
    public List<RestaurantReview> getReviews(@PathVariable Long restaurantId) {
        return reviewService.getReviewsByRestaurantId(restaurantId);
    }
}
