package com.example.foodmap.model;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurant")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String county;
    private String address;
    private String phone;
    private Double rating;

    @Column(name = "category") // type 映射到資料庫欄位 category
    private String type;

    @Column(name = "keywords", columnDefinition = "nvarchar(max)") // 🔥 新增關鍵字欄位
    private String keywords;

    @Transient
    private String thumbnail;

    @Transient
    private Double avgRating;

    public Restaurant() {}

    public Restaurant(String name, String county, String address, String phone, Double rating, String type) {
        this.name = name;
        this.county = county;
        this.address = address;
        this.phone = phone;
        this.rating = rating;
        this.type = type;
    }

    // --- Getter/Setter ---

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
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

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
}
