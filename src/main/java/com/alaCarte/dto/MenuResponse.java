package com.alaCarte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponse {
    private UUID id;
    private Long restaurantId;
    private String name;
    private String description;
    private String menuType;
    private Boolean isActive;
    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;
    private Integer displayOrder;
    private String coverImage;
    private Map<String, Object> meta;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}
