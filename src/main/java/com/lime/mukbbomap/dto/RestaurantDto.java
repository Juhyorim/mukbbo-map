package com.lime.mukbbomap.dto;


import com.lime.mukbbomap.domain.Restaurant;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class RestaurantDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {

        @NotBlank(message = "맛집 이름은 필수입니다")
        @Size(max = 100, message = "맛집 이름은 100자 이내여야 합니다")
        private String name;

        @NotBlank(message = "카테고리는 필수입니다")
        @Size(max = 50, message = "카테고리는 50자 이내여야 합니다")
        private String category;

        @Size(max = 500, message = "설명은 500자 이내여야 합니다")
        private String description;

        @NotBlank(message = "주소는 필수입니다")
        @Size(max = 200, message = "주소는 200자 이내여야 합니다")
        private String address;

        @NotNull(message = "위도는 필수입니다")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
        @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
        private Double latitude;

        @NotNull(message = "경도는 필수입니다")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
        @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
        private Double longitude;

        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",
                message = "전화번호 형식이 올바르지 않습니다 (예: 02-1234-5678)")
        private String phoneNumber;

        @DecimalMin(value = "0.0", message = "평점은 0.0 이상이어야 합니다")
        @DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다")
        private Double rating;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response implements Serializable {
        private Long id;
        private String name;
        private String category;
        private String description;
        private String address;
        private Double latitude;
        private Double longitude;
        private String geohash;
        private String phoneNumber;
        private Double rating;
        private Double distance; // 검색 위치로부터의 거리 (미터)
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(Restaurant restaurant) {
            return Response.builder()
                    .id(restaurant.getId())
                    .name(restaurant.getName())
                    .category(restaurant.getCategory())
                    .description(restaurant.getDescription())
                    .address(restaurant.getAddress())
                    .latitude(restaurant.getLatitude())
                    .longitude(restaurant.getLongitude())
                    .geohash(restaurant.getGeohash())
                    .phoneNumber(restaurant.getPhoneNumber())
                    .rating(restaurant.getRating())
                    .createdAt(restaurant.getCreatedAt())
                    .updatedAt(restaurant.getUpdatedAt())
                    .build();
        }

        public static Response from(Restaurant restaurant, Double distance) {
            Response response = from(restaurant);
            response.distance = distance;
            return response;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchRequest {

        @NotNull(message = "위도는 필수입니다")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
        @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
        private Double latitude;

        @NotNull(message = "경도는 필수입니다")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
        @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
        private Double longitude;

        @NotNull(message = "반경은 필수입니다")
        @Min(value = 100, message = "반경은 최소 100m 이상이어야 합니다")
        @Max(value = 10000, message = "반경은 최대 10km 이하여야 합니다")
        private Integer radiusInMeters; // 1000, 3000, 5000 등

        private String category; // 선택적 필터
    }
}

