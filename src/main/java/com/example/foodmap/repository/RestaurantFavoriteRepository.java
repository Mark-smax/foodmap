package com.example.foodmap.repository;

public interface RestaurantFavoriteRepository {
    boolean isFavorite(Long restaurantId, Long memberId);
}
