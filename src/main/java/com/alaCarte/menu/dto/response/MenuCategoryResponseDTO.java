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
public class MenuCategoryResponseDTO {

    private UUID id;
    private UUID menuId;
    private String name;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isActive;
    private UUID parentCategoryId;
    private String parentCategoryName;
    private Integer itemCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
