package com.lime.mukbbomap.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lime.mukbbomap.domain.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantDocument {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("category")
    private String category;

    @JsonProperty("description")
    private String description;

    @JsonProperty("address")
    private String address;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("geohash")
    private String geohash;

    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @JsonProperty("rating")
    private Double rating;

    public static RestaurantDocument from(Restaurant restaurant) {
        return RestaurantDocument.builder()
                .id(restaurant.getId().toString())
                .name(restaurant.getName())
                .category(restaurant.getCategory())
                .description(restaurant.getDescription())
                .address(restaurant.getAddress())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .geohash(restaurant.getGeohash())
                .phoneNumber(restaurant.getPhoneNumber())
                .rating(restaurant.getRating())
                .build();
    }
}