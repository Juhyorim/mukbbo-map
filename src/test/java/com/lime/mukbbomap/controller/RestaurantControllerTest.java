package com.lime.mukbbomap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lime.mukbbomap.dto.RestaurantDto;
import com.lime.mukbbomap.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    @Autowired
    private RestaurantRepository restaurantRepository;

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/restaurants - 맛집 등록 성공")
    void createRestaurant() throws Exception {
        // given
        RestaurantDto.CreateRequest request = new RestaurantDto.CreateRequest(
                "테스트 맛집",
                "한식",
                "맛있는 음식",
                "서울 강남구 테스트로 123",
                37.4979,
                127.0276,
                "02-1234-5678",
                4.5
        );

        // when & then
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("테스트 맛집"))
                .andExpect(jsonPath("$.category").value("한식"))
                .andExpect(jsonPath("$.geohash").exists())
                .andExpect(jsonPath("$.latitude").value(37.4979))
                .andExpect(jsonPath("$.longitude").value(127.0276));
    }

    @Test
    @DisplayName("POST /api/restaurants - 유효성 검증 실패")
    void createRestaurant_validationFail() throws Exception {
        // given - name이 없음
        RestaurantDto.CreateRequest request = new RestaurantDto.CreateRequest(
                "",
                "한식",
                "테스트 설명",
                "서울 강남구 테스트로 123",
                37.4979,
                127.0276,
                "02-1234-5678",
                4.0
        );

        // when & then
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/restaurants/nearby - 주변 맛집 검색")
    void searchNearbyRestaurants() throws Exception {
        // given
        createTestRestaurant("맛집1", 37.4979, 127.0276, "한식");
        createTestRestaurant("맛집2", 37.4985, 127.0280, "양식");

        // when & then
        mockMvc.perform(get("/api/restaurants/nearby")
                        .param("latitude", "37.4979")
                        .param("longitude", "127.0276")
                        .param("radiusInMeters", "1000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].geohash").exists());
    }

    @Test
    @DisplayName("GET /api/restaurants/nearby - 카테고리 필터링")
    void searchNearbyRestaurants_withCategory() throws Exception {
        // given
        createTestRestaurant("한식당", 37.4979, 127.0276, "한식");
        createTestRestaurant("양식당", 37.4985, 127.0280, "양식");

        // when & then
        mockMvc.perform(get("/api/restaurants/nearby")
                        .param("latitude", "37.4979")
                        .param("longitude", "127.0276")
                        .param("radiusInMeters", "1000")
                        .param("category", "한식"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].category", everyItem(is("한식"))));
    }

    @Test
    @DisplayName("GET /api/restaurants/{id} - 맛집 상세 조회")
    void getRestaurant() throws Exception {
        // given
        Long id = createTestRestaurant("조회할 맛집", 37.4979, 127.0276, "한식");

        // when & then
        mockMvc.perform(get("/api/restaurants/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("조회할 맛집"));
    }

    @Test
    @DisplayName("DELETE /api/restaurants/{id} - 맛집 삭제")
    void deleteRestaurant() throws Exception {
        // given
        Long id = createTestRestaurant("삭제할 맛집", 37.4979, 127.0276, "한식");

        // when & then
        mockMvc.perform(delete("/api/restaurants/{id}", id))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    private Long createTestRestaurant(String name, double lat, double lon, String category) throws Exception {
        RestaurantDto.CreateRequest request = new RestaurantDto.CreateRequest(
                name,
                category,
                "테스트 설명",
                "서울 강남구 테스트로 123",
                lat,
                lon,
                "02-1234-5678",
                4.0
        );

        String response = mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        RestaurantDto.Response created = objectMapper.readValue(response, RestaurantDto.Response.class);
        return created.id();
    }
}