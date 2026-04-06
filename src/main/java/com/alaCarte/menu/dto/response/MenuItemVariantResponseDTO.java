package com.alaCarte.menu.dto.response;

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
public class MenuItemVariantResponseDTO {

    private UUID id;
    private UUID menuItemId;
    private String variantType;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Boolean isAvailable;
    private Boolean isDefault;
    private Integer displayOrder;
}
