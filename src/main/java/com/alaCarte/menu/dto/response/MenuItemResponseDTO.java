package com.alaCarte.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponseDTO {

    private UUID id;
    private UUID menuId;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private Boolean isAvailable;
    private Boolean isVeg;
    private Boolean isVegan;
    private Boolean isSpicy;
    private Integer spiceLevel;
    private String imageUrl;
    private String badge;
    private Boolean hasVariants;
    private Boolean hasAddons;
    private Integer displayOrder;
    private String courseType;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
