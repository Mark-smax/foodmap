package com.example.foodmap.controller;

import com.example.foodmap.dto.RestaurantDto;
import com.example.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.model.Restaurant;
import com.example.foodmap.service.RestaurantService;

import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {

    private final RestaurantService service;

    public RestaurantController(RestaurantService service) {
        this.service = service;
    }

    @GetMapping
    public Page<RestaurantDto> search(
            @RequestParam(required = false) String county,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") Double minRating,
            @RequestParam(defaultValue = "") String type,
            @RequestParam(required = false) String memberId // 新增這個參數
    ) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Long memberIdLong = null;
        try {
            if (memberId != null && !memberId.isBlank()) {
                memberIdLong = Long.parseLong(memberId);
            }
        } catch (NumberFormatException e) {
            memberIdLong = null;
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            return service.searchByKeyword(keyword.trim(), pageRequest, memberIdLong); // 改這裡
        }

        if (county != null && !county.trim().isEmpty() &&
            type != null && !type.trim().isEmpty() &&
            (minRating == null || minRating == 0)) {
            return service.searchByCountyAndType(county.trim(), type.trim(), pageRequest, memberIdLong); // 改這裡
        }

        return service.searchRestaurants(county, minRating, type, pageRequest, memberIdLong); // 改這裡
    }


    @GetMapping("/{id}/details")
    public RestaurantDetailsDTO getRestaurantDetails(
        @PathVariable("id") Long restaurantId,
        @RequestParam(value = "memberId", required = false) String memberIdStr
    ) {
        Long memberId = null;
        if (memberIdStr != null && !memberIdStr.isBlank()) {
            try {
                memberId = Long.parseLong(memberIdStr);
            } catch (NumberFormatException e) {
                memberId = null;
            }
        }
        return service.getRestaurantDetails(restaurantId, memberId);
    }
}
