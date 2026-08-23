package com.lime.mukbbomap.dto;

import com.lime.mukbbomap.domain.Restaurant;

/**
 * 반경 검색 결과 한 건. 맛집 정보 + 검색 지점으로부터의 거리(m).
 */
public record NearbyRestaurantResponse(
        Long id,
        String name,
        String category,
        String address,
        double latitude,
        double longitude,
        Double rating,
        double distanceMeters
) {
    public static NearbyRestaurantResponse of(Restaurant r, double distanceMeters) {
        return new NearbyRestaurantResponse(
                r.getId(),
                r.getName(),
                r.getCategory(),
                r.getAddress(),
                r.getLatitude(),
                r.getLongitude(),
                r.getRating(),
                Math.round(distanceMeters * 10.0) / 10.0 // 소수점 1자리
        );
    }
}
