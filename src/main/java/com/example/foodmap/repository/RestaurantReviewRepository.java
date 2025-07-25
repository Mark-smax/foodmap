package com.example.foodmap.repository;

import com.example.foodmap.model.RestaurantReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, Long> {

	List<RestaurantReview> findByRestaurantId(Long restaurantId); // 🔺 這是你 RestaurantService 用的

	List<RestaurantReview> findByRestaurantIdOrderByCreatedTimeDesc(Long restaurantId); // 🔺 這是 ReviewService 用的

	boolean existsByRestaurantIdAndMemberId(Long restaurantId, Long memberId);

	void deleteByRestaurantIdAndMemberId(Long restaurantId, Long memberId);
}
