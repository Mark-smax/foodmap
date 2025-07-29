package com.example.foodmap.foodmap.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.example.foodmap.member.domain.Member;

@Entity
@Table(name = "restaurant_review")
public class RestaurantReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private String comment;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    // 多對一關聯，與 Member 資料表進行關聯
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;

    // 新增暱稱欄位，用來顯示評論者的暱稱
    @Transient
    private String memberNickName;

    // 建構子
    public RestaurantReview() {
    }

    // Getter / Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }

    // Getter / Setter for memberNickName
    public String getMemberNickName() {
        if (this.member != null) {
            return this.member.getMemberNickName(); // 確保可以從 Member 類別獲取暱稱
        }
        return memberNickName;
    }

    public void setMemberNickName(String memberNickName) {
        this.memberNickName = memberNickName;
    }
}
