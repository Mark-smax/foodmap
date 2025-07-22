package com.example.foodmap.controller;

import org.springframework.web.bind.annotation.*;

import com.example.foodmap.model.Restaurant;

import java.util.*;

@RestController
@RequestMapping("/api")
public class RestaurantController {

    @GetMapping("/restaurants")
    public List<Restaurant> getRestaurants(@RequestParam String county) {
        List<Restaurant> list = new ArrayList<>();

        if ("屏東縣".equals(county)) {
            list.add(new Restaurant("阿豬肉圓",
                    "https://i.imgur.com/0KfbGDb.jpg",
                    4.5,
                    "屏東市中正路123號",
                    "08-1234567",
                    Arrays.asList("#屏東縣", "#屏東市", "#小吃", "#銅板美食")));

            list.add(new Restaurant("屏東豆花",
                    "https://i.imgur.com/tv9FqT1.jpg",
                    4.2,
                    "屏東市民族路88號",
                    "08-7654321",
                    Arrays.asList("#屏東縣", "#甜品", "#下午茶")));
        }

        return list;
    }
}
