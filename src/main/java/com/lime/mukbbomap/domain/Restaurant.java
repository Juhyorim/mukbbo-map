package com.lime.mukbbomap.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


@Entity
@Table(name = "restaurants", indexes = {
        @Index(name = "idx_geohash", columnList = "geohash"),
        @Index(name = "idx_latitude_longitude", columnList = "latitude, longitude")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false, length = 20)
    private String geohash;

    @Column(length = 20)
    private String phoneNumber;

    @Column
    private Double rating;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void updateLocation(Double latitude, Double longitude, String geohash) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.geohash = geohash;
    }

    public void updateInfo(String name, String category, String description,
                           String address, String phoneNumber) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }
}