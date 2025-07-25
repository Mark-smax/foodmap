package com.example.foodmap.dto;

import java.time.LocalDateTime;

public class ReviewWithNicknameDTO {
    private int rating;
    private String comment;
    private LocalDateTime createdTime;
    private String memberNickname;

    public ReviewWithNicknameDTO(int rating, String comment, LocalDateTime createdTime, String memberNickname) {
        this.rating = rating;
        this.comment = comment;
        this.createdTime = createdTime;
        this.memberNickname = memberNickname;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public String getMemberNickname() {
        return memberNickname;
    }
}
