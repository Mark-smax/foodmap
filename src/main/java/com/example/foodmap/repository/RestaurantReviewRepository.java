package com.example.foodmap.repository;

import com.example.foodmap.model.RestaurantReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, Long> {

    // 撈出該餐廳最新的所有評論（已經有的）
    List<RestaurantReview> findByRestaurantIdOrderByCreatedTimeDesc(Long restaurantId);

    // 🔍 撈出該餐廳最新的 5 則評論（用於詳細頁）
    List<RestaurantReview> findTop5ByRestaurantIdOrderByCreatedTimeDesc(Long restaurantId);
}
