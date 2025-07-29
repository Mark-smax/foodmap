package com.example.foodmap.foodmap.domain;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.foodmap.member.domain.MemberRepository;

@Service
public class RestaurantReviewService {

    private final RestaurantReviewRepository reviewRepo;
    private final MemberRepository memberRepository;

    public RestaurantReviewService(RestaurantReviewRepository reviewRepo, MemberRepository memberRepository) {
        this.reviewRepo = reviewRepo;
        this.memberRepository = memberRepository;
    }

    /**
     * 根據餐廳 ID 取得所有評論（按時間排序）
     */
    public List<RestaurantReview> getReviewsByRestaurantId(Long restaurantId) {
        List<RestaurantReview> reviews = reviewRepo.findByRestaurantIdOrderByCreatedTimeDesc(restaurantId);

        // 為每個評論填充上傳者的暱稱
        for (RestaurantReview review : reviews) {
            // 轉換 memberId 為 Integer，並查詢 Member
            Integer memberId = review.getMemberId().intValue(); // 轉換 Long -> Integer
            memberRepository.findById(memberId).ifPresent(member -> {
                review.setMemberNickName(member.getMemberNickName()); // 設置評論者的暱稱
            });
        }

        return reviews;
    }

    /**
     * 新增一筆評論
     */
    public RestaurantReview insertReview(RestaurantReview review) {
        return reviewRepo.save(review);
    }

    /**
     * 設置評論為隱藏或顯示（管理員操作）
     */
    @Transactional
    public void setReviewHidden(Long reviewId, boolean hidden) {
        RestaurantReview review = reviewRepo.findById(reviewId).orElse(null);
        if (review != null) {
            review.setIsHidden(hidden);
        }
    }

    /**
     * 刪除評論（只能刪除自己的評論）
     */
    @Transactional
    public boolean deleteReviewByIdAndMemberId(Long reviewId, Long memberId) {
        System.out.println("🧨 刪除請求 reviewId = " + reviewId + ", memberId = " + memberId);

        // 查詢該評論是否為該會員所發
        RestaurantReview review = reviewRepo.findByIdAndMemberId(reviewId, memberId);
        if (review == null) {
            review = reviewRepo.findById(reviewId).orElse(null); // 額外查詢看看是誰的評論
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
     * 修改評論（只能編輯自己的評論）
     */
    @Transactional
    public boolean updateReview(Long reviewId, Long memberId, int rating, String comment) {
        System.out.println("🛠️ 編輯請求 reviewId = " + reviewId + ", memberId = " + memberId + ", rating = " + rating + ", comment = " + comment);

        // 查詢該評論是否為該會員所發
        RestaurantReview review = reviewRepo.findByIdAndMemberId(reviewId, memberId);
        if (review == null) {
            review = reviewRepo.findById(reviewId).orElse(null); // 額外查詢看看是誰的評論
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
