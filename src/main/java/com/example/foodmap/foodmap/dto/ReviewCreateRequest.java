package com.example.foodmap.foodmap.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 新增評論用的請求 DTO
 * - rating 必填 1~5
 * - comment 可空（後端會自行 trim）
 * - memberId 可選：若你從 session/token 取，不需要由前端傳
 *
 * 注意：隱藏(hidden) 欄位不在此 DTO 中，請透過「隱藏/顯示評論」的專用 API 操作。
 */
public class ReviewCreateRequest {

    /**
     * 評分 1~5，必填
     */
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    /**
     * 評論內容，可空；後端會做 trim 與長度限制
     */
    private String comment;

    /**
     * 發文者（可選）；通常從 session/token 取得，不建議由前端決定
     */
    private Long memberId;

    public ReviewCreateRequest() {}

    public Integer getRating() {
        return rating;
    }
    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getMemberId() {
        return memberId;
    }
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
}
