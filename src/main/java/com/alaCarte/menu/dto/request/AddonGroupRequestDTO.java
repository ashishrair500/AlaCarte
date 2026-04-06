package com.alaCarte.menu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddonGroupRequestDTO {

    @NotBlank
    private String name;
    private String description;

    @NotBlank
    private String selectionType;

    @Min(0)
    private Integer minSelections;

    private Integer maxSelections;
    private Integer displayOrder;
}
