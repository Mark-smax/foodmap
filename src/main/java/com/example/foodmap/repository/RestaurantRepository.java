package com.example.foodmap.repository;

import com.example.foodmap.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByCounty(String county);

    Page<Restaurant> findByCountyAndRatingGreaterThanEqualAndTypeContainingIgnoreCase(String county, Double rating,
            String type, Pageable pageable);

    Page<Restaurant> findByCountyAndTypeContainingIgnoreCase(String county, String type, Pageable pageable);

    Page<Restaurant> findByTypeContainingIgnoreCase(String type, Pageable pageable);

    Page<Restaurant> findByCounty(String county, Pageable pageable);

    @Query("SELECT r FROM Restaurant r WHERE " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.type) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.county) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Restaurant> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
