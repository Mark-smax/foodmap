package com.example.foodmap.service.impl;

import com.example.foodmap.model.Restaurant;
import com.example.foodmap.model.RestaurantPhoto;
import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.repository.RestaurantRepository;
import com.example.foodmap.repository.RestaurantPhotoRepository;
import com.example.foodmap.repository.RestaurantReviewRepository;
import com.example.foodmap.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantPhotoRepository restaurantPhotoRepository;

    @Autowired
    private RestaurantReviewRepository restaurantReviewRepository;

    @Override
    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到該餐廳"));
    }

    @Override
    public List<RestaurantPhoto> getPhotosByRestaurantId(Long restaurantId) {
        return restaurantPhotoRepository.findTop5ByRestaurantIdOrderByIdAsc(restaurantId);
    }

    @Override
    public List<RestaurantReview> getReviewsByRestaurantId(Long restaurantId) {
        return restaurantReviewRepository.findByRestaurantId(restaurantId);
    }
}
