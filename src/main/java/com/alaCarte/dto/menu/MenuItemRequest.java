package com.alaCarte.dto.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemRequest {

    @NotNull(message = "Restaurant ID is required")
    private UUID restaurantId;

    @NotNull(message = "Menu ID is required")
    private UUID menuId;

    @NotBlank(message = "Item name is required")
    private String name;

    private String description;

    private String category;

    @NotNull(message = "Price is required")
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
}
