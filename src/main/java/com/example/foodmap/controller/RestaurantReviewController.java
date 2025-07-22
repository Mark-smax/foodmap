package com.example.foodmap.controller;

import com.example.foodmap.dao.RestaurantReviewDao;
import com.example.foodmap.model.RestaurantReview;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class RestaurantReviewController {

    private final RestaurantReviewDao reviewDao = new RestaurantReviewDao();

    @PostMapping
    public String addReview(@RequestBody RestaurantReview review) {
        if (review.getRestaurantId() == null || review.getMemberId() == null || review.getRating() < 1 || review.getRating() > 5) {
            return "Invalid input";
        }

        reviewDao.insertReview(review);
        return "Review added successfully";
    }
}
