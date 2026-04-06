package com.alaCarte.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private String menuType;
    private Boolean isActive;
    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;
    private Integer displayOrder;
    private String coverImage;
    private Integer totalCategories;
    private Integer totalItems;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
