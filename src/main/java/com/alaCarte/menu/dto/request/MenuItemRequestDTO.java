package com.alaCarte.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemRequestDTO {

    @NotBlank
    private String name;
    private String description;
    private String aiDescription;
    private UUID categoryId;

    @NotNull
    @Positive
    private BigDecimal basePrice;

    private BigDecimal discountPrice;
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
    private Integer displayOrder;
    private String badge;
    private Map<String, Object> nutrition;
    private Map<String, Object> meta;
}
