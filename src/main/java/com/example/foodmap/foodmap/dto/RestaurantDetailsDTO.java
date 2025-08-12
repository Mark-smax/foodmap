package com.example.foodmap.foodmap.dto;

import java.time.DayOfWeek;
import java.util.LinkedHashMap;
import java.util.List;

import com.example.foodmap.foodmap.domain.Restaurant;

/**
 * Step 3：移除 photoBase64List，統一改用 photos(URL)。
 * reviews 已在 Step 2 換成 ReviewDto。
 */
public class RestaurantDetailsDTO {

    // 主要資料
    private Restaurant restaurant;
    private List<ReviewDto> reviews;      // Step 2：DTO
    private boolean isFavorite;
    private String uploaderNickname;

    // 照片 (URL)
    private List<PhotoDto> photos;

    // 營業時間 / 今日狀態
    private LinkedHashMap<DayOfWeek, List<String>> weeklyHours; // 週一→週日固定順序
    private Boolean openNow;
    private String todayRange;
    private String todayStatusText;
    private String todayLabel;

    public RestaurantDetailsDTO() {}

    // 簡化建構子（無 photoBase64List）
    public RestaurantDetailsDTO(Restaurant restaurant,
                                List<ReviewDto> reviews,
                                boolean isFavorite,
                                String uploaderNickname) {
        this.restaurant = restaurant;
        this.reviews = reviews;
        this.isFavorite = isFavorite;
        this.uploaderNickname = uploaderNickname;
    }

    // 全欄位建構子
    public RestaurantDetailsDTO(Restaurant restaurant,
                                List<ReviewDto> reviews,
                                boolean isFavorite,
                                String uploaderNickname,
                                List<PhotoDto> photos,
                                LinkedHashMap<DayOfWeek, List<String>> weeklyHours,
                                Boolean openNow,
                                String todayRange,
                                String todayStatusText,
                                String todayLabel) {
        this.restaurant = restaurant;
        this.reviews = reviews;
        this.isFavorite = isFavorite;
        this.uploaderNickname = uploaderNickname;
        this.photos = photos;
        this.weeklyHours = weeklyHours;
        this.openNow = openNow;
        this.todayRange = todayRange;
        this.todayStatusText = todayStatusText;
        this.todayLabel = todayLabel;
    }

    // Getters / Setters
    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }

    public List<ReviewDto> getReviews() { return reviews; }
    public void setReviews(List<ReviewDto> reviews) { this.reviews = reviews; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getUploaderNickname() { return uploaderNickname; }
    public void setUploaderNickname(String uploaderNickname) { this.uploaderNickname = uploaderNickname; }

    public List<PhotoDto> getPhotos() { return photos; }
    public void setPhotos(List<PhotoDto> photos) { this.photos = photos; }

    public LinkedHashMap<DayOfWeek, List<String>> getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(LinkedHashMap<DayOfWeek, List<String>> weeklyHours) { this.weeklyHours = weeklyHours; }

    public Boolean getOpenNow() { return openNow; }
    public void setOpenNow(Boolean openNow) { this.openNow = openNow; }

    public String getTodayRange() { return todayRange; }
    public void setTodayRange(String todayRange) { this.todayRange = todayRange; }

    public String getTodayStatusText() { return todayStatusText; }
    public void setTodayStatusText(String todayStatusText) { this.todayStatusText = todayStatusText; }

    public String getTodayLabel() { return todayLabel; }
    public void setTodayLabel(String todayLabel) { this.todayLabel = todayLabel; }
}
