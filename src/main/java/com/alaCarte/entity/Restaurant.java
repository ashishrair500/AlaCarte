package com.alaCarte.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a Restaurant in the database
 * Supports various restaurant types including cafes, fine dining, and hotels
 */
@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "user_name", nullable = false, length = 40)
    private String userName;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String password;

    // Address fields
    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 20)
    private String pincode;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    // Contact information
    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    // Restaurant details
    @Column(name = "cuisine_types", columnDefinition = "TEXT[]")
    private String[] cuisineTypes;

    @Column(name = "price_range")
    private Integer priceRange; // 1-3 range

    @Column(name = "avg_cost_for_two", precision = 10, scale = 2)
    private BigDecimal avgCostForTwo;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "total_ratings")
    private Integer totalRatings = 0;

    // Restaurant features
    @Column(name = "is_veg_only")
    private Boolean isVegOnly = false;

    @Column(name = "has_parking")
    private Boolean hasParking = false;

    @Column(name = "is_popular")
    private Boolean isPopular = false;

    @Column(name = "restaurant_type", length = 50)
    private String restaurantType; // cafe, fine_dine, hotel, etc.

    @Column(name = "hotel_star_category")
    private Integer hotelStarCategory; // null for non-hotels

    @Column(name = "service_fee", precision = 10, scale = 2)
    private BigDecimal serviceFee;

    @Column(name = "reservation_required")
    private Boolean reservationRequired = false;

    @Column(name = "dress_code", length = 100)
    private String dressCode;

    // URLs and media
    @Column(name = "menu_qr_url", columnDefinition = "TEXT")
    private String menuQrUrl;

    @Column(name = "featured_image_url", columnDefinition = "TEXT")
    private String featuredImageUrl;

    @Column(name = "gallery_images", columnDefinition = "TEXT[]")
    private String[] galleryImages;

    // Status and timestamps
    @Column(length = 30)
    private String status = "open"; // open, closed, temp_closed

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (totalRatings == null) {
            totalRatings = 0;
        }
        if (isVegOnly == null) {
            isVegOnly = false;
        }
        if (hasParking == null) {
            hasParking = false;
        }
        if (isPopular == null) {
            isPopular = false;
        }
        if (reservationRequired == null) {
            reservationRequired = false;
        }
        if (status == null) {
            status = "open";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

