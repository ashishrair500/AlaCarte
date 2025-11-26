package com.alaCarte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for restaurant response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantResponse {

    private UUID id;
    private String name;
    private String description;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private String phone;
    private String email;

    private String[] cuisineTypes;
    private Integer priceRange;
    private BigDecimal avgCostForTwo;

    private LocalTime openingTime;
    private LocalTime closingTime;

    private BigDecimal rating;
    private Integer totalRatings;

    private Boolean isVegOnly;
    private Boolean hasParking;
    private Boolean isPopular;

    private String restaurantType;
    private Integer hotelStarCategory;

    private BigDecimal serviceFee;
    private Boolean reservationRequired;
    private String dressCode;

    private String menuQrUrl;
    private String featuredImageUrl;
    private String[] galleryImages;

    private String status;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private UUID createdBy;
}

