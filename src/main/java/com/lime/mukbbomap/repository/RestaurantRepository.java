package com.lime.mukbbomap.repository;


import com.lime.mukbbomap.domain.Restaurant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * GeoHash prefix로 시작하는 맛집 검색
     */
    List<Restaurant> findByGeohashStartingWith(String geohashPrefix);

    /**
     * 여러 GeoHash prefix로 맛집 검색 (IN 쿼리)
     */
    @Query("SELECT r FROM Restaurant r WHERE " +
            "r.geohash LIKE CONCAT(:prefix1, '%') OR " +
            "r.geohash LIKE CONCAT(:prefix2, '%') OR " +
            "r.geohash LIKE CONCAT(:prefix3, '%') OR " +
            "r.geohash LIKE CONCAT(:prefix4, '%') OR " +
            "r.geohash LIKE CONCAT(:prefix5, '%') OR " +
            "r.geohash LIKE CONCAT(:prefix6, '%') OR " +
            "r.geohash LIKE CONCAT(:prefix7, '%') OR " +
            "r.geohash LIKE CONCAT(:prefix8, '%') OR " +
            "r.geohash LIKE CONCAT(:prefix9, '%')")
    List<Restaurant> findByGeohashPrefixes(
            @Param("prefix1") String prefix1,
            @Param("prefix2") String prefix2,
            @Param("prefix3") String prefix3,
            @Param("prefix4") String prefix4,
            @Param("prefix5") String prefix5,
            @Param("prefix6") String prefix6,
            @Param("prefix7") String prefix7,
            @Param("prefix8") String prefix8,
            @Param("prefix9") String prefix9
    );

    /**
     * 카테고리별 검색
     */
    List<Restaurant> findByCategory(String category);

    /**
     * 이름으로 검색
     */
    List<Restaurant> findByNameContaining(String name);
}

