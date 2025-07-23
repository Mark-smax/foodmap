package com.example.foodmap.controller;

import com.example.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.model.Restaurant;
import com.example.foodmap.model.RestaurantPhoto;
import com.example.foodmap.model.RestaurantReview;
import com.example.foodmap.service.RestaurantService;
import com.example.foodmap.repository.RestaurantFavoriteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*") // 允許所有前端網域跨域請求，開發用
public class RestaurantController {

    private final RestaurantService service;

    @Autowired
    private RestaurantFavoriteRepository favoriteRepository;

    public RestaurantController(RestaurantService service) {
        this.service = service;
    }

    // GET 多條件查詢 + 分頁（新增 keyword）
    @GetMapping
    public Page<Restaurant> search(
            @RequestParam(required = false) String county,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") Double minRating,
            @RequestParam(defaultValue = "") String type) {

        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // 1. keyword 優先做模糊搜尋
        if (keyword != null && !keyword.trim().isEmpty()) {
            return service.searchByKeyword(keyword.trim(), pageRequest);
        }

        // 2. 若無 keyword，判斷是否只有 county + type，且 minRating == 0，呼叫不帶 rating 篩選的新方法
        if (county != null && !county.trim().isEmpty() &&
            type != null && !type.trim().isEmpty() &&
            (minRating == null || minRating == 0)) {
            return service.searchByCountyAndType(county.trim(), type.trim(), pageRequest);
        }

        // 3. 其他情況，呼叫原本有 rating 篩選的多條件查詢
        return service.searchRestaurants(county, minRating, type, pageRequest);
    }

    // POST 新增餐廳
    @PostMapping
    public Restaurant create(@RequestBody Restaurant restaurant) {
        return service.createRestaurant(restaurant);
    }

    // PUT 修改餐廳
    @PutMapping("/{id}")
    public Restaurant update(@PathVariable Long id, @RequestBody Restaurant restaurant) {
        return service.updateRestaurant(id, restaurant);
    }

    // DELETE 刪除餐廳
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteRestaurant(id);
    }

    // GET 取得餐廳詳細資訊（圖片、評論、是否已收藏）
    @GetMapping("/{id}/details")
    public RestaurantDetailsDTO getRestaurantDetails(@PathVariable("id") Long restaurantId,
                                                     @RequestParam(value = "memberId", required = false) Long memberId) {
        Restaurant restaurant = service.getRestaurantById(restaurantId);
        List<RestaurantPhoto> photos = service.getPhotosByRestaurantId(restaurantId);
        List<RestaurantReview> reviews = service.getReviewsByRestaurantId(restaurantId);
        boolean isFavorite = false;
        if (memberId != null) {
            isFavorite = favoriteRepository.isFavorite(restaurantId, memberId);
        }

        return new RestaurantDetailsDTO(restaurant, photos, reviews, isFavorite);
    }
}
