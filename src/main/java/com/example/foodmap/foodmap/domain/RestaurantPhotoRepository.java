package com.example.foodmap.foodmap.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.foodmap.foodmap.domain.RestaurantPhoto;

@Repository
public interface RestaurantPhotoRepository extends JpaRepository<RestaurantPhoto, Long> {
    List<RestaurantPhoto> findTop5ByRestaurantIdOrderByIdAsc(Long restaurantId);
}
