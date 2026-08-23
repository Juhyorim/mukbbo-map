package com.lime.mukbbomap.service;

import com.lime.mukbbomap.domain.Restaurant;

import com.lime.mukbbomap.dto.NearbyRestaurantResponse;
import com.lime.mukbbomap.index.RestaurantQuadTreeIndex;
import com.lime.mukbbomap.repository.RestaurantRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 맛집 CRUD 및 반경 검색 서비스.
 *
 * <p>쓰기 작업은 DB에 먼저 반영한 뒤 인메모리 쿼드트리 인덱스를 함께 갱신해
 * 두 저장소를 일관되게 유지한다. 반경 검색은 인덱스(쿼드트리)로만 처리한다.
 *
 * <p>참고: 단일 인스턴스 기준 구현이다. 여러 인스턴스로 스케일아웃하면 각 인스턴스의
 * 인메모리 인덱스를 어떻게 동기화할지(예: 이벤트 브로드캐스트, 주기적 rebuild)를
 * 별도로 설계해야 한다 — 이게 GeoHash+RDB 방식 대비 쿼드트리 인메모리 방식의 대표적 트레이드오프다.
 */
@Service
public class RestaurantSearchService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantQuadTreeIndex index;

    public RestaurantSearchService(RestaurantRepository restaurantRepository,
                                   RestaurantQuadTreeIndex index) {
        this.restaurantRepository = restaurantRepository;
        this.index = index;
    }

    /** 맛집 등록: DB 저장 후 인덱스에 추가. */
    @Transactional
    public Restaurant create(Restaurant restaurant) {
        Restaurant saved = restaurantRepository.save(restaurant);
        index.add(saved);
        return saved;
    }

    /** 위치 변경: DB 갱신 후 인덱스 갱신(제거 후 재삽입). */
    @Transactional
    public Restaurant updateLocation(Long id, double lat, double lng, String geohash) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("맛집 없음: " + id));
        r.updateLocation(lat, lng, geohash);
        Restaurant saved = restaurantRepository.save(r);
        index.update(saved);
        return saved;
    }

    /** 삭제: DB 삭제 후 인덱스에서 제거. */
    @Transactional
    public void delete(Long id) {
        restaurantRepository.deleteById(id);
        index.remove(id);
    }

    /**
     * 반경 검색. (lat, lng)에서 radiusMeters 이내 맛집을 가까운 순으로 반환.
     */
    @Transactional(readOnly = true)
    public List<NearbyRestaurantResponse> findNearby(
            double lat, double lng, double radiusMeters) {
        return index.searchNearby(lat, lng, radiusMeters).stream()
                .map(m -> NearbyRestaurantResponse.of(m.point().value(), m.distanceMeters()))
                .toList();
    }

    /**
     * 반경 검색 + 카테고리 필터. 검색은 인덱스로 하고 카테고리는 메모리에서 거른다.
     */
    @Transactional(readOnly = true)
    public List<NearbyRestaurantResponse> findNearbyByCategory(
            double lat, double lng, double radiusMeters, String category) {
        return index.searchNearby(lat, lng, radiusMeters).stream()
                .filter(m -> category.equals(m.point().value().getCategory()))
                .map(m -> NearbyRestaurantResponse.of(m.point().value(), m.distanceMeters()))
                .toList();
    }
}
