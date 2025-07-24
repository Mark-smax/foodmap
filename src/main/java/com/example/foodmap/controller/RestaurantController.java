package com.example.foodmap.controller;

import com.example.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.model.Restaurant;
import com.example.foodmap.model.RestaurantPhoto;
import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.service.RestaurantService;
import com.example.foodmap.repository.RestaurantFavoriteRepository;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {

    private final RestaurantService service;
    private final RestaurantFavoriteRepository favoriteRepository;

    @Autowired
    public RestaurantController(RestaurantService service,
                                RestaurantFavoriteRepository favoriteRepository) {
        this.service = service;
        this.favoriteRepository = favoriteRepository;
    }

    @GetMapping
    public Page<Restaurant> search(...) { /* 原本程式不動領 */ }

    // 詳細頁 API
    @GetMapping("/{id}/details")
    public RestaurantDetailsDTO getRestaurantDetails(
        @PathVariable("id") Long restaurantId,
        @RequestParam(value = "memberId", required = false) Long memberId) {

        Restaurant restaurant = service.getRestaurantById(restaurantId);
        List<RestaurantPhoto> photos = service.getPhotosByRestaurantId(restaurantId);
        List<RestaurantReview> reviews = service.getReviewsByRestaurantId(restaurantId);

        boolean isFavorite = (memberId != null) &&
            favoriteRepository.existsByRestaurantIdAndMemberId(restaurantId, memberId);

        return new RestaurantDetailsDTO(restaurant, photos, reviews, isFavorite);
    }
}
