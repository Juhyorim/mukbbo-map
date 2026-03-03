package com.lime.mukbbomap.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

@Getter
@Entity
@Table(name = "restaurant_location")
public class RestaurantLocation {
    @Id
    private Long restaurantId;

    @Column(nullable = false, columnDefinition = "POINT SRID 4326")
    private Point location;

    protected RestaurantLocation() {
    }

    public RestaurantLocation(Long restaurantId, Point location) {
        this.restaurantId = restaurantId;
        this.location = location;
    }

    public void updateLocation(Point location) {
        this.location = location;
    }
}