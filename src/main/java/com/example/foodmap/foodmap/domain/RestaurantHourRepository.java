package com.example.foodmap.foodmap.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.DayOfWeek;
import java.util.List;

public interface RestaurantHourRepository extends JpaRepository<RestaurantHour, Long> {
    List<RestaurantHour> findByRestaurantIdAndDayOfWeekOrderByIdAsc(Long restaurantId, DayOfWeek dayOfWeek);
    List<RestaurantHour> findByRestaurantIdOrderByDayOfWeekAsc(Long restaurantId);
    void deleteByRestaurantId(Long restaurantId);
}
