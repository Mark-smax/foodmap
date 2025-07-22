package com.example.foodmap.service;

import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.repository.RestaurantReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantReviewService {

    private final RestaurantReviewRepository reviewRepository;

    public RestaurantReviewService(RestaurantReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public RestaurantReview insertReview(RestaurantReview review) {
        review.setCreatedTime(java.time.LocalDateTime.now());
        return reviewRepository.save(review);
    }

    public List<RestaurantReview> getReviewsByRestaurantId(Long restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByCreatedTimeDesc(restaurantId);
    }
}
