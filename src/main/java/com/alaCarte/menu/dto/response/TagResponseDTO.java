package com.alaCarte.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponseDTO {

    private UUID id;
    private String name;
    private String slug;
    private String tagType;
    private String description;
    private String colorHex;
    private String iconUrl;
    private Boolean isRestaurantSpecific;
}
