package com.alaCarte.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryRequestDTO {

    @NotBlank
    private String name;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private UUID parentCategoryId;
}
