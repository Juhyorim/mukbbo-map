package com.lime.mukbbomap.index;

import com.lime.mukbbomap.domain.Restaurant;
import com.lime.mukbbomap.repository.RestaurantRepository;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 맛집 위치 인메모리 쿼드트리 인덱스.
 *
 * <p>DB(RDB)는 원본 저장소로 그대로 두고, 이 컴포넌트가 공간 인덱스 역할을 한다.
 * 애플리케이션 기동 시 DB의 맛집을 전부 로드해 트리를 만들고, 이후 맛집이
 * 추가/수정/삭제될 때 {@link com.lime.mukbbomap.service.RestaurantSearchService}가
 * 이 인덱스를 함께 갱신한다.
 *
 * <h3>동시성</h3>
 * 트리 자체는 스레드 안전하지 않으므로 {@link ReentrantReadWriteLock}으로 감싼다.
 * 검색은 읽기 락(동시 다수 허용), 삽입/삭제/재구축은 쓰기 락(배타적)을 잡는다.
 *
 * <h3>위치 갱신</h3>
 * {@code id -> GeoPoint} 맵을 함께 유지한다. 좌표가 바뀌면 예전 좌표의 노드를 찾아야
 * 정확히 제거할 수 있는데, 이 맵이 '이전에 넣었던 바로 그 GeoPoint 참조'를 돌려준다.
 */
@Component
public class RestaurantQuadTreeIndex {

    private static final Logger log = LoggerFactory.getLogger(RestaurantQuadTreeIndex.class);

    // 대한민국 전역을 넉넉히 덮는 루트 경계
    //   위도 약 33~39, 경도 약 124~132 → 중심(경도 128, 위도 36), 반경 4.5/3.5도
    private static final BoundingBox KOREA_BOUNDS =
            new BoundingBox(128.0, 36.0, 4.5, 3.5);
    private static final int NODE_CAPACITY = 16;

    private final RestaurantRepository restaurantRepository;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private QuadTree<Restaurant> tree = new QuadTree<>(KOREA_BOUNDS, NODE_CAPACITY);
    private final Map<Long, GeoPoint<Restaurant>> byId = new HashMap<>();

    public RestaurantQuadTreeIndex(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    /** 기동 시 DB의 모든 맛집으로 트리를 초기 구축. */
    @PostConstruct
    public void init() {
        rebuild(restaurantRepository.findAll());
    }

    /** 주어진 목록으로 트리를 통째로 재구축한다(주기적 정리나 대량 변경 후). */
    public void rebuild(List<Restaurant> restaurants) {
        lock.writeLock().lock();
        try {
            QuadTree<Restaurant> fresh = new QuadTree<>(KOREA_BOUNDS, NODE_CAPACITY);
            Map<Long, GeoPoint<Restaurant>> freshIndex = new HashMap<>();
            int skipped = 0;
            for (Restaurant r : restaurants) {
                GeoPoint<Restaurant> gp =
                        new GeoPoint<>(r.getLatitude(), r.getLongitude(), r);
                if (fresh.insert(gp)) {
                    freshIndex.put(r.getId(), gp);
                } else {
                    skipped++; // 경계 밖 좌표(데이터 오류 등)
                }
            }
            this.tree = fresh;
            this.byId.clear();
            this.byId.putAll(freshIndex);
            log.info("쿼드트리 재구축 완료: {}건 색인, {}건 스킵", freshIndex.size(), skipped);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** 맛집 하나를 인덱스에 추가. */
    public void add(Restaurant r) {
        lock.writeLock().lock();
        try {
            GeoPoint<Restaurant> gp =
                    new GeoPoint<>(r.getLatitude(), r.getLongitude(), r);
            if (tree.insert(gp)) {
                byId.put(r.getId(), gp);
            } else {
                log.warn("경계 밖 좌표로 색인 실패: id={}, lat={}, lng={}",
                        r.getId(), r.getLatitude(), r.getLongitude());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 위치/정보가 바뀐 맛집을 반영. 예전 좌표의 점을 제거하고 새 좌표로 다시 넣는다.
     */
    public void update(Restaurant r) {
        lock.writeLock().lock();
        try {
            GeoPoint<Restaurant> old = byId.remove(r.getId());
            if (old != null) {
                tree.remove(old);
            }
            GeoPoint<Restaurant> gp =
                    new GeoPoint<>(r.getLatitude(), r.getLongitude(), r);
            if (tree.insert(gp)) {
                byId.put(r.getId(), gp);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** 맛집을 인덱스에서 제거. */
    public void remove(Long id) {
        lock.writeLock().lock();
        try {
            GeoPoint<Restaurant> old = byId.remove(id);
            if (old != null) {
                tree.remove(old);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 반경 검색. (lat, lng)에서 radiusMeters 이내 맛집을 가까운 순으로 반환.
     */
    public List<QuadTree.Match<Restaurant>> searchNearby(
            double lat, double lng, double radiusMeters) {
        lock.readLock().lock();
        try {
            return tree.queryRadius(lat, lng, radiusMeters);
        } finally {
            lock.readLock().unlock();
        }
    }

    /** 현재 색인된 맛집 수. */
    public int size() {
        lock.readLock().lock();
        try {
            return tree.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    //디버그용, TODO 삭제
    /** 현재 메모리에 구성된 트리 구조를 문자열로 반환. */
    public String dumpTree() {
        lock.readLock().lock();
        try {
            return tree.dumpStructure();
        } finally {
            lock.readLock().unlock();
        }
    }
}
