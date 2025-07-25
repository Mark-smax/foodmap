package com.example.foodmap.service;

import com.example.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.model.Restaurant;
import com.example.foodmap.model.RestaurantFavorite;
import com.example.foodmap.model.RestaurantPhoto;
import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantPhotoRepository photoRepository;
    private final RestaurantReviewRepository reviewRepository;
    private final RestaurantFavoriteRepository favoriteRepository;

    public RestaurantService(RestaurantRepository restaurantRepository,
                             RestaurantPhotoRepository photoRepository,
                             RestaurantReviewRepository reviewRepository,
                             RestaurantFavoriteRepository favoriteRepository) {
        this.restaurantRepository = restaurantRepository;
        this.photoRepository = photoRepository;
        this.reviewRepository = reviewRepository;
        this.favoriteRepository = favoriteRepository;
    }

    public Page<Restaurant> searchRestaurants(String county, Double minRating, String type, Pageable pageable) {
        boolean hasCounty = county != null && !county.trim().isEmpty();
        boolean hasType = type != null && !type.trim().isEmpty();

        Page<Restaurant> page;

        if (hasCounty && hasType) {
            page = restaurantRepository.findByCountyAndRatingGreaterThanEqualAndTypeContainingIgnoreCase(
                    county, minRating, type, pageable);
        } else if (hasCounty) {
            page = restaurantRepository.findByCounty(county, pageable);
        } else if (hasType) {
            page = restaurantRepository.findByTypeContainingIgnoreCase(type, pageable);
        } else {
            return Page.empty(pageable);
        }

        // 加入縮圖處理
        page.forEach(this::setRandomThumbnail);

        return page;
    }

    private void setRandomThumbnail(Restaurant restaurant) {
        List<RestaurantPhoto> photos = photoRepository.findTop5ByRestaurantIdOrderByIdAsc(restaurant.getId());
        if (!photos.isEmpty()) {
            int randomIndex = new Random().nextInt(photos.size());
            String randomBase64 = Base64.getEncoder().encodeToString(photos.get(randomIndex).getImage());
            restaurant.setThumbnail(randomBase64);
        }
    }

    public Page<Restaurant> searchByCountyAndType(String county, String type, Pageable pageable) {
        if (county == null || county.trim().isEmpty() || type == null || type.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        Page<Restaurant> page = restaurantRepository.findByCountyAndTypeContainingIgnoreCase(county, type, pageable);
        page.forEach(this::setRandomThumbnail);
        return page;
    }

    public Page<Restaurant> searchByKeyword(String keyword, Pageable pageable) {
        Page<Restaurant> page = restaurantRepository.searchByKeyword(keyword, pageable);
        page.forEach(this::setRandomThumbnail);
        return page;
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
    
    @Transactional
    public void saveReview(RestaurantReview review) {
        reviewRepository.save(review);
    }

    @Transactional
    public void toggleFavorite(Long restaurantId, Long memberId) {
        boolean exists = favoriteRepository.existsByRestaurantIdAndMemberId(restaurantId, memberId);
        if (exists) {
            favoriteRepository.deleteByRestaurantIdAndMemberId(restaurantId, memberId);
        } else {
            RestaurantFavorite fav = new RestaurantFavorite();
            fav.setRestaurantId(restaurantId);
            fav.setMemberId(memberId);
            favoriteRepository.save(fav);
        }
    }

    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("餐廳不存在"));
    }

    public List<RestaurantPhoto> getPhotosByRestaurantId(Long id) {
        return photoRepository.findTop5ByRestaurantIdOrderByIdAsc(id);
    }

    public List<RestaurantReview> getReviewsByRestaurantId(Long id) {
        return reviewRepository.findByRestaurantIdOrderByCreatedTimeDesc(id);
    }

    public RestaurantDetailsDTO getRestaurantDetails(Long restaurantId, Long memberId) {
        Restaurant restaurant = getRestaurantById(restaurantId);

        List<String> base64Photos = getPhotosByRestaurantId(restaurantId).stream()
            .map(photo -> Base64.getEncoder().encodeToString(photo.getImage()))
            .collect(Collectors.toList());

        List<RestaurantReview> reviews = getReviewsByRestaurantId(restaurantId);

        boolean isFavorite = (memberId != null) &&
            favoriteRepository.existsByRestaurantIdAndMemberId(restaurantId, memberId);

        return new RestaurantDetailsDTO(restaurant, base64Photos, reviews, isFavorite);
    }
    
}
