package com.example.foodmap.foodmap.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, Long> {

    // 依餐廳查全部評論（不分頁）
    List<RestaurantReview> findByRestaurantId(Long restaurantId);

    // 依餐廳查全部評論（時間新到舊）
    List<RestaurantReview> findByRestaurantIdOrderByCreatedTimeDesc(Long restaurantId);

    // 該會員是否已對該餐廳評論（避免重複評論）
    boolean existsByRestaurantIdAndMemberId(Long restaurantId, Integer memberId);

    // 刪除某會員對該餐廳的評論
    void deleteByRestaurantIdAndMemberId(Long restaurantId, Integer memberId);

    // 刪除自己的某一筆評論
    void deleteByIdAndMemberId(Long id, Integer memberId);

    // 取某會員自己的某一筆評論（例如做權限檢查）
    RestaurantReview findByIdAndMemberId(Long id, Integer memberId);

    // 分頁查全部評論（含隱藏與未隱藏）
    Page<RestaurantReview> findByRestaurantId(Long restaurantId, Pageable pageable);

    // 分頁查未隱藏評論
    Page<RestaurantReview> findByRestaurantIdAndIsHiddenFalse(Long restaurantId, Pageable pageable);
}
