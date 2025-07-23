package com.example.foodmap.model;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurant_photo")
public class RestaurantPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Lob
    @Column(name = "image")
    private byte[] image;

    public RestaurantPhoto() {}

    public RestaurantPhoto(Long restaurantId, byte[] image) {
        this.restaurantId = restaurantId;
        this.image = image;
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
