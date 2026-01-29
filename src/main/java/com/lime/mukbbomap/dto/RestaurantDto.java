package com.lime.mukbbomap.dto;

import com.lime.mukbbomap.domain.Restaurant;
import java.io.Serializable;
import java.time.LocalDateTime;

public class RestaurantDto {
    public record CreateRequest(
            String name,
            String category,
            String description,
            String address,
            Double latitude,
            Double longitude,
            String phoneNumber,
            Double rating
    ) {}

    public record Response(
            Long id,
            String name,
            String category,
            String description,
            String address,
            Double latitude,
            Double longitude,
            String geohash,
            String phoneNumber,
            Double rating,
            Double distance, // 검색 위치로부터의 거리 (미터)
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) implements Serializable {

        public static Response from(Restaurant restaurant) {
            return new Response(
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getCategory(),
                    restaurant.getDescription(),
                    restaurant.getAddress(),
                    restaurant.getLatitude(),
                    restaurant.getLongitude(),
                    restaurant.getGeohash(),
                    restaurant.getPhoneNumber(),
                    restaurant.getRating(),
                    null,
                    restaurant.getCreatedAt(),
                    restaurant.getUpdatedAt()
            );
        }

        public static Response from(Restaurant restaurant, Double distance) {
            return new Response(
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getCategory(),
                    restaurant.getDescription(),
                    restaurant.getAddress(),
                    restaurant.getLatitude(),
                    restaurant.getLongitude(),
                    restaurant.getGeohash(),
                    restaurant.getPhoneNumber(),
                    restaurant.getRating(),
                    distance,
                    restaurant.getCreatedAt(),
                    restaurant.getUpdatedAt()
            );
        }
    }

    public record SearchRequest(
            Double latitude,
            Double longitude,
            Integer radiusInMeters,
            String category
    ) {}
}