package com.example.foodmap.foodmap.domain;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.foodmap.foodmap.domain.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

	List<Restaurant> findByCounty(String county);

	Page<Restaurant> findByCountyAndRatingGreaterThanEqualAndTypeContainingIgnoreCase(String county, Double rating,
			String type, Pageable pageable);

	Page<Restaurant> findByCountyAndTypeContainingIgnoreCase(String county, String type, Pageable pageable);

	Page<Restaurant> findByTypeContainingIgnoreCase(String type, Pageable pageable);

	Page<Restaurant> findByCounty(String county, Pageable pageable);

	@Query("SELECT r FROM Restaurant r WHERE " + "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(r.type) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(r.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	Page<Restaurant> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
	
    Page<Restaurant> findByKeywordsContainingIgnoreCase(String keyword, Pageable pageable);

	Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);

	@Query("SELECT r FROM Restaurant r")
	List<Restaurant> findAllWithReviews();
}
