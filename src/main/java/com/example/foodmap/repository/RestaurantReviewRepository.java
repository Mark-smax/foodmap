package com.example.foodmap.repository;

import com.example.foodmap.model.RestaurantReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, Long> {

    // 🔍 根據餐廳 ID 取得所有評論
    List<RestaurantReview> findByRestaurantId(Long restaurantId);

    // 🔍 根據餐廳 ID 取得評論（依時間排序，給詳細頁用）
    List<RestaurantReview> findByRestaurantIdOrderByCreatedTimeDesc(Long restaurantId);

    // ✅ 檢查某會員是否對某餐廳已經發表過評論（用於防止重複評論）
    boolean existsByRestaurantIdAndMemberId(Long restaurantId, Long memberId);

    // ❌ 刪除特定會員對特定餐廳的評論（用於取消評論功能）
    void deleteByRestaurantIdAndMemberId(Long restaurantId, Long memberId);

    // ❌ 刪除自己的評論（安全性更高，只刪除該會員的）
    void deleteByIdAndMemberId(Long id, Long memberId);

    // 🔍 找出某會員自己發表的特定評論（用於編輯與刪除權限確認）
    RestaurantReview findByIdAndMemberId(Long id, Long memberId);
}
