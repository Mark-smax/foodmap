package com.example.foodmap.repository;

import com.example.foodmap.model.RestaurantReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, Long> {
    List<RestaurantReview> findByRestaurantIdOrderByCreatedTimeDesc(Long restaurantId);
}
