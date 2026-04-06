package com.alaCarte.menu.dto.response;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDetailResponseDTO {

    private UUID id;
    private UUID menuId;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String description;
    private String aiDescription;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private Boolean isAvailable;
    private Boolean isVeg;
    private Boolean isVegan;
    private Boolean isSpicy;
    private Boolean containsEgg;
    private Boolean containsGluten;
    private Boolean containsNuts;
    private Boolean containsDairy;
    private Integer spiceLevel;
    private Integer prepTimeMinutes;
    private String imageUrl;
    private List<String> gallery;
    private List<String> ingredients;
    private String cuisineType;
    private String courseType;
    private String badge;
    private Boolean hasVariants;
    private Boolean hasAddons;
    private Integer displayOrder;
    private Map<String, Object> nutrition;
    private Map<String, Object> meta;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<MenuItemVariantResponseDTO> variants;
    private List<AddonGroupDetailResponseDTO> addonGroups;
    private List<TagResponseDTO> tags;
}
