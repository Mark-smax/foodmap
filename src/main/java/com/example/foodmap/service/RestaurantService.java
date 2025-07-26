package com.example.foodmap.service;

import com.example.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.dto.RestaurantDto;
import com.example.foodmap.model.Restaurant;
import com.example.foodmap.model.RestaurantFavorite;
import com.example.foodmap.model.RestaurantPhoto;
import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

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

    public Page<RestaurantDto> searchRestaurants(String county, Double minRating, String type, Pageable pageable, Long memberId) {
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

        return convertToDtoPage(page, memberId);
    }

    public Page<RestaurantDto> searchByCountyAndType(String county, String type, Pageable pageable, Long memberId) {
        Page<Restaurant> page = restaurantRepository.findByCountyAndTypeContainingIgnoreCase(county, type, pageable);
        return convertToDtoPage(page, memberId);
    }

    public Page<RestaurantDto> searchByKeyword(String keyword, Pageable pageable, Long memberId) {
        Page<Restaurant> page = restaurantRepository.searchByKeyword(keyword, pageable);
        return convertToDtoPage(page, memberId);
    }

    private Page<RestaurantDto> convertToDtoPage(Page<Restaurant> page, Long memberId) {
        List<RestaurantDto> dtoList = page.getContent().stream().map(r -> {
            setAverageRating(r);
            String thumbnail = getRandomThumbnail(r.getId());
            boolean isFav = memberId != null && favoriteRepository.existsByRestaurantIdAndMemberId(r.getId(), memberId);
            return new RestaurantDto(r.getId(), r.getName(), r.getAddress(), r.getPhone(), r.getCounty(), r.getType(), r.getAvgRating(), thumbnail, isFav);
        }).collect(Collectors.toList());

        // ⭐ 收藏優先排序，再依星等高低
        dtoList.sort((a, b) -> {
            if (a.isFavorite != b.isFavorite) {
                return Boolean.compare(b.isFavorite, a.isFavorite); // true 優先
            }
            return Double.compare(b.avgRating, a.avgRating); // 星等高優先
        });

        return new PageImpl<>(dtoList, page.getPageable(), page.getTotalElements());
    }

    private String getRandomThumbnail(Long restaurantId) {
        List<RestaurantPhoto> photos = photoRepository.findTop5ByRestaurantIdOrderByIdAsc(restaurantId);
        if (!photos.isEmpty()) {
            int randomIndex = new Random().nextInt(photos.size());
            return Base64.getEncoder().encodeToString(photos.get(randomIndex).getImage());
        }
        return null;
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
        setAverageRating(restaurant);

        List<String> base64Photos = getPhotosByRestaurantId(restaurantId).stream()
            .map(photo -> Base64.getEncoder().encodeToString(photo.getImage()))
            .collect(Collectors.toList());

        List<RestaurantReview> reviews = getReviewsByRestaurantId(restaurantId);

        boolean isFavorite = (memberId != null) &&
            favoriteRepository.existsByRestaurantIdAndMemberId(restaurantId, memberId);

        return new RestaurantDetailsDTO(restaurant, base64Photos, reviews, isFavorite);
    }

    private void setAverageRating(Restaurant restaurant) {
        List<RestaurantReview> reviews = reviewRepository.findByRestaurantId(restaurant.getId());
        if (!reviews.isEmpty()) {
            double avg = reviews.stream()
                .mapToInt(RestaurantReview::getRating)
                .average()
                .orElse(0.0);
            restaurant.setAvgRating(Math.round(avg * 10.0) / 10.0);
        } else {
            restaurant.setAvgRating(0.0);
        }
    }

    public List<Restaurant> getTopRestaurantsByAvgRating(int limit) {
        List<Restaurant> all = restaurantRepository.findAll();
        all.forEach(this::setAverageRating);

        return all.stream()
                .sorted((a, b) -> Double.compare(b.getAvgRating(), a.getAvgRating()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
