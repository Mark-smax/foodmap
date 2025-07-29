package com.example.foodmap.foodmap.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.foodmap.foodmap.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRestaurantId(Long restaurantId);
}
