package com.alaCarte.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * DTO for creating/updating restaurant
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private String description;

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private String phone;

    @Email(message = "Email should be valid")
    private String email;

    private String[] cuisineTypes;

    @Min(value = 1, message = "Price range must be between 1 and 3")
    @Max(value = 3, message = "Price range must be between 1 and 3")
    private Integer priceRange;

    private BigDecimal avgCostForTwo;

    private LocalTime openingTime;
    private LocalTime closingTime;

    private Boolean isVegOnly = false;
    private Boolean hasParking = false;
    private Boolean isPopular = false;

    private String restaurantType; // cafe, fine_dine, hotel
    private Integer hotelStarCategory;

    private BigDecimal serviceFee;
    private Boolean reservationRequired = false;
    private String dressCode;

    private String menuQrUrl;
    private String featuredImageUrl;
    private String[] galleryImages;

    private String status = "open"; // open, closed, temp_closed
}

