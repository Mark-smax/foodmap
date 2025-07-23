package com.example.foodmap.repository;

import java.util.List;

public interface RestaurantPhotoRepository {
    List<byte[]> findTop5ImagesByRestaurantId(Long restaurantId);
}
