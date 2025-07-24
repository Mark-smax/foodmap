package com.example.foodmap.service;

import com.example.foodmap.model.Restaurant;
import com.example.foodmap.model.RestaurantPhoto;
import com.example.foodmap.model.Review;
import com.example.foodmap.model.RestaurantFavorite;
import com.example.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.repository.RestaurantRepository;
import com.example.foodmap.repository.RestaurantPhotoRepository;
import com.example.foodmap.repository.ReviewRepository;
import com.example.foodmap.repository.RestaurantFavoriteRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantPhotoRepository restaurantPhotoRepository;
    private final ReviewRepository reviewRepository;
    private final RestaurantFavoriteRepository restaurantFavoriteRepository;

    public RestaurantService(RestaurantRepository restaurantRepository,
                             RestaurantPhotoRepository restaurantPhotoRepository,
                             ReviewRepository reviewRepository,
                             RestaurantFavoriteRepository restaurantFavoriteRepository) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantPhotoRepository = restaurantPhotoRepository;
        this.reviewRepository = reviewRepository;
        this.restaurantFavoriteRepository = restaurantFavoriteRepository;
    }

    public Page<Restaurant> searchRestaurants(String county, Double minRating, String type, Pageable pageable) {
        if (type == null || type.trim().isEmpty()) {
            return restaurantRepository.findByCounty(county, pageable);
        } else {
            return restaurantRepository.findByCountyAndRatingGreaterThanEqualAndTypeContainingIgnoreCase(
                    county, minRating, type, pageable);
        }
    }

    public Page<Restaurant> searchByCountyAndType(String county, String type, Pageable pageable) {
        if (county == null || county.trim().isEmpty() || type == null || type.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        return restaurantRepository.findByCountyAndTypeContainingIgnoreCase(county, type, pageable);
    }

    public Page<Restaurant> searchByKeyword(String keyword, Pageable pageable) {
        return restaurantRepository.searchByKeyword(keyword, pageable);
    }

    @Transactional
    public Restaurant createRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public Restaurant updateRestaurant(Long id, Restaurant updated) {
        Optional<Restaurant> optional = restaurantRepository.findById(id);
        if (optional.isPresent()) {
            Restaurant existing = optional.get();
            existing.setName(updated.getName());
            existing.setCounty(updated.getCounty());
            existing.setAddress(updated.getAddress());
            existing.setPhone(updated.getPhone());
            existing.setRating(updated.getRating());
            existing.setType(updated.getType());
            return restaurantRepository.save(existing);
        } else {
            return null;
        }
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }

    public RestaurantDetailsDTO getRestaurantDetails(Long restaurantId, Long memberId) {
        Optional<Restaurant> optionalRestaurant = restaurantRepository.findById(restaurantId);
        if (optionalRestaurant.isEmpty()) {
            return null;
        }
        Restaurant restaurant = optionalRestaurant.get();
        List<RestaurantPhoto> photos = restaurantPhotoRepository.findTop5ByRestaurantIdOrderByIdAsc(restaurantId);
        List<Review> reviews = reviewRepository.findByRestaurantId(restaurantId);
        boolean isFavorite = restaurantFavoriteRepository.existsByRestaurantIdAndMemberId(restaurantId, memberId);
        return new RestaurantDetailsDTO(restaurant, photos, reviews, isFavorite);
    }
}
