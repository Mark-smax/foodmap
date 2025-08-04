package com.example.foodmap.foodmap.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantFavoriteRepository extends JpaRepository<RestaurantFavorite, Long> {

    // 檢查某會員是否收藏某餐廳
    boolean existsByRestaurantIdAndMemberId(Long restaurantId, Long memberId);

    // 移除收藏
    void deleteByRestaurantIdAndMemberId(Long restaurantId, Long memberId);
}
