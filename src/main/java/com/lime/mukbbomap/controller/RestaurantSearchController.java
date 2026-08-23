package com.lime.mukbbomap.controller;

import com.lime.mukbbomap.dto.NearbyRestaurantResponse;
import java.util.List;

import com.lime.mukbbomap.service.RestaurantSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 주변 맛집찾기 API.
 *
 * <pre>
 *   GET /api/restaurants/nearby?lat=37.5663&lng=126.9779&radius=1500
 *   GET /api/restaurants/nearby?lat=37.5663&lng=126.9779&radius=1500&category=한식
 * </pre>
 */
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantSearchController {

    private final RestaurantSearchService searchService;

    public RestaurantSearchController(RestaurantSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/nearbyquad")
    public List<NearbyRestaurantResponse> nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1000") double radius,
            @RequestParam(required = false) String category) {

        if (category != null && !category.isBlank()) {
            return searchService.findNearbyByCategory(lat, lng, radius, category);
        }
        return searchService.findNearby(lat, lng, radius);
    }
}
