package com.example.foodmap.foodmap.dto;

public class RestaurantDto {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String county;
    private String type;
    private double avgRating;
    private String thumbnail;
    private boolean isFavorite;
    private String uploaderNickname;

    // ✅ 無參數建構子（必要）
    public RestaurantDto() {
    }

    public RestaurantDto(Long id, String name, String address, String phone, String county, String type,
                         double avgRating, String thumbnail, boolean isFavorite, String uploaderNickname) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.county = county;
        this.type = type;
        this.avgRating = avgRating;
        this.thumbnail = thumbnail;
        this.isFavorite = isFavorite;
        this.uploaderNickname = uploaderNickname;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
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
}
