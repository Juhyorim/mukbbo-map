//package com.lime.mukbbomap.util;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.within;
//
//import ch.hsr.geohash.WGS84Point;
//import com.lime.mukbbomap.repository.RestaurantRepository;
//import java.util.List;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest
//@TestPropertySource(properties = {
//        "spring.datasource.url=jdbc:h2:mem:testdb",
//        "spring.datasource.driver-class-name=org.h2.Driver"
//})
//class GeoHashUtilTest {
//    @Autowired
//    RestaurantRepository restaurantRepository;
//
//    @Autowired
//    private GeoHashUtil geoHashUtil;
//
//    @Test
//    @Transactional
//        //테스트 데이터 삽입을 위한 코드
//    void insertTestData() {
////        Random random = new Random();
////        double baseLat = 37.4979;
////        double baseLon = 127.0276;
////
////        List<Restaurant> restaurants = new ArrayList<>();
////
////        for (int i = 1; i < 2001; i++) {
////            //5km
//////            double latitude = baseLat + (random.nextDouble() * 0.09 - 0.045);
//////            double longitude = baseLon + (random.nextDouble() * 0.114 - 0.057);
////
////            double latitude = baseLat + (random.nextDouble() * 0.18 - 0.09);
////            double longitude = baseLon + (random.nextDouble() * 0.228 - 0.114);
////            String geohash = GeoHash.geoHashStringWithCharacterPrecision(latitude, longitude, 7);
////
////            Restaurant restaurant = Restaurant.builder()
////                    .name("레스토랑" + i)
////                    .category("양식")
////                    .description("테스트 요리 전문점" + i)
////                    .address("서울 강남구 테스트주소" + i)
////                    .latitude(latitude)
////                    .longitude(longitude)
////                    .geohash(geohash)
////                    .phoneNumber(String.format("02-1234-56%02d", i % 100))
////                    .rating(4.3)
////                    .build();
////
////            restaurants.add(restaurant);
////        }
////
////        restaurantRepository.saveAll(restaurants);
//    }
//
//    @Test
//    @DisplayName("위도 경도를 GeoHash로 인코딩")
//    void encode() {
//        // given
//        double latitude = 37.4979;
//        double longitude = 127.0276;
//
//        // when
//        String geohash = geoHashUtil.encode(latitude, longitude);
//
//        // then
//        assertThat(geohash).isNotNull();
//        assertThat(geohash).hasSize(7);
//        assertThat(geohash).startsWith("wydm");
//    }
//
//    @Test
//    @DisplayName("GeoHash를 위도 경도로 디코딩")
//    void decode() {
//        // given
//        String geohash = "wydm6gg";
//
//        // when
//        WGS84Point point = geoHashUtil.decode(geohash);
//
//        // then
//        assertThat(point.getLatitude()).isCloseTo(37.4979, within(0.01));
//        assertThat(point.getLongitude()).isCloseTo(127.0397, within(0.01));
//    }
//
//    @Test
//    @DisplayName("두 지점 간 거리 계산")
//    void calculateDistance() {
//        // given - 강남역과 역삼역
//        double lat1 = 37.4979;
//        double lon1 = 127.0276;
//        double lat2 = 37.5009;
//        double lon2 = 127.0341;
//
//        // when
//        double distance = geoHashUtil.calculateDistance(lat1, lon1, lat2, lon2);
//
//        // then
//        assertThat(distance).isGreaterThan(0);
//        assertThat(distance).isLessThan(1000); // 1km 이내
//    }
//
//    @Test
//    @DisplayName("반경에 따른 정밀도 결정")
//    void getPrecisionByRadius() {
//        // when & then
//        assertThat(geoHashUtil.getPrecisionByRadius(1000)).isEqualTo(6);
//        assertThat(geoHashUtil.getPrecisionByRadius(3000)).isEqualTo(5);
//        assertThat(geoHashUtil.getPrecisionByRadius(5000)).isEqualTo(5);
//    }
//
//    @Test
//    @DisplayName("이웃 GeoHash 영역 생성 (9개)")
//    void getNeighborGeohashes() {
//        // given
//        String centerGeohash = "wydm6g";
//
//        // when
//        List<String> neighbors = geoHashUtil.getNeighborGeohashes(centerGeohash);
//
//        // then
//        assertThat(neighbors).hasSize(9);
//        assertThat(neighbors).contains(centerGeohash);
//    }
//
//    @Test
//    @DisplayName("검색 영역 GeoHash prefix 생성")
//    void getSearchPrefixes() {
//        // given
//        double latitude = 37.4979;
//        double longitude = 127.0276;
//        int radius = 1000;
//
//        // when
//        List<String> prefixes = geoHashUtil.getSearchPrefixes(latitude, longitude, radius);
//
//        // then
//        assertThat(prefixes).hasSize(9);
//        assertThat(prefixes).allMatch(prefix -> prefix.length() == 6);
//    }
//}