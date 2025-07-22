package com.example.foodmap.dao;

import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.utils.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantReviewDao {

    public void insertReview(RestaurantReview review) {
        String sql = "INSERT INTO restaurant_review (restaurant_id, member_id, rating, comment) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, review.getRestaurantId());
            stmt.setLong(2, review.getMemberId());
            stmt.setInt(3, review.getRating());
            stmt.setString(4, review.getComment());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<RestaurantReview> getReviewsByRestaurantId(Long restaurantId) {
        List<RestaurantReview> list = new ArrayList<>();
        String sql = "SELECT * FROM restaurant_review WHERE restaurant_id = ? ORDER BY created_time DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, restaurantId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                RestaurantReview review = new RestaurantReview();
                review.setId(rs.getLong("id"));
                review.setRestaurantId(rs.getLong("restaurant_id"));
                review.setMemberId(rs.getLong("member_id"));
                review.setRating(rs.getInt("rating"));
                review.setComment(rs.getString("comment"));
                review.setCreatedTime(rs.getTimestamp("created_time").toLocalDateTime());
                list.add(review);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
