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
public class AddonResponseDTO {

    private UUID id;
    private UUID addonGroupId;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean isAvailable;
    private Boolean isDefault;
    private Integer displayOrder;
}
