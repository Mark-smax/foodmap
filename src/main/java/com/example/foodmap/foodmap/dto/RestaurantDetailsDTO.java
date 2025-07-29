package com.example.foodmap.foodmap.dto;

import java.util.List;
import com.example.foodmap.foodmap.domain.Restaurant;
import com.example.foodmap.foodmap.domain.RestaurantReview;

public class RestaurantDetailsDTO {

    private Restaurant restaurant;
    private List<String> photoBase64List;
    private List<RestaurantReview> reviews;
    private boolean isFavorite;
    private String uploaderNickname;  // 新增的欄位，用來儲存上傳者的暱稱

    // 無參數建構子
    public RestaurantDetailsDTO() {}

    // 帶參數的建構子，加入 uploaderNickname
    public RestaurantDetailsDTO(Restaurant restaurant,
                                 List<String> photoBase64List,
                                 List<RestaurantReview> reviews,
                                 boolean isFavorite,
                                 String uploaderNickname) {
        this.restaurant = restaurant;
        this.photoBase64List = photoBase64List;
        this.reviews = reviews;
        this.isFavorite = isFavorite;
        this.uploaderNickname = uploaderNickname;  // 設置上傳者暱稱
    }

    // Getter 和 Setter 方法
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

    // Getter 和 Setter 用來處理 uploaderNickname
    public String getUploaderNickname() {
        return uploaderNickname;
    }

    public void setUploaderNickname(String uploaderNickname) {
        this.uploaderNickname = uploaderNickname;
    }
}
