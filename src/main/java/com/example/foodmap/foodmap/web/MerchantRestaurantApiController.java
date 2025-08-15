// src/main/java/com/example/foodmap/foodmap/web/MerchantRestaurantApiController.java
package com.example.foodmap.foodmap.web;

import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.foodmap.foodmap.domain.ModerationStatus;
import com.example.foodmap.foodmap.domain.Restaurant;
import com.example.foodmap.foodmap.domain.RestaurantRepository;
import com.example.foodmap.foodmap.domain.RestaurantService;
import com.example.foodmap.foodmap.dto.RestaurantDto;

@RestController
@RequestMapping("/api/merchant/restaurants")
public class MerchantRestaurantApiController {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;

    public MerchantRestaurantApiController(RestaurantRepository restaurantRepository,
                                           RestaurantService restaurantService) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
    }

    // 取「我提交的餐廳」
    @GetMapping("/mine")
    public Page<RestaurantDto> myRestaurants(
            @RequestParam("memberId") Long memberId,
            @PageableDefault(size = 12) Pageable pageable) {
        Page<Restaurant> page = restaurantRepository.findBySubmittedBy(memberId, pageable);
        return page.map(r -> restaurantService.toDtoPublic(r, memberId));
    }

    // 取得單一（給編輯頁）
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(
            @PathVariable Long id,
            @RequestParam("memberId") Long memberId) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("restaurant not found"));
        if (!Objects.equals(r.getSubmittedBy(), memberId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("ok", false, "error", "NOT_OWNER"));
        }
        return ResponseEntity.ok(restaurantService.toDtoPublic(r, memberId));
    }

    // 新增
    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam("memberId") Long memberId,
            @RequestBody UpsertReq req) {
        Restaurant r = new Restaurant();
        r.setName(req.name);
        r.setCounty(req.county);
        r.setAddress(req.address);
        r.setPhone(req.phone);
        r.setType(req.type);
        r.setKeywords(req.keywords);
        r.setSubmittedBy(memberId);
        r.setStatus(ModerationStatus.PENDING);
        Restaurant saved = restaurantService.createRestaurant(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("ok", true, "id", saved.getId()));
    }

    // 修改
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestParam("memberId") Long memberId,
            @RequestBody UpsertReq req) {
        Restaurant existing = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("restaurant not found"));
        if (!Objects.equals(existing.getSubmittedBy(), memberId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("ok", false, "error", "NOT_OWNER"));
        }
        Restaurant patch = new Restaurant();
        patch.setName(req.name);
        patch.setCounty(req.county);
        patch.setAddress(req.address);
        patch.setPhone(req.phone);
        patch.setType(req.type);
        patch.setKeywords(req.keywords);
        restaurantService.updateRestaurant(id, patch);
        return ResponseEntity.ok(Map.of("ok", true, "id", id));
    }

    public static class UpsertReq {
        public String name;
        public String county;
        public String address;
        public String phone;
        public String type;
        public String keywords;
    }
}
