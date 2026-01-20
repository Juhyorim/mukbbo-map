package com.lime.mukbbomap.util;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GeoHashUtil {
    private static final int DEFAULT_PRECISION = 7;

    public String encode(double latitude, double longitude) {
        return encode(latitude, longitude, DEFAULT_PRECISION);
    }

    public String encode(double latitude, double longitude, int precision) {
        return GeoHash.geoHashStringWithCharacterPrecision(latitude, longitude, precision);
    }

    public WGS84Point decode(String geohash) {
        return GeoHash.fromGeohashString(geohash).getOriginatingPoint();
    }

    /**
     * 두 지점 간의 거리 계산 (Haversine formula - 미터 단위)
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000; // 지구 반지름 (미터)

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 반경에 따른 GeoHash 정밀도 결정
     * 1km: 6자리, 3km: 5자리, 5km: 5자리
     */
    public int getPrecisionByRadius(int radiusInMeters) {
        if (radiusInMeters <= 1000) {
            return 6;  // ~0.61km 정밀도
        } else if (radiusInMeters <= 3000) {
            return 5;  // ~2.4km 정밀도
        } else {
            return 5;  // 5km도 5자리 사용
        }
    }

    /**
     * 중심점을 기준으로 이웃 GeoHash 영역 포함한 검색 영역 리턴
     * (현재 영역 + 8방향 이웃 = 총 9개 영역)
     */
    public List<String> getNeighborGeohashes(String centerGeohash) {
        List<String> neighbors = new ArrayList<>();

        GeoHash center = GeoHash.fromGeohashString(centerGeohash);
        neighbors.add(centerGeohash);

        // 8방향 이웃 추가
        neighbors.add(center.getNorthernNeighbour().toBase32());
        neighbors.add(center.getSouthernNeighbour().toBase32());
        neighbors.add(center.getEasternNeighbour().toBase32());
        neighbors.add(center.getWesternNeighbour().toBase32());

        GeoHash northern = center.getNorthernNeighbour();
        neighbors.add(northern.getEasternNeighbour().toBase32());
        neighbors.add(northern.getWesternNeighbour().toBase32());

        GeoHash southern = center.getSouthernNeighbour();
        neighbors.add(southern.getEasternNeighbour().toBase32());
        neighbors.add(southern.getWesternNeighbour().toBase32());

        return neighbors;
    }

    /**
     * 주어진 위치와 반경으로 검색 영역의 GeoHash prefix들 생성
     */
    public List<String> getSearchPrefixes(double latitude, double longitude, int radiusInMeters) {
        int precision = getPrecisionByRadius(radiusInMeters);
        String centerGeohash = encode(latitude, longitude, precision);

        return getNeighborGeohashes(centerGeohash);
    }
}