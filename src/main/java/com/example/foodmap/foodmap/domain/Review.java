package com.example.foodmap.foodmap.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.example.foodmap.member.domain.Member;

@Entity
@Table(name = "restaurant_rating") // 對應資料表
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ratingId;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false) // 外鍵連接到餐廳
    private Restaurant restaurant; // 關聯餐廳

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false) // 外鍵連接到會員
    private Member member; // 關聯會員

    private int stars;
    private String comment;
    private LocalDateTime ratedTime;

    // Getters & Setters
    public Long getRatingId() {
        return ratingId;
    }

    public void setRatingId(Long ratingId) {
        this.ratingId = ratingId;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getRatedTime() {
        return ratedTime;
    }

    public void setRatedTime(LocalDateTime ratedTime) {
        this.ratedTime = ratedTime;
    }
}
