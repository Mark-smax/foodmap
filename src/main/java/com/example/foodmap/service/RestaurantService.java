package com.example.foodmap.service;

import com.example.foodmap.model.Restaurant;
import com.example.foodmap.repository.RestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
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

    public Restaurant createRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

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

    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }
}
