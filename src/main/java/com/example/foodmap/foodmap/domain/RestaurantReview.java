package com.example.foodmap.foodmap.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.example.foodmap.member.domain.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "restaurant_review")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})   // 忽略 Hibernate 代理屬性
@JsonInclude(JsonInclude.Include.NON_NULL)                      // 只輸出非 null 欄位
public class RestaurantReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    // 與資料庫 INT 對齊
    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private String comment;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    /**
     * 多對一關聯（LAZY）
     * 重點：避免把 Hibernate 代理序列化到 JSON，所以加 @JsonIgnore
     * 真正要回前端的資訊（例如暱稱）用下方的 transient 欄位輸出。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    @JsonIgnore
    private Member member;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;

    /**
     * 顯示評論者暱稱（不入庫）
     * 建議在 Service 組 DTO 時主動塞進來；
     * 若未手動塞、且當前 session 仍開啟，getter 會從 LAZY member 取暱稱。
     */
    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
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
        // 若 Service 先 set 了就直接回；否則在 session 開啟下可從 LAZY member 取值
        if (this.memberNickName != null) return this.memberNickName;
        if (this.member != null) {
            try { return this.member.getMemberNickName(); } catch (Exception ignore) {}
        }
        return null;
    }
    public void setMemberNickName(String memberNickName) { this.memberNickName = memberNickName; }
}
