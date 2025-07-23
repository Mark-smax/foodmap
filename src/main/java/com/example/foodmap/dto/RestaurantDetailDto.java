package com.example.foodmap.dto;

import java.util.List;

public class RestaurantDetailDto {
    public Long id;
    public String name;
    public String phone;
    public String address;
    public List<String> images; // base64 image
    public double avgRating;
    public List<ReviewDto> reviews;
    public Boolean isFavorite;

    public static class ReviewDto {
        public String memberNickname;
        public int stars;
        public String comment;
        public String createdTime;
    }
}
