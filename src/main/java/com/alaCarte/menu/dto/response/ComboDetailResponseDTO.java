package com.alaCarte.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboDetailResponseDTO {

    private UUID id;
    private UUID menuId;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal comboPrice;
    private BigDecimal originalPrice;
    private BigDecimal discountPrice;
    private Boolean isAvailable;
    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;
    private List<String> availableDays;
    private String badge;
    private Integer displayOrder;
    private Integer slotCount;
    private String aiDescription;
    private Integer maxQuantityPerOrder;
    private List<ComboItemResponseDTO> items;
}
