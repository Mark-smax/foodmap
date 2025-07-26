package com.example.foodmap.dto;

public class RestaurantDto {
    public Long id;
    public String name;
    public String address;
    public String phone;
    public String county;
    public String type;
    public double avgRating;  // ← 改成 public
    public String thumbnail;
    public boolean isFavorite; // ← 改成 public

    public RestaurantDto(Long id, String name, String address, String phone, String county, String type,
                         double avgRating, String thumbnail, boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.county = county;
        this.type = type;
        this.avgRating = avgRating;
        this.thumbnail = thumbnail;
        this.isFavorite = isFavorite;
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
}
