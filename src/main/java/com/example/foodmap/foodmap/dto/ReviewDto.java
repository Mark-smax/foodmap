package com.example.foodmap.foodmap.dto;

import java.time.LocalDateTime;

public class ReviewDto {
    private Long id;
    private Long memberId;
    private String memberNickname;
    private int rating;
    private String comment;
    private LocalDateTime createdTime; // 前端再格式化
    private boolean hidden;

    public ReviewDto() {}

    public ReviewDto(Long id,
                     Long memberId,
                     String memberNickname,
                     int rating,
                     String comment,
                     LocalDateTime createdTime,
                     boolean hidden) {
        this.id = id;
        this.memberId = memberId;
        this.memberNickname = memberNickname;
        this.rating = rating;
        this.comment = comment;
        this.createdTime = createdTime;
        this.hidden = hidden;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getMemberNickname() { return memberNickname; }
    public void setMemberNickname(String memberNickname) { this.memberNickname = memberNickname; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
}
