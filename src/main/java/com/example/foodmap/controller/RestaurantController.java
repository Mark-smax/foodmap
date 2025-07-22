package com.example.foodmap.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import com.example.foodmap.model.Restaurant;
import com.example.foodmap.service.RestaurantService;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*") // 允許所有前端網域跨域請求，開發用
public class RestaurantController {

    private final RestaurantService service;

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
}
