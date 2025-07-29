package com.example.foodmap.foodmap.dto;

import java.util.List;

import com.example.foodmap.foodmap.domain.Restaurant;
import com.example.foodmap.foodmap.domain.RestaurantReview;

public class RestaurantDetailsDTO {

    private Restaurant restaurant;
    private List<String> photoBase64List;
    private List<RestaurantReview> reviews;
    private boolean isFavorite;

    public RestaurantDetailsDTO() {}

    public RestaurantDetailsDTO(Restaurant restaurant,
                                 List<String> photoBase64List,
                                 List<RestaurantReview> reviews,
                                 boolean isFavorite) {
        this.restaurant = restaurant;
        this.photoBase64List = photoBase64List;
        this.reviews = reviews;
        this.isFavorite = isFavorite;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public List<String> getPhotoBase64List() {
        return photoBase64List;
    }

    public void setPhotoBase64List(List<String> photoBase64List) {
        this.photoBase64List = photoBase64List;
    }

    public List<RestaurantReview> getReviews() {
        return reviews;
    }

    public void setReviews(List<RestaurantReview> reviews) {
        this.reviews = reviews;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
