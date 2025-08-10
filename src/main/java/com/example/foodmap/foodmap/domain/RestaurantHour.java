package com.example.foodmap.foodmap.domain;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "restaurant_hour")
public class RestaurantHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 餐廳ID（不綁 JPA 關聯，保持輕量）
    @Column(nullable = false)
    private Long restaurantId;

    // 週一=1 ... 週日=7（用 DayOfWeek 儲存最直觀）
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    // 是否整天公休（true 時忽略 open/close）
    @Column(nullable = false)
    private boolean closedAllDay = false;

    // 開/閉店時間，可為跨夜以外的單段（跨夜在 Step 2 再加）
    private LocalTime openTime;
    private LocalTime closeTime;

    public Long getId() { return id; }
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public boolean isClosedAllDay() { return closedAllDay; }
    public void setClosedAllDay(boolean closedAllDay) { this.closedAllDay = closedAllDay; }

    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }

    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }
}
