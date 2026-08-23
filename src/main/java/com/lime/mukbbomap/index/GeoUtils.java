package com.lime.mukbbomap.index;

/**
 * 위경도 기반 거리 계산 유틸.
 */
public final class GeoUtils {

    private static final double EARTH_RADIUS_M = 6_371_000.0;
    /** 위도 1도당 대략적인 거리(m). 지구를 구로 근사. */
    private static final double METERS_PER_DEG_LAT = 111_320.0;

    private GeoUtils() {
    }

    /**
     * 두 위경도 사이의 대원 거리(미터). Haversine 공식.
     */
    public static double haversine(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * EARTH_RADIUS_M * Math.asin(Math.sqrt(a));
    }

    /**
     * (lat, lng)를 중심으로 반경 radiusMeters를 감싸는 사각형 경계를 만든다.
     *
     * <p>원을 쿼드트리 범위 검색에 바로 넣을 수 없으니, 원을 감싸는 정사각형으로 후보를
     * 좁힌 뒤 Haversine으로 실제 거리를 재는 2단계 검색에 쓴다. 위도는 어디서나 거의
     * 일정하지만, 경도 1도의 실제 거리는 고위도로 갈수록 짧아지므로 cos(lat)로 보정한다.
     */
    public static BoundingBox radiusToBox(double lat, double lng, double radiusMeters) {
        double degLat = radiusMeters / METERS_PER_DEG_LAT;
        double cosLat = Math.max(Math.cos(Math.toRadians(lat)), 1e-6);
        double degLng = radiusMeters / (METERS_PER_DEG_LAT * cosLat);
        return new BoundingBox(lng, lat, degLng, degLat);
    }
}
