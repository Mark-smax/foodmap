package com.example.foodmap.service;

import com.example.foodmap.model.Restaurant;
import com.example.foodmap.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantService {
    private final RestaurantRepository repository;

    public RestaurantService(RestaurantRepository repository) {
        this.repository = repository;
    }

    public List<Restaurant> getRestaurantsByCounty(String county) {
        return repository.findByCounty(county);
    }

    public List<Restaurant> getAll() {
        return repository.findAll();
    }
}
