package com.lime.mukbbomap.service;

import com.lime.mukbbomap.domain.Restaurant;
import com.lime.mukbbomap.domain.RestaurantLocation;
import com.lime.mukbbomap.dto.RestaurantDto;
import com.lime.mukbbomap.dto.RestaurantDto.Response;
import com.lime.mukbbomap.repository.RestaurantLocationRepository;
import com.lime.mukbbomap.repository.RestaurantLocationRepository.SpatialSearchResult;
import com.lime.mukbbomap.repository.RestaurantRepository;
import com.lime.mukbbomap.util.GeoHashUtil;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
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
    private static final int SRID_WGS84 = 4326;

    private final RestaurantRepository restaurantRepository;
    private final RestaurantLocationRepository restaurantLocationRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID_WGS84);
    private final GeoHashUtil geoHashUtil;

    /**
     * 맛집 등록
     */
    @Transactional
    @CacheEvict(value = "restaurants", allEntries = true)
    public RestaurantDto.Response createRestaurant(RestaurantDto.CreateRequest request) {
        log.info("Creating restaurant: {}", request.name());

        // 1) GeoHash 생성
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

        // 2) restaurant_location 저장 (Spatial Index용)
        Point point = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        RestaurantLocation location = new RestaurantLocation(saved.getId(), point);
        restaurantLocationRepository.save(location);

        return RestaurantDto.Response.from(saved);
    }

    /**
     * 반경 기반 맛집 검색 - geohash
     */
//    @Cacheable(value = "restaurants",
//            key = "#request.latitude + ':' + #request.longitude + ':' + #request.radiusInMeters + ':' + (#request.category != null ? #request.category : 'all')",
//            unless = "#result == null || #result.isEmpty()")
    public List<RestaurantDto.Response> searchNearbyRestaurants(RestaurantDto.SearchRequest request) {
        double lat = request.latitude();
        double lon = request.longitude();
        int radius = request.radiusInMeters();

        List<String> searchPrefixes = geoHashUtil.getSearchPrefixes(lat, lon, radius);
        List<Restaurant> candidates = findByPrefixes(searchPrefixes);

        return candidates.stream()
                .filter(r -> request.category() == null ||
                        r.getCategory().equals(request.category()))
                // GeoHash 후보 → 실제 거리 검증
                .map(r -> {
                    double distance = geoHashUtil.calculateDistance(
                            lat, lon, r.getLatitude(), r.getLongitude());
                    return Map.entry(r, distance);
                })
                .filter(entry -> entry.getValue() <= radius)
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .map(entry -> RestaurantDto.Response.from(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Spatial Index 기반 검색
     */
    public List<RestaurantDto.Response> searchBySpatialIndex(RestaurantDto.SearchRequest request) {
        double lat = request.latitude();
        double lng = request.longitude();
        int radius = request.radiusInMeters();

        // 1) Spatial 쿼리로 반경 내 restaurantId + distance 조회
        List<SpatialSearchResult> spatialResults =
                restaurantLocationRepository.findWithinRadius(lat, lng, radius);

        if (spatialResults.isEmpty()) {
            return List.of();
        }

        // 2) restaurantId 목록으로 Restaurant 엔티티 일괄 조회
        List<Long> restaurantIds = spatialResults.stream()
                .map(SpatialSearchResult::getRestaurantId)
                .toList();

        Map<Long, Restaurant> restaurantMap = restaurantRepository.findAllById(restaurantIds)
                .stream()
                .collect(Collectors.toMap(Restaurant::getId, Function.identity()));

        // 3) 카테고리 필터 + distance 매핑 → Response 변환
        return spatialResults.stream()
                .filter(sr -> {
                    Restaurant r = restaurantMap.get(sr.getRestaurantId());
                    return r != null && (request.category() == null ||
                            r.getCategory().equals(request.category()));
                })
                .map(sr -> {
                    Restaurant r = restaurantMap.get(sr.getRestaurantId());
                    return RestaurantDto.Response.from(r, sr.getDistance());
                })
                .toList();
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
        restaurantLocationRepository.deleteById(id);
        restaurantRepository.deleteById(id);
        log.info("Restaurant deleted: {}", id);
    }
}