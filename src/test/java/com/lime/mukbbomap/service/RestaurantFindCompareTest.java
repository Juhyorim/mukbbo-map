package com.lime.mukbbomap.service;

import com.lime.mukbbomap.dto.RestaurantDto;
import com.lime.mukbbomap.dto.RestaurantDto.Response;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RestaurantFindCompareTest {
    @Autowired
    private RestaurantService restaurantService;

    //실제 디비 시간 테스트
    @Test
    @DisplayName("반경 5km 내 맛집 검색")
    void searchNearbyRestaurants_1km() {
        RestaurantDto.SearchRequest request = new RestaurantDto.SearchRequest(
                37.4979,
                127.0276,
                5000,
                null
        );

        // when
        long geoStart = System.currentTimeMillis();
        List<Response> geoResult = restaurantService.searchNearbyRestaurants(request);
        long geoEnd = System.currentTimeMillis();

        long spaStart = System.currentTimeMillis();
        List<Response> spaResult = restaurantService.searchBySpatialIndex(request);
        long spaEnd = System.currentTimeMillis();

        // then
        System.out.println("GEO 조회 결과: " + geoResult.size() + "건");
        System.out.println("GEO 소요 시간: " + (geoEnd - geoStart) + "ms");

        System.out.println("SPA 조회 결과: " + spaResult.size() + "건");
        System.out.println("SPA 소요 시간: " + (spaEnd - spaStart) + "ms");
    }
}