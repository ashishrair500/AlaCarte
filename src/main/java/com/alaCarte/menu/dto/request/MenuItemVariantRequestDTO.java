package com.alaCarte.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemVariantRequestDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String variantType;

    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    private BigDecimal discountPrice;
    private Boolean isDefault;
    private Integer displayOrder;
}
