package com.example.foodmap.foodmap.domain;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class RestaurantReviewService {

    private final RestaurantReviewRepository reviewRepo;

    public RestaurantReviewService(RestaurantReviewRepository reviewRepo) {
        this.reviewRepo = reviewRepo;
    }

    /**
     * 根據餐廳 ID 取得所有評論（按時間排序）
     */
    public List<RestaurantReview> getReviewsByRestaurantId(Long restaurantId) {
        return reviewRepo.findByRestaurantIdOrderByCreatedTimeDesc(restaurantId);
    }

    /**
     * 新增一筆評論
     */
    public RestaurantReview insertReview(RestaurantReview review) {
        return reviewRepo.save(review);
    }

    /**
     * 刪除評論（只能刪自己的評論）✔️
     */
    
    @Transactional
    public void setReviewHidden(Long reviewId, boolean hidden) {
        RestaurantReview review = reviewRepo.findById(reviewId).orElse(null);
        if (review != null) {
            review.setIsHidden(hidden);
        }
    }

    @Transactional
    public boolean deleteReviewByIdAndMemberId(Long reviewId, Long memberId) {
        System.out.println("🧨 刪除請求 reviewId = " + reviewId + ", memberId = " + memberId);

        RestaurantReview review = reviewRepo.findByIdAndMemberId(reviewId, memberId);
        if (review == null) {
            // 額外查一次看是哪個 member 留的
            review = reviewRepo.findById(reviewId).orElse(null);
            if (review != null) {
                System.out.println("⚠️ 該評論實際是 memberId = " + review.getMemberId());
            } else {
                System.out.println("❌ 查無此評論 ID");
            }
            return false;
        }

        reviewRepo.delete(review);
        System.out.println("✅ 刪除成功");
        return true;
    }

    /**
     * 修改評論（只能編輯自己的評論）✔️
     */
    @Transactional
    public boolean updateReview(Long reviewId, Long memberId, int rating, String comment) {
        System.out.println("🛠️ 編輯請求 reviewId = " + reviewId + ", memberId = " + memberId + ", rating = " + rating + ", comment = " + comment);

        RestaurantReview review = reviewRepo.findByIdAndMemberId(reviewId, memberId);
        if (review == null) {
            // 額外查一次看是哪個 member 留的
            review = reviewRepo.findById(reviewId).orElse(null);
            if (review != null) {
                System.out.println("⚠️ 該評論實際是 memberId = " + review.getMemberId());
            } else {
                System.out.println("❌ 查無此評論 ID");
            }
            return false;
        }

        review.setRating(rating);
        review.setComment(comment);
        System.out.println("✅ 更新成功");
        return true;
    }
}
