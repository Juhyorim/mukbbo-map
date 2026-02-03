package com.lime.mukbbomap.service;

import com.lime.mukbbomap.domain.Restaurant;
import com.lime.mukbbomap.dto.RestaurantDto;
import com.lime.mukbbomap.dto.RestaurantDto.Response;
import com.lime.mukbbomap.repository.RestaurantRepository;
import com.lime.mukbbomap.util.GeoHashUtil;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final GeoHashUtil geoHashUtil;

    /**
     * 맛집 등록
     */
    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public RestaurantDto.Response createRestaurant(RestaurantDto.CreateRequest request) {
        log.info("Creating restaurant: {}", request.name());

        // GeoHash 생성
        String geohash = geoHashUtil.encode(request.latitude(), request.longitude());

        Restaurant restaurant = Restaurant.builder()
                .name(request.name())
                .category(request.category())
                .description(request.description())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .geohash(geohash)
                .phoneNumber(request.phoneNumber())
                .rating(request.rating())
                .build();

        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Restaurant created with ID: {}, GeoHash: {}", saved.getId(), geohash);

        return RestaurantDto.Response.from(saved);
    }

    /**
     * 반경 기반 맛집 검색 (캐싱 적용)
     */
    @Cacheable(value = "restaurants",
            key = "#request.latitude + ':' + #request.longitude + ':' + #request.radiusInMeters + ':' + (#request.category != null ? #request.category : 'all')",
            unless = "#result == null || #result.isEmpty()")
    public List<RestaurantDto.Response> searchNearbyRestaurants(RestaurantDto.SearchRequest request) {
        log.info("Searching restaurants - lat: {}, lon: {}, radius: {}m, category: {}",
                request.latitude(), request.longitude(),
                request.radiusInMeters(), request.category());

        // GeoHash prefix 생성 (중심점 + 8방향 이웃)
        List<String> searchPrefixes = geoHashUtil.getSearchPrefixes(
                request.latitude(),
                request.longitude(),
                request.radiusInMeters()
        );

        log.debug("Search prefixes: {}", searchPrefixes);

        // GeoHash prefix로 DB 조회 (9개 영역)
        List<Restaurant> candidates = findByPrefixes(searchPrefixes);
        log.debug("Found {} candidate restaurants", candidates.size());

        // 카테고리 필터링만 수행
        List<RestaurantDto.Response> results = candidates.stream()
                .filter(restaurant -> request.category() == null ||
                        restaurant.getCategory().equals(request.category()))
                .map(RestaurantDto.Response::from)
                .collect(Collectors.toList());

        log.info("Found {} restaurants within search area", results.size());
        return results;
    }

    /**
     * 여러 GeoHash prefix로 조회 (9개 파라미터 전달)
     */
    private List<Restaurant> findByPrefixes(List<String> prefixes) {
        if (prefixes.size() < 9) {
            // 부족한 경우 빈 문자열로 채움
            List<String> paddedPrefixes = new ArrayList<>(prefixes);
            while (paddedPrefixes.size() < 9) {
                paddedPrefixes.add("");
            }
            prefixes = paddedPrefixes;
        }

        return restaurantRepository.findByGeohashPrefixes(
                prefixes.get(0), prefixes.get(1), prefixes.get(2),
                prefixes.get(3), prefixes.get(4), prefixes.get(5),
                prefixes.get(6), prefixes.get(7), prefixes.get(8)
        );
    }

    /**
     * ID로 맛집 조회
     */
    public RestaurantDto.Response getRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + id));
        return RestaurantDto.Response.from(restaurant);
    }

    /**
     * 전체 맛집 조회 (페이지네이션)
     */
    public Page<Response> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findAll(pageable)
                .map(RestaurantDto.Response::from);
    }

    /**
     * 맛집 삭제
     */
    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
        log.info("Restaurant deleted: {}", id);
    }
}