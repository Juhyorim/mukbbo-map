package com.lime.mukbbomap.repository;

import com.lime.mukbbomap.domain.RestaurantLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantLocationRepository extends JpaRepository<RestaurantLocation, Long> {
    /**
     * 반경 검색 (2단계 필터링: MBR → ST_Distance_Sphere)
     * MBR로 Spatial Index를 태워 후보군을 줄인 뒤, 정밀 거리 필터링
     *
     * @return [restaurantId, distance(m)] 쌍의 리스트
     */
    @Query(value = """
            SELECT rl.restaurant_id AS restaurantId,
                   ST_Distance_Sphere(
                       rl.location,
                       ST_SRID(ST_PointFromText(CONCAT('POINT(', :lng, ' ', :lat, ')')), 4326)
                   ) AS distance
            FROM restaurant_location rl
            WHERE MBRContains(
                ST_SRID(ST_GeomFromText(CONCAT(
                    'LINESTRING(',
                    :lng - (:radiusMeters / (111320 * COS(RADIANS(:lat)))), ' ',
                    :lat - (:radiusMeters / 110574), ', ',
                    :lng + (:radiusMeters / (111320 * COS(RADIANS(:lat)))), ' ',
                    :lat + (:radiusMeters / 110574),
                    ')'
                )), 4326),
                rl.location
            )
            AND ST_Distance_Sphere(
                rl.location,
                ST_SRID(ST_PointFromText(CONCAT('POINT(', :lng, ' ', :lat, ')')), 4326)
            ) <= :radiusMeters
            ORDER BY distance
            """, nativeQuery = true)
    List<SpatialSearchResult> findWithinRadius(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMeters") double radiusMeters
    );

    /**
     * 인터페이스 프로젝션 - native query 결과 매핑용
     */
    interface SpatialSearchResult {
        Long getRestaurantId();
        Double getDistance();
    }
}