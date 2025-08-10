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

    // ✅ 改為 Integer，與 SQL Server INT 對齊
    @Column(name = "member_id", nullable = false)
    private Integer memberId;

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

    // 顯示評論者暱稱（不入庫）
    @Transient
    private String memberNickName;

    public RestaurantReview() {}

    @PrePersist
    protected void onCreate() {
        if (this.createdTime == null) {
            this.createdTime = LocalDateTime.now();
        }
        if (this.isHidden == null) {
            this.isHidden = false;
        }
    }

    // ===== Getter / Setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public Boolean getIsHidden() { return isHidden; }
    public void setIsHidden(Boolean isHidden) { this.isHidden = isHidden; }

    public String getMemberNickName() {
        if (this.member != null) {
            return this.member.getMemberNickName();
        }
        return memberNickName;
    }
    public void setMemberNickName(String memberNickName) { this.memberNickName = memberNickName; }
}
