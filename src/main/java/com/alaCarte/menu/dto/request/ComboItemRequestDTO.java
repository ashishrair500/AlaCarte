package com.alaCarte.menu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboItemRequestDTO {

    @NotNull
    private UUID menuItemId;
    private UUID variantId;

    @Min(1)
    private Integer quantity;

    private Boolean isSubstitutable;
    private UUID substitutionCategoryId;
    private Integer displayOrder;
    private String note;
}
