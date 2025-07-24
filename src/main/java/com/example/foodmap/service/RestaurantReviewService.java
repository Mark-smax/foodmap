package com.example.foodmap.service;

import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.repository.RestaurantReviewRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantReviewService {

    private final RestaurantReviewRepository reviewRepo;

    public RestaurantReviewService(RestaurantReviewRepository reviewRepo) {
        this.reviewRepo = reviewRepo;
    }

    public List<RestaurantReview> getReviewsByRestaurantId(Long restaurantId) {
        return reviewRepo.findByRestaurantIdOrderByCreatedTimeDesc(restaurantId);
    }

    public RestaurantReview insertReview(RestaurantReview review) {
        return reviewRepo.save(review);
    }
    // 其他 review 相關邏輯...
}
