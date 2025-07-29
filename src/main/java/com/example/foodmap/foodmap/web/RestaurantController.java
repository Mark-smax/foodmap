package com.example.foodmap.foodmap.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.foodmap.foodmap.domain.RestaurantService;
import com.example.foodmap.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.foodmap.dto.RestaurantDto;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {

    private final RestaurantService service;

    public RestaurantController(RestaurantService service) {
        this.service = service;
    }

    /**
     * 搜索餐厅，支持按关键字、县市、类型、评分等搜索条件
     */
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
            @RequestParam(required = false) String memberId // 新增这个参数，用来表示会员ID
    ) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // 将传入的 memberId 字符串转换为 Long 类型
        Long memberIdLong = null;
        try {
            if (memberId != null && !memberId.isBlank()) {
                memberIdLong = Long.parseLong(memberId);
            }
        } catch (NumberFormatException e) {
            memberIdLong = null; // 如果 memberId 无法转换为 Long，设为 null
        }

        // 按关键字搜索餐厅
        if (keyword != null && !keyword.trim().isEmpty()) {
            return service.searchByKeyword(keyword.trim(), pageRequest, memberIdLong);
        }

        // 按县市和类型搜索餐厅
        if (county != null && !county.trim().isEmpty() &&
            type != null && !type.trim().isEmpty() &&
            (minRating == null || minRating == 0)) {
            return service.searchByCountyAndType(county.trim(), type.trim(), pageRequest, memberIdLong);
        }

        // 按县市、最小评分和类型进行默认搜索
        return service.searchRestaurants(county, minRating, type, pageRequest, memberIdLong);
    }

    /**
     * 获取餐厅详细信息
     * URL: /api/restaurants/{id}/details
     */
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
