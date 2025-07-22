package com.example.foodmap.repository;

import com.example.foodmap.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByCounty(String county);

    // 多條件查詢（自動由 Spring Data JPA 解析命名規則產生 SQL）
    Page<Restaurant> findByCountyAndRatingGreaterThanEqualAndTypeContainingIgnoreCase(
        String county, Double rating, String type, Pageable pageable);
}
