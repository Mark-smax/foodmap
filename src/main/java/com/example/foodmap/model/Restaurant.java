package com.example.foodmap.model;

import java.util.List;

public class Restaurant {
    private String name;
    private String imgUrl;
    private double rating;
    private String address;
    private String phone;
    private List<String> tags;

    public Restaurant(String name, String imgUrl, double rating, String address, String phone, List<String> tags) {
        this.name = name;
        this.imgUrl = imgUrl;
        this.rating = rating;
        this.address = address;
        this.phone = phone;
        this.tags = tags;
    }

    public String getName() { return name; }
    public String getImgUrl() { return imgUrl; }
    public double getRating() { return rating; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public List<String> getTags() { return tags; }
}
