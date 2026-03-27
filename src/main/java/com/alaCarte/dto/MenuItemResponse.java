package com.alaCarte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemResponse {
    private UUID id;
    private UUID restaurantId;
    private UUID menuId;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Boolean isAvailable;
    private Boolean isVeg;
    private Boolean isSpicy;
    private Boolean containsEgg;
    private List<Map<String, Object>> variants;
    private List<Map<String, Object>> addons;
    private String[] tags;
    private String imageUrl;
    private List<String> gallery;
    private Map<String, Object> nutrition;
    private Integer displayOrder;
    private String badge;
    private Map<String, Object> meta;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
}
