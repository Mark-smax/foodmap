package com.example.foodmap.foodmap.dto;

public class PhotoDto {
    private Long id;
    private String url; // e.g. /api/restaurants/photos/123

    public PhotoDto() {}
    public PhotoDto(Long id, String url) {
        this.id = id;
        this.url = url;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
