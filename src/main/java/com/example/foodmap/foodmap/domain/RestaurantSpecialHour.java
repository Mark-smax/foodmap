package com.example.foodmap.foodmap.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "restaurant_special_hour",
       indexes = {
           @Index(name = "idx_special_restaurant_date", columnList = "restaurantId,specificDate")
       })
public class RestaurantSpecialHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long restaurantId;

    // 特別的某一天
    @Column(nullable = false)
    private LocalDate specificDate;

    // 是否整天公休（true 時忽略 open/close）
    @Column(nullable = false)
    private boolean closedAllDay = false;

    // 若非整天，則用單段時間（之後要支援多段再加子表）
    private LocalTime openTime;
    private LocalTime closeTime;

    // 備註（端午節店休、員工旅遊等）
    @Column(length = 255)
    private String note;

    public Long getId() { return id; }
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public LocalDate getSpecificDate() { return specificDate; }
    public void setSpecificDate(LocalDate specificDate) { this.specificDate = specificDate; }

    public boolean isClosedAllDay() { return closedAllDay; }
    public void setClosedAllDay(boolean closedAllDay) { this.closedAllDay = closedAllDay; }

    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }

    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
