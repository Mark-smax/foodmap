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

    // 多條件分頁查詢，根據 type 是否空白選擇不同方法 (包含 rating 篩選)
    public Page<Restaurant> searchRestaurants(String county, Double minRating, String type, Pageable pageable) {
        if (type == null || type.trim().isEmpty()) {
            // type 空白時，只根據 county 查詢
            return restaurantRepository.findByCounty(county, pageable);
        } else {
            // 有指定 type，依多條件查詢（含 rating 篩選）
            return restaurantRepository.findByCountyAndRatingGreaterThanEqualAndTypeContainingIgnoreCase(
                    county, minRating, type, pageable);
        }
    }

    // 新增：縣市 + 類別 查詢，不帶 rating 篩選（用於點選分類時）
    public Page<Restaurant> searchByCountyAndType(String county, String type, Pageable pageable) {
        if (county == null || county.trim().isEmpty() || type == null || type.trim().isEmpty()) {
            return Page.empty(pageable);  // 沒條件就回空分頁
        }
        return restaurantRepository.findByCountyAndTypeContainingIgnoreCase(county, type, pageable);
    }

    // 新增：關鍵字模糊搜尋，搜尋 name, address, type, county
    public Page<Restaurant> searchByKeyword(String keyword, Pageable pageable) {
        return restaurantRepository.searchByKeyword(keyword, pageable);
    }

    // 新增餐廳
    public Restaurant createRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    // 修改餐廳
    public Restaurant updateRestaurant(Long id, Restaurant updated) {
        Optional<Restaurant> optional = restaurantRepository.findById(id);
        if (optional.isPresent()) {
            Restaurant existing = optional.get();
            // 更新欄位
            existing.setName(updated.getName());
            existing.setCounty(updated.getCounty());
            existing.setAddress(updated.getAddress());
            existing.setPhone(updated.getPhone());
            existing.setRating(updated.getRating());
            existing.setType(updated.getType());
            return restaurantRepository.save(existing);
        } else {
            // 找不到該餐廳，這裡可自行決定要丟例外或回 null
            return null;
        }
    }

    // 刪除餐廳
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }
}
