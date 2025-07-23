package com.example.foodmap.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RestaurantPhotoRepositoryImpl implements RestaurantPhotoRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<byte[]> findTop5ImagesByRestaurantId(Long restaurantId) {
        String sql = "SELECT TOP 5 image FROM restaurant_photo WHERE restaurant_id = ?";
        return jdbcTemplate.query(sql, new Object[]{restaurantId},
            (rs, rowNum) -> rs.getBytes("image"));
    }
}
