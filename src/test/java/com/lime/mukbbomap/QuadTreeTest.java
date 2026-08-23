//package com.lime.mukbbomap;
//
//import java.util.*;
//
//public class QuadTreeTest {
//    public static void main(String[] args) {
//        var tree = new QuadTree<String>(new BoundingBox(128.0, 36.0, 4.5, 3.5), 4);
//
//        String[][] data = {
//            {"을지로 노포 곱창","37.5662","126.9910"},
//            {"광화문 국밥집","37.5720","126.9769"},
//            {"명동 칼국수","37.5636","126.9850"},
//            {"종로 빈대떡","37.5701","126.9880"},
//            {"성수 브런치카페","37.5445","127.0557"},
//            {"홍대 라멘","37.5563","126.9236"},
//            {"강남 스시오마카세","37.4979","127.0276"},
//            {"이태원 타코","37.5344","126.9945"},
//            {"부산 돼지국밥","35.1796","129.0756"},
//        };
//        for (String[] d : data)
//            tree.insert(new GeoPoint<>(Double.parseDouble(d[1]), Double.parseDouble(d[2]), d[0]));
//
//        System.out.println("총 색인: " + tree.size() + "곳 (부산 포함 9곳)\n");
//
//        double lat = 37.5663, lng = 126.9779, radius = 1500;
//        System.out.printf("[%.4f, %.4f] 반경 %.0fm:%n", lat, lng, radius);
//        var hits = tree.queryRadius(lat, lng, radius);
//        for (var m : hits)
//            System.out.printf("  - %-16s %7.1f m%n", m.point().value(), m.distanceMeters());
//
//        // 검증 1: 반경 안 결과가 거리 오름차순인가
//        for (int i = 1; i < hits.size(); i++)
//            assert hits.get(i-1).distanceMeters() <= hits.get(i).distanceMeters();
//
//        // 검증 2: 부산은 절대 안 잡혀야
//        boolean busan = hits.stream().anyMatch(m -> m.point().value().equals("부산 돼지국밥"));
//        System.out.println("\n[검증] 부산 미포함: " + (!busan ? "OK" : "FAIL"));
//
//        // 검증 3: remove 동작
//        var gp = new GeoPoint<>(37.5720, 126.9769, "광화문 국밥집");
//        boolean removed = tree.remove(gp);
//        var after = tree.queryRadius(lat, lng, radius);
//        boolean gone = after.stream().noneMatch(m -> m.point().value().equals("광화문 국밥집"));
//        System.out.println("[검증] remove 후 결과에서 제거: " + (removed && gone ? "OK" : "FAIL"));
//        System.out.println("[검증] 삭제 후 총 개수 8곳: " + (tree.size()==8 ? "OK" : "FAIL ("+tree.size()+")"));
//
//        // 검증 4: 반경 0 → 결과 없음, 반경 300km → 부산 포함
//        System.out.println("[검증] 반경 400km에 부산 포함: "
//                + (tree.queryRadius(lat,lng,400_000).stream()
//                     .anyMatch(m -> m.point().value().equals("부산 돼지국밥")) ? "OK" : "FAIL"));
//
//        // 검증 5: haversine 기지값 (서울시청~부산 대략 325km)
//        double seoulBusan = GeoUtils.haversine(37.5663,126.9779,35.1796,129.0756);
//        System.out.printf("[검증] 서울-부산 거리 ≈ %.0f km (예상 ~325km)%n", seoulBusan/1000);
//    }
//}
//
//
//
//// --- 실제 프로젝트의 순수 로직을 package 없이 한 파일로 합쳐 검증 ---
//
//record BoundingBox(double centerLng, double centerLat, double halfWidth, double halfHeight) {
//    boolean contains(double lat, double lng) {
//        return lng >= centerLng - halfWidth && lng <= centerLng + halfWidth
//                && lat >= centerLat - halfHeight && lat <= centerLat + halfHeight;
//    }
//    boolean intersects(BoundingBox o) {
//        return !(o.centerLng - o.halfWidth > centerLng + halfWidth
//                || o.centerLng + o.halfWidth < centerLng - halfWidth
//                || o.centerLat - o.halfHeight > centerLat + halfHeight
//                || o.centerLat + o.halfHeight < centerLat - halfHeight);
//    }
//    BoundingBox quadrant(boolean east, boolean north) {
//        double hw = halfWidth / 2.0, hh = halfHeight / 2.0;
//        return new BoundingBox(east ? centerLng + hw : centerLng - hw,
//                north ? centerLat + hh : centerLat - hh, hw, hh);
//    }
//}
//
//record GeoPoint<T>(double lat, double lng, T value) {}
//
//class GeoUtils {
//    static final double R = 6_371_000.0, MPD = 111_320.0;
//    static double haversine(double lat1, double lng1, double lat2, double lng2) {
//        double dLat = Math.toRadians(lat2 - lat1), dLng = Math.toRadians(lng2 - lng1);
//        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
//                + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
//                *Math.sin(dLng/2)*Math.sin(dLng/2);
//        return 2*R*Math.asin(Math.sqrt(a));
//    }
//    static BoundingBox radiusToBox(double lat, double lng, double r) {
//        double degLat = r/MPD, degLng = r/(MPD*Math.max(Math.cos(Math.toRadians(lat)),1e-6));
//        return new BoundingBox(lng, lat, degLng, degLat);
//    }
//}
//
//class QuadTree<T> {
//    record Match<T>(GeoPoint<T> point, double distanceMeters) {}
//    final BoundingBox boundary; final int capacity;
//    final List<GeoPoint<T>> points = new ArrayList<>();
//    boolean divided = false;
//    QuadTree<T> nw, ne, sw, se;
//    QuadTree(BoundingBox b, int c) { boundary = b; capacity = c; }
//
//    boolean insert(GeoPoint<T> p) {
//        if (!boundary.contains(p.lat(), p.lng())) return false;
//        if (!divided && points.size() < capacity) { points.add(p); return true; }
//        if (!divided) subdivide();
//        return nw.insert(p) || ne.insert(p) || sw.insert(p) || se.insert(p);
//    }
//    void subdivide() {
//        nw = new QuadTree<>(boundary.quadrant(false,true), capacity);
//        ne = new QuadTree<>(boundary.quadrant(true,true), capacity);
//        sw = new QuadTree<>(boundary.quadrant(false,false), capacity);
//        se = new QuadTree<>(boundary.quadrant(true,false), capacity);
//        divided = true;
//        for (GeoPoint<T> e : points)
//            if(!(nw.insert(e)||ne.insert(e)||sw.insert(e)||se.insert(e)))
//                throw new IllegalStateException("재배치 실패");
//        points.clear();
//    }
//    boolean remove(GeoPoint<T> p) {
//        if (!boundary.contains(p.lat(), p.lng())) return false;
//        if (!divided) return points.remove(p);
//        return nw.remove(p)||ne.remove(p)||sw.remove(p)||se.remove(p);
//    }
//    List<GeoPoint<T>> queryRange(BoundingBox area) {
//        List<GeoPoint<T>> f = new ArrayList<>(); queryRange(area, f); return f;
//    }
//    void queryRange(BoundingBox area, List<GeoPoint<T>> f) {
//        if (!boundary.intersects(area)) return;
//        for (GeoPoint<T> p : points) if (area.contains(p.lat(), p.lng())) f.add(p);
//        if (divided) { nw.queryRange(area,f); ne.queryRange(area,f); sw.queryRange(area,f); se.queryRange(area,f); }
//    }
//    List<Match<T>> queryRadius(double lat, double lng, double r) {
//        List<Match<T>> res = new ArrayList<>();
//        for (GeoPoint<T> p : queryRange(GeoUtils.radiusToBox(lat,lng,r))) {
//            double d = GeoUtils.haversine(lat,lng,p.lat(),p.lng());
//            if (d <= r) res.add(new Match<>(p, d));
//        }
//        res.sort(Comparator.comparingDouble(Match::distanceMeters));
//        return res;
//    }
//    int size() {
//        int n = points.size();
//        if (divided) n += nw.size()+ne.size()+sw.size()+se.size();
//        return n;
//    }
//}
