package com.example.foodmap.foodmap.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantFavoriteRepository extends JpaRepository<RestaurantFavorite, Long> {

    // ✅ 修改成 Integer，跟 Entity 欄位一致
    boolean existsByRestaurantIdAndMemberId(Long restaurantId, Integer memberId);

    void deleteByRestaurantIdAndMemberId(Long restaurantId, Integer memberId);
}
