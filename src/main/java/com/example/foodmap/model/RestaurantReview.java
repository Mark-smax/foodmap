package com.example.foodmap.model;

import java.time.LocalDateTime;

public class RestaurantReview {
    private Long id;
    private Long restaurantId;
    private Long memberId;
    private int rating;
    private String comment;
    private LocalDateTime createdTime;

    // Constructors
    public RestaurantReview() {}

    public RestaurantReview(Long restaurantId, Long memberId, int rating, String comment) {
        this.restaurantId = restaurantId;
        this.memberId = memberId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters & Setters
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
}
