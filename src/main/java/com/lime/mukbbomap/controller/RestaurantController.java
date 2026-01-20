package com.lime.mukbbomap.controller;

import com.lime.mukbbomap.dto.RestaurantDto;
import com.lime.mukbbomap.dto.RestaurantDto.Response;
import com.lime.mukbbomap.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurant API", description = "맛집 등록 및 검색 API")
public class RestaurantController {
    private final RestaurantService restaurantService;

    @PostMapping
    @Operation(summary = "맛집 등록", description = "새로운 맛집을 등록합니다")
    public ResponseEntity<Response> createRestaurant(@Valid @RequestBody RestaurantDto.CreateRequest request) {
        log.info("POST /api/restaurants - Creating restaurant: {}", request.getName());

        Response response = restaurantService.createRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/nearby")
    @Operation(summary = "주변 맛집 검색", description = "지정한 반경 내의 맛집을 검색합니다")
    public ResponseEntity<List<Response>> searchNearbyRestaurants(
            @RequestParam(name = "latitude") Double latitude,
            @RequestParam(name = "longitude") Double longitude,
            @RequestParam(name = "radiusInMeters") Integer radiusInMeters,
            @RequestParam(name = "category", required = false) String category) {
        log.info("GET /api/restaurants/nearby - lat: {}, lon: {}, radius: {}m",
                latitude, longitude, radiusInMeters);

        RestaurantDto.SearchRequest request = RestaurantDto.SearchRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .radiusInMeters(radiusInMeters)
                .category(category)
                .build();

        List<RestaurantDto.Response> restaurants = restaurantService.searchNearbyRestaurants(request);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/{id}")
    @Operation(summary = "맛집 상세 조회", description = "ID로 특정 맛집을 조회합니다")
    public ResponseEntity<Response> getRestaurant(@PathVariable Long id) {
        log.info("GET /api/restaurants/{}", id);

        Response response = restaurantService.getRestaurant(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "전체 맛집 조회", description = "등록된 모든 맛집을 조회합니다")
    public ResponseEntity<List<Response>> getAllRestaurants() {
        log.info("GET /api/restaurants - Getting all restaurants");

        List<Response> restaurants = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "맛집 삭제", description = "ID로 맛집을 삭제합니다")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        log.info("DELETE /api/restaurants/{}", id);

        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }
}
