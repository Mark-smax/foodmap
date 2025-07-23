package com.example.foodmap.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RestaurantFavoriteRepositoryImpl implements RestaurantFavoriteRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean isFavorite(Long restaurantId, Long memberId) {
        String sql = "SELECT COUNT(*) FROM restaurant_favorite WHERE restaurant_id = ? AND member_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{restaurantId, memberId}, Integer.class);
        return count != null && count > 0;
    }
}
