package com.example.foodmap.repository;

import com.example.foodmap.model.RestaurantFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantFavoriteRepository extends JpaRepository<RestaurantFavorite, Long> {
    boolean existsByRestaurantIdAndMemberId(Long restaurantId, Long memberId);
}
