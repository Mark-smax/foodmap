package com.example.foodmap.foodmap.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.foodmap.member.domain.MemberRepository;

@Service
public class RestaurantReviewService {

    private final RestaurantReviewRepository reviewRepo;
    private final MemberRepository memberRepository;

    public RestaurantReviewService(RestaurantReviewRepository reviewRepo,
                                   MemberRepository memberRepository) {
        this.reviewRepo = reviewRepo;
        this.memberRepository = memberRepository;
    }

    /**
     * 根據餐廳 ID 取得所有評論（按時間排序），並補上評論者暱稱
     */
    public List<RestaurantReview> getReviewsByRestaurantId(Long restaurantId) {
        List<RestaurantReview> reviews =
                reviewRepo.findByRestaurantIdOrderByCreatedTimeDesc(restaurantId);

        // 為每個評論補上暱稱（memberId 是 Integer）
        for (RestaurantReview review : reviews) {
            Integer memberId = review.getMemberId();  // Entity 裡是 Integer
            if (memberId != null) {
                memberRepository.findById(memberId).ifPresent(m ->
                        review.setMemberNickName(m.getMemberNickName()));
            }
        }
        return reviews;
    }

    /** 新增一筆評論 */
    @Transactional
    public RestaurantReview insertReview(RestaurantReview review) {
        return reviewRepo.save(review);
    }

    /** 設置評論為隱藏或顯示（管理員操作） */
    @Transactional
    public void setReviewHidden(Long reviewId, boolean hidden) {
        reviewRepo.findById(reviewId).ifPresent(rv -> rv.setIsHidden(hidden));
        // 進到這個方法時在同一個交易中，JPA 會自動做 dirty-check flush
    }

    /**
     * 刪除自己的評論
     * 注意：Repository 方法簽名是 (Long, Integer)，這裡把 Long 轉成 Integer 再查
     */
    @Transactional
    public boolean deleteReviewByIdAndMemberId(Long reviewId, Long memberId) {
        Integer mid = (memberId == null) ? null : memberId.intValue();
        RestaurantReview review = reviewRepo.findByIdAndMemberId(reviewId, mid);
        if (review != null) {
            reviewRepo.delete(review);
            return true;
        }
        return false;
    }

    /**
     * 修改自己的評論
     * 同樣轉型成 Integer 再查
     */
    @Transactional
    public boolean updateReview(Long reviewId, Long memberId, int rating, String comment) {
        Integer mid = (memberId == null) ? null : memberId.intValue();
        RestaurantReview review = reviewRepo.findByIdAndMemberId(reviewId, mid);
        if (review != null) {
            review.setRating(rating);
            review.setComment(comment);
            return true;
        }
        return false;
    }

    /** 依 ID 直接刪除（管理用途） */
    @Transactional
    public boolean deleteReviewById(Long reviewId) {
        Optional<RestaurantReview> reviewOptional = reviewRepo.findById(reviewId);
        if (reviewOptional.isPresent()) {
            reviewRepo.deleteById(reviewId);
            return true;
        }
        return false;
    }
}
