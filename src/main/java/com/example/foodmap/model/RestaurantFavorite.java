package com.example.foodmap.model;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurant_favorite")
public class RestaurantFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    // 建構子、Getter/Setter
    public RestaurantFavorite() {}

    public RestaurantFavorite(Long restaurantId, Long memberId) {
        this.restaurantId = restaurantId;
        this.memberId = memberId;
    }

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
}
