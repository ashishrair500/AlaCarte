package com.alaCarte.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddonRequestDTO {

    @NotBlank
    private String name;
    private String description;

    @NotNull
    @PositiveOrZero
    private BigDecimal price;

    private Boolean isDefault;
    private Integer displayOrder;
}
