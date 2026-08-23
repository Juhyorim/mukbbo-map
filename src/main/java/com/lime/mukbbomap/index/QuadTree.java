package com.lime.mukbbomap.index;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 위경도 좌표를 담는 제네릭 쿼드트리.
 *
 * <p>한 노드에 점이 {@code capacity}개까지 담기고, 넘으면 사분면(북서/북동/남서/남동)으로
 * 분할한 뒤 기존 점들을 자식으로 내려보낸다. 데이터가 몰린 지역은 자동으로 잘게 쪼개지고,
 * 한산한 지역은 큰 노드로 남는 것이 GeoHash 고정 격자와의 가장 큰 차이다.
 *
 * <p>이 클래스는 <b>스레드 안전하지 않다.</b> 동시 접근 제어는 이 트리를 감싸는
 * {@link RestaurantQuadTreeIndex}에서 읽기/쓰기 락으로 처리한다.
 *
 * @param <T> 각 좌표에 붙는 값의 타입
 */
public class QuadTree<T> {

    /** 반경 검색 결과. 값과 중심으로부터의 실제 거리(m)를 함께 담는다. */
    public record Match<T>(GeoPoint<T> point, double distanceMeters) {
    }

    private final BoundingBox boundary;
    private final int capacity;
    private final List<GeoPoint<T>> points = new ArrayList<>();

    private boolean divided = false;
    private QuadTree<T> nw, ne, sw, se;

    public QuadTree(BoundingBox boundary, int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity는 1 이상이어야 합니다.");
        }
        this.boundary = boundary;
        this.capacity = capacity;
    }

    // ----------------------------------------------------------------- 삽입

    /**
     * 점 하나를 삽입한다. 이 노드의 경계 밖이면 {@code false}.
     */
    public boolean insert(GeoPoint<T> p) {
        if (!boundary.contains(p.lat(), p.lng())) {
            return false;
        }
        if (!divided && points.size() < capacity) {
            points.add(p);
            return true;
        }
        if (!divided) {
            subdivide();
        }
        return nw.insert(p) || ne.insert(p) || sw.insert(p) || se.insert(p);
    }

    private void subdivide() {
        nw = new QuadTree<>(boundary.quadrant(false, true), capacity);
        ne = new QuadTree<>(boundary.quadrant(true, true), capacity);
        sw = new QuadTree<>(boundary.quadrant(false, false), capacity);
        se = new QuadTree<>(boundary.quadrant(true, false), capacity);
        divided = true;

        for (GeoPoint<T> existing : points) {
            boolean placed = nw.insert(existing) || ne.insert(existing)
                    || sw.insert(existing) || se.insert(existing);
            assert placed : "분할 후 재배치 실패: " + existing;
        }
        points.clear();
    }

    // ----------------------------------------------------------------- 삭제

    /**
     * 점 하나를 제거한다. 좌표로 해당 노드까지 내려간 뒤 {@code equals}로 찾아 지운다.
     *
     * <p>빈 자식 노드를 부모로 병합(merge)하는 처리는 생략했다. 삭제가 잦아도 검색
     * 정확도에는 영향이 없고, 빈 노드를 지나치는 비용은 미미하기 때문. 갱신이 매우 잦다면
     * {@link RestaurantQuadTreeIndex#rebuild}로 주기적으로 통째 재구축하는 편이 낫다.
     *
     * @return 제거되었으면 true
     */
    public boolean remove(GeoPoint<T> p) {
        if (!boundary.contains(p.lat(), p.lng())) {
            return false;
        }
        if (!divided) {
            return points.remove(p);
        }
        return nw.remove(p) || ne.remove(p) || sw.remove(p) || se.remove(p);
    }

    // ------------------------------------------------------------- 범위 검색

    /** 사각형 영역 안의 점들을 모은다. */
    public List<GeoPoint<T>> queryRange(BoundingBox area) {
        List<GeoPoint<T>> found = new ArrayList<>();
        queryRange(area, found);
        return found;
    }

    private void queryRange(BoundingBox area, List<GeoPoint<T>> found) {
        if (!boundary.intersects(area)) {
            return; // 겹치지 않는 서브트리는 통째로 스킵
        }
        for (GeoPoint<T> p : points) {
            if (area.contains(p.lat(), p.lng())) {
                found.add(p);
            }
        }
        if (divided) {
            nw.queryRange(area, found);
            ne.queryRange(area, found);
            sw.queryRange(area, found);
            se.queryRange(area, found);
        }
    }

    // ------------------------------------------------------------- 반경 검색

    /**
     * (lat, lng)에서 radiusMeters 이내의 점을 가까운 순으로 반환한다.
     *
     * <p>1) 반경을 감싸는 사각형으로 후보를 빠르게 좁히고(트리 가지치기),
     * 2) 후보만 Haversine으로 실제 거리를 재서 원 밖을 걸러낸다.
     */
    public List<Match<T>> queryRadius(double lat, double lng, double radiusMeters) {
        BoundingBox box = GeoUtils.radiusToBox(lat, lng, radiusMeters);
        List<GeoPoint<T>> candidates = queryRange(box);

        List<Match<T>> results = new ArrayList<>();
        for (GeoPoint<T> p : candidates) {
            double d = GeoUtils.haversine(lat, lng, p.lat(), p.lng());
            if (d <= radiusMeters) {
                results.add(new Match<>(p, d));
            }
        }
        results.sort(Comparator.comparingDouble(Match::distanceMeters));
        return results;
    }

    // --------------------------------------------------------------- 기타

    /** 트리에 담긴 점의 총 개수. */
    public int size() {
        int n = points.size();
        if (divided) {
            n += nw.size() + ne.size() + sw.size() + se.size();
        }
        return n;
    }

    //디버그용, 운영삭제 필요 TODO 삭제 start
    public String dumpStructure() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("총 %d곳 · 최대깊이 %d · 리프 %d개%n%n",
                size(), maxDepth(), countLeaves()));
        dump(sb, "", "ROOT");
        return sb.toString();
    }

    private void dump(StringBuilder sb, String indent, String label) {
        double cellKm = boundary.halfWidth() * 2 * 111.32;
        if (!divided) {
            if (points.isEmpty()) return;
            sb.append(String.format("%s%s [리프] %d개 · 셀폭 %.1fkm%n",
                    indent, label, points.size(), cellKm));
            for (GeoPoint<T> p : points) {
                sb.append(String.format("%s      └ %s (%.4f, %.4f)%n",
                        indent, p.value(), p.lat(), p.lng()));
            }
        } else {
            sb.append(String.format("%s%s [분할됨]%n", indent, label));
            nw.dump(sb, indent + "   ", "├ NW");
            ne.dump(sb, indent + "   ", "├ NE");
            sw.dump(sb, indent + "   ", "├ SW");
            se.dump(sb, indent + "   ", "└ SE");
        }
    }

    public int maxDepth() {
        if (!divided) return points.isEmpty() ? 0 : 1;
        return 1 + Math.max(Math.max(nw.maxDepth(), ne.maxDepth()),
                Math.max(sw.maxDepth(), se.maxDepth()));
    }

    public int countLeaves() {
        if (!divided) return points.isEmpty() ? 0 : 1;
        return nw.countLeaves() + ne.countLeaves() + sw.countLeaves() + se.countLeaves();
    }
    // TODO 삭제 end
}
