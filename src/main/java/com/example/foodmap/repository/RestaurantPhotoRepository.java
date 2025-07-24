package com.example.foodmap.repository;

import com.example.foodmap.model.RestaurantPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantPhotoRepository extends JpaRepository<RestaurantPhoto, Long> {
    List<RestaurantPhoto> findTop5ByRestaurantIdOrderByIdAsc(Long restaurantId);
}
