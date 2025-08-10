package com.example.foodmap.foodmap.dto;

import java.time.DayOfWeek;
import java.util.LinkedHashMap;
import java.util.List;

import com.example.foodmap.foodmap.domain.Restaurant;
import com.example.foodmap.foodmap.domain.RestaurantReview;

public class RestaurantDetailsDTO {

    // 既有欄位
    private Restaurant restaurant;
    private List<String> photoBase64List;
    private List<RestaurantReview> reviews;
    private boolean isFavorite;
    private String uploaderNickname; // 上傳者暱稱

    // 新增：營業時間 / 今日狀態
    // 週一→週日固定順序的時段表；每天可能有多個時段（e.g., 10:00–14:00 / 17:00–21:00）
    private LinkedHashMap<DayOfWeek, List<String>> weeklyHours;
    private Boolean openNow;        // 此刻是否營業
    private String todayRange;      // 今天時段字串（多段以 " / " 連接；公休=公休）
    private String todayStatusText; // 顯示用：營業中 / 今日公休 / 已打烊 / 未設定
    private String todayLabel;      // 週幾（中文顯示：週一、週二...）

    // 無參數建構子
    public RestaurantDetailsDTO() {}

    // 既有的五參數建構子（保留以相容既有呼叫）
    public RestaurantDetailsDTO(Restaurant restaurant,
                                List<String> photoBase64List,
                                List<RestaurantReview> reviews,
                                boolean isFavorite,
                                String uploaderNickname) {
        this.restaurant = restaurant;
        this.photoBase64List = photoBase64List;
        this.reviews = reviews;
        this.isFavorite = isFavorite;
        this.uploaderNickname = uploaderNickname;
    }

    // 新增：全欄位建構子（可選用）
    public RestaurantDetailsDTO(Restaurant restaurant,
                                List<String> photoBase64List,
                                List<RestaurantReview> reviews,
                                boolean isFavorite,
                                String uploaderNickname,
                                LinkedHashMap<DayOfWeek, List<String>> weeklyHours,
                                Boolean openNow,
                                String todayRange,
                                String todayStatusText,
                                String todayLabel) {
        this.restaurant = restaurant;
        this.photoBase64List = photoBase64List;
        this.reviews = reviews;
        this.isFavorite = isFavorite;
        this.uploaderNickname = uploaderNickname;
        this.weeklyHours = weeklyHours;
        this.openNow = openNow;
        this.todayRange = todayRange;
        this.todayStatusText = todayStatusText;
        this.todayLabel = todayLabel;
    }

    // Getters / Setters
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

    public String getUploaderNickname() {
        return uploaderNickname;
    }

    public void setUploaderNickname(String uploaderNickname) {
        this.uploaderNickname = uploaderNickname;
    }

    public LinkedHashMap<DayOfWeek, List<String>> getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(LinkedHashMap<DayOfWeek, List<String>> weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public Boolean getOpenNow() {
        return openNow;
    }

    public void setOpenNow(Boolean openNow) {
        this.openNow = openNow;
    }

    public String getTodayRange() {
        return todayRange;
    }

    public void setTodayRange(String todayRange) {
        this.todayRange = todayRange;
    }

    public String getTodayStatusText() {
        return todayStatusText;
    }

    public void setTodayStatusText(String todayStatusText) {
        this.todayStatusText = todayStatusText;
    }

    public String getTodayLabel() {
        return todayLabel;
    }

    public void setTodayLabel(String todayLabel) {
        this.todayLabel = todayLabel;
    }
}
