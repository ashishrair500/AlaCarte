package com.alaCarte.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboItemResponseDTO {

    private UUID id;
    private UUID comboId;
    private UUID menuItemId;
    private String menuItemName;
    private String menuItemImage;
    private UUID variantId;
    private String variantName;
    private Integer quantity;
    private Boolean isSubstitutable;
    private UUID substitutionCategoryId;
    private String substitutionCategoryName;
    private Integer displayOrder;
    private String note;
}
