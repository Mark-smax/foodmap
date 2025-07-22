package com.example.foodmap.repository;

import com.example.foodmap.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

	// 傳統依縣市查詢，回傳 List（可留著，也可用分頁方法代替）
	List<Restaurant> findByCounty(String county);

	// 多條件查詢（縣市 + 評分以上 + 類型包含，不區分大小寫，分頁）
	Page<Restaurant> findByCountyAndRatingGreaterThanEqualAndTypeContainingIgnoreCase(String county, Double rating,
			String type, Pageable pageable);

	// 新增：縣市 + 類別（不分大小寫）查詢，無 rating 條件
	Page<Restaurant> findByCountyAndTypeContainingIgnoreCase(String county, String type, Pageable pageable);

	// 單純用縣市查詢，分頁版
	Page<Restaurant> findByCounty(String county, Pageable pageable);

	// 模糊關鍵字查詢（搜尋 name、address、type、county）
	@Query("SELECT r FROM Restaurant r WHERE " + "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(r.type) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(r.county) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	Page<Restaurant> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
