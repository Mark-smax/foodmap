package com.example.foodmap.controller;

import com.example.foodmap.model.Restaurant;
import com.example.foodmap.service.RestaurantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RestaurantController {

    private final RestaurantService service;

    public RestaurantController(RestaurantService service) {
        this.service = service;
    }

    @GetMapping("/restaurants")
    public List<Restaurant> getRestaurants(@RequestParam String county) {
        return service.getRestaurantsByCounty(county);
    }
}
