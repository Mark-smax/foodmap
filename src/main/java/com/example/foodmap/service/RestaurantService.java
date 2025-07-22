package com.example.foodmap.service;

import com.example.foodmap.model.Restaurant;
import com.example.foodmap.repository.RestaurantRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository repository;

    public RestaurantService(RestaurantRepository repository) {
        this.repository = repository;
    }

    // 根據縣市取得餐廳列表
    public List<Restaurant> getRestaurantsByCounty(String county) {
        return repository.findByCounty(county);
    }

    // 多條件查詢 + 分頁
    public Page<Restaurant> searchRestaurants(String county, Double minRating, String type, Pageable pageable) {
        return repository.findByCountyAndRatingGreaterThanEqualAndTypeContainingIgnoreCase(county, minRating, type, pageable);
    }

    // 新增餐廳
    public Restaurant createRestaurant(Restaurant restaurant) {
        return repository.save(restaurant);
    }

    // 修改餐廳
    public Restaurant updateRestaurant(Long id, Restaurant newData) {
        return repository.findById(id).map(r -> {
            r.setName(newData.getName());
            r.setCounty(newData.getCounty());
            r.setAddress(newData.getAddress());
            r.setPhone(newData.getPhone());
            r.setRating(newData.getRating());
            r.setType(newData.getType());
            return repository.save(r);
        }).orElseThrow(() -> new RuntimeException("Restaurant not found"));
    }

    // 刪除餐廳
    public void deleteRestaurant(Long id) {
        repository.deleteById(id);
    }
}
