package com.alaCarte.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagRequestDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    @NotBlank
    private String tagType;

    private String description;
    private String colorHex;
    private String iconUrl;
}
