package com.example.foodmap.dto;

import com.example.foodmap.model.Restaurant;
import com.example.foodmap.model.RestaurantPhoto;
import com.example.foodmap.model.RestaurantReview;

import java.util.List;

public class RestaurantDetailsDTO {
    private Restaurant restaurant;
    private List<RestaurantPhoto> photos;
    private List<RestaurantReview> reviews;
    private boolean isFavorite;

    public RestaurantDetailsDTO(Restaurant restaurant, List<RestaurantPhoto> photos, List<RestaurantReview> reviews, boolean isFavorite) {
        this.restaurant = restaurant;
        this.photos = photos;
        this.reviews = reviews;
        this.isFavorite = isFavorite;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public List<RestaurantPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<RestaurantPhoto> photos) {
        this.photos = photos;
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
