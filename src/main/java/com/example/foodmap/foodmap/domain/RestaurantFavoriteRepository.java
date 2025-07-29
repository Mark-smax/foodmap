package com.example.foodmap.foodmap.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.foodmap.foodmap.domain.RestaurantFavorite;

@Repository
public interface RestaurantFavoriteRepository extends JpaRepository<RestaurantFavorite, Long> {
	boolean existsByRestaurantIdAndMemberId(Long restaurantId, Long memberId);

	void deleteByRestaurantIdAndMemberId(Long restaurantId, Long memberId);
}
