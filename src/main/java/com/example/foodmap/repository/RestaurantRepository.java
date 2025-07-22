package com.example.foodmap.repository;

import com.example.foodmap.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // 傳統依縣市查詢，回傳 List（可留著，也可用分頁方法代替）
    List<Restaurant> findByCounty(String county);

    // 多條件查詢（縣市 + 評分以上 + 類型包含，不區分大小寫，分頁）
    Page<Restaurant> findByCountyAndRatingGreaterThanEqualAndTypeContainingIgnoreCase(
        String county, Double rating, String type, Pageable pageable);

    // 新增：單純用縣市查詢，分頁版（方便你做寬鬆查詢測試）
    Page<Restaurant> findByCounty(String county, Pageable pageable);

}
