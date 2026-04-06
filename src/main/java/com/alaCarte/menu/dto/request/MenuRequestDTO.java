package com.alaCarte.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuRequestDTO {

    @NotBlank
    private String name;
    private String description;
    private String menuType;
    private Boolean isActive;
    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;
    private Integer displayOrder;
    private String coverImage;
}
