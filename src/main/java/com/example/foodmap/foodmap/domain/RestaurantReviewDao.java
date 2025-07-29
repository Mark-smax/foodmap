package com.example.foodmap.foodmap.domain;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RestaurantReviewDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insertReview(RestaurantReview review) {
        String sql = "INSERT INTO restaurant_review (restaurant_id, member_id, rating, comment) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                review.getRestaurantId(),
                review.getMemberId(),
                review.getRating(),
                review.getComment());
    }

    public List<RestaurantReview> getReviewsByRestaurantId(Long restaurantId) {
        String sql = "SELECT * FROM restaurant_review WHERE restaurant_id = ? ORDER BY created_time DESC";

        return jdbcTemplate.query(sql,
                new Object[]{restaurantId},
                new BeanPropertyRowMapper<>(RestaurantReview.class));
    }
}
