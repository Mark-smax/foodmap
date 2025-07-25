package com.example.foodmap.service;

import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.repository.RestaurantReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
     * 刪除評論（只能刪自己的）
     */
    public void deleteReviewByIdAndMemberId(Long reviewId, Long memberId) {
        reviewRepo.deleteByIdAndMemberId(reviewId, memberId);
    }

    /**
     * 修改評論（只能改自己的）
     */
    public void updateReview(Long reviewId, Long memberId, int rating, String comment) {
        RestaurantReview review = reviewRepo.findByIdAndMemberId(reviewId, memberId);
        if (review != null) {
            review.setRating(rating);
            review.setComment(comment);
            reviewRepo.save(review);
        }
    }
}
