package com.alaCarte.menu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemFilterParams {

    private UUID categoryId;
    private Boolean isAvailable;
    private Boolean isVeg;
    private String search;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String badge;
    private String tag;
    private Boolean hasVariants;
    private Boolean hasAddons;
}
