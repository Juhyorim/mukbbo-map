package com.lime.mukbbomap.controller;

import com.lime.mukbbomap.document.RestaurantDocument;
import com.lime.mukbbomap.service.RestaurantSearchService;
import com.lime.mukbbomap.service.RestaurantSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants/search")
@RequiredArgsConstructor
public class RestaurantSearchController {
    private final RestaurantSearchService searchService;
    private final RestaurantSyncService syncService;

    @GetMapping
    public ResponseEntity<List<RestaurantDocument>> searchRestaurants(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<RestaurantDocument> results = searchService.searchRestaurants(keyword, page, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<RestaurantDocument>> searchByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<RestaurantDocument> results = searchService.searchByCategory(category, page, size);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncAllRestaurants() {
        syncService.syncAllRestaurants();
        return ResponseEntity.ok("Sync completed");
    }
}