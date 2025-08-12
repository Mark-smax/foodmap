package com.example.foodmap.foodmap.domain;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.foodmap.foodmap.domain.RestaurantPhoto;

@Repository
public interface RestaurantPhotoRepository extends JpaRepository<RestaurantPhoto, Long> {

    // 取某餐廳最前面的 5 張照片（你原本的方法）
    List<RestaurantPhoto> findTop5ByRestaurantIdOrderByIdAsc(Long restaurantId);

    // 取某餐廳所有照片（依 id 由小到大）
    List<RestaurantPhoto> findAllByRestaurantIdOrderByIdAsc(Long restaurantId);
}
