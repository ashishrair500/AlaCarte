package com.alaCarte.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuRequest {

    @NotNull(message = "Restaurant ID is required")
    private UUID restaurantId;

    @NotBlank(message = "Menu name is required")
    private String name;

    private String description;

    private String menuType;

    private Boolean isActive;

    private OffsetDateTime validFrom;

    private OffsetDateTime validTo;

    private Integer displayOrder;

    private String coverImage;

    private Map<String, Object> meta;
}
