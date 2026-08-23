//package com.lime.mukbbomap.service;
//
//import com.lime.mukbbomap.dto.RestaurantDto;
//import com.lime.mukbbomap.dto.RestaurantDto.Response;
//import com.lime.mukbbomap.repository.RestaurantRepository;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@Transactional
//@TestPropertySource(properties = {
//        "spring.datasource.url=jdbc:h2:mem:testdb",
//        "spring.datasource.driver-class-name=org.h2.Driver",
//        "spring.jpa.hibernate.ddl-auto=create-drop"
//})
//class RestaurantServiceTest {
//
//    @Autowired
//    private RestaurantService restaurantService;
//
//    @Autowired
//    private RestaurantRepository restaurantRepository;
//
//    @BeforeEach
//    void setUp() {
//        restaurantRepository.deleteAll();
//    }
//
//    @Test
//    @DisplayName("맛집 등록 성공")
//    void createRestaurant() {
//        // given
//        RestaurantDto.CreateRequest request = new RestaurantDto.CreateRequest(
//                "테스트 맛집",
//                "한식",
//                "맛있는 한식",
//                "서울 강남구 테스트로 123",
//                37.4979,
//                127.0276,
//                "02-1234-5678",
//                4.5
//        );
//        // when
//        RestaurantDto.Response response = restaurantService.createRestaurant(request);
//
//        // then
//        assertThat(response.id()).isNotNull();
//        assertThat(response.name()).isEqualTo("테스트 맛집");
//        assertThat(response.geohash()).isNotNull();
//        assertThat(response.geohash()).startsWith("wydm");
//    }
//
//    @Test
//    @DisplayName("반경 1km 내 맛집 검색")
//    void searchNearbyRestaurants_1km() {
//        // given
//        createTestRestaurant("강남역 맛집", 37.4979, 127.0276, "한식");
//        createTestRestaurant("역삼역 맛집", 37.5009, 127.0341, "양식");
//        createTestRestaurant("선릉역 맛집", 37.5045, 127.0490, "일식");
//
//        RestaurantDto.SearchRequest request = new RestaurantDto.SearchRequest(
//                37.4979,
//                127.0276,
//                1000,
//                null
//        );
//
//        // when
//        List<Response> results = restaurantService.searchNearbyRestaurants(request);
//
//        // then
//        assertThat(results).isNotEmpty();
//        assertThat(results).hasSizeGreaterThan(0);
//    }
//
//    @Test
//    @DisplayName("카테고리 필터링 검색")
//    void searchNearbyRestaurants_withCategory() {
//        // given
//        createTestRestaurant("한식당1", 37.4979, 127.0276, "한식");
//        createTestRestaurant("한식당2", 37.4985, 127.0280, "한식");
//        createTestRestaurant("양식당", 37.4975, 127.0270, "양식");
//
//        RestaurantDto.SearchRequest request = new RestaurantDto.SearchRequest(
//                37.4979,
//                127.0276,
//                1000,
//                "한식"
//        );
//
//        // when
//        List<RestaurantDto.Response> results = restaurantService.searchNearbyRestaurants(request);
//
//        // then
//        assertThat(results).allMatch(r -> r.category().equals("한식"));
//    }
//
//    @Test
//    @DisplayName("맛집 삭제")
//    void deleteRestaurant() {
//        // given
//        RestaurantDto.Response created = createTestRestaurant("삭제될 맛집", 37.4979, 127.0276, "한식");
//
//        // when
//        restaurantService.deleteRestaurant(created.id());
//
//        // then
//        assertThat(restaurantRepository.findById(created.id())).isEmpty();
//    }
//
//    private RestaurantDto.Response createTestRestaurant(String name, double lat, double lon, String category) {
//        RestaurantDto.CreateRequest request = new RestaurantDto.CreateRequest(
//                name,
//                category,
//                "테스트 설명",
//                "서울 강남구 테스트로 123",
//                lat,
//                lon,
//                "02-1234-5678",
//                4.0
//        );
//        return restaurantService.createRestaurant(request);
//    }
//}