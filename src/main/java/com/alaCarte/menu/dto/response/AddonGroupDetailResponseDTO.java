package com.alaCarte.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddonGroupDetailResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private String selectionType;
    private Integer minSelections;
    private Integer maxSelections;
    private Boolean isRequired;
    private Boolean isActive;
    private Integer displayOrder;
    private List<AddonResponseDTO> addons;
}
