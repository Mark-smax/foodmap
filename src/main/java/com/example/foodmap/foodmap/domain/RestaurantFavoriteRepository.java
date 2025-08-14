package com.example.foodmap.foodmap.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantFavoriteRepository extends JpaRepository<RestaurantFavorite, Long> {

    /**
     * 取得某會員對某餐廳的收藏紀錄（用於 toggle 時判斷）
     */
    Optional<RestaurantFavorite> findByRestaurantIdAndMemberId(Long restaurantId, Long memberId);

    /**
     * 檢查某會員是否已收藏某餐廳
     */
    boolean existsByRestaurantIdAndMemberId(Long restaurantId, Long memberId);

    /**
     * 移除收藏（刪除符合條件的記錄）
     * 也可以改成 long deleteByRestaurantIdAndMemberId(...) 取得刪除筆數
     */
    void deleteByRestaurantIdAndMemberId(Long restaurantId, Long memberId);

    /**
     * 計算餐廳被收藏次數（可用於熱門排序或顯示）
     */
    long countByRestaurantId(Long restaurantId);

    /**
     * 查詢某會員所有收藏清單（若之後要做「我的收藏」頁面）
     */
    List<RestaurantFavorite> findByMemberId(Long memberId);
}
