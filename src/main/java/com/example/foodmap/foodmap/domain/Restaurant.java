package com.example.foodmap.foodmap.domain;

import com.example.foodmap.member.domain.Member;

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

    @Column(name = "created_by")
    private Integer createdBy; // 對應 member_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private Member createdByMember; // 使用 Member 類別來映射 createdBy 欄位

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
    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

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

    // 使用這個方法來取得上傳者的暱稱
    public String getUploaderNickname() {
        return createdByMember != null ? createdByMember.getMemberNickName() : null;
    }
}
