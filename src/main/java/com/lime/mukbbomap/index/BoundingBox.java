package com.lime.mukbbomap.index;

/**
 * 쿼드트리 노드의 경계 사각형.
 *
 * <p>위경도 좌표계를 그대로 쓴다. 중심 좌표 + 반너비/반높이(half-width/half-height)
 * 방식으로 표현하는데, 이렇게 두면 4분할할 때 자식 경계 계산이 간결하다.
 *
 * @param centerLng 중심 경도(x)
 * @param centerLat 중심 위도(y)
 * @param halfWidth 경도 방향 반너비
 * @param halfHeight 위도 방향 반높이
 */
public record BoundingBox(
        double centerLng,
        double centerLat,
        double halfWidth,
        double halfHeight
) {

    /** 점(lat, lng)이 이 사각형 안에 있는지. */
    public boolean contains(double lat, double lng) {
        return lng >= centerLng - halfWidth
                && lng <= centerLng + halfWidth
                && lat >= centerLat - halfHeight
                && lat <= centerLat + halfHeight;
    }

    /** 다른 사각형과 겹치는지(범위 검색에서 가지치기용). */
    public boolean intersects(BoundingBox other) {
        return !(other.centerLng - other.halfWidth > centerLng + halfWidth
                || other.centerLng + other.halfWidth < centerLng - halfWidth
                || other.centerLat - other.halfHeight > centerLat + halfHeight
                || other.centerLat + other.halfHeight < centerLat - halfHeight);
    }

    /** 사분면 자식 경계 생성 헬퍼. */
    public BoundingBox quadrant(boolean east, boolean north) {
        double hw = halfWidth / 2.0;
        double hh = halfHeight / 2.0;
        double cx = east ? centerLng + hw : centerLng - hw;
        double cy = north ? centerLat + hh : centerLat - hh;
        return new BoundingBox(cx, cy, hw, hh);
    }
}
