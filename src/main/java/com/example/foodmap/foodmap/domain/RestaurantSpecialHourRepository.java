package com.example.foodmap.foodmap.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RestaurantSpecialHourRepository extends JpaRepository<RestaurantSpecialHour, Long> {
    Optional<RestaurantSpecialHour> findByRestaurantIdAndSpecificDate(Long restaurantId, LocalDate specificDate);
    List<RestaurantSpecialHour> findByRestaurantIdAndSpecificDateBetweenOrderBySpecificDateAsc(
            Long restaurantId, LocalDate from, LocalDate to);
    void deleteByRestaurantId(Long restaurantId);
}
