package com.alaCarte.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboRequestDTO {

    @NotBlank
    private String name;
    private String description;
    private String aiDescription;
    private String imageUrl;

    @NotNull
    @Positive
    private BigDecimal comboPrice;

    private BigDecimal originalPrice;
    private BigDecimal discountPrice;
    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;
    private List<String> availableDays;
    private Integer maxQuantityPerOrder;
    private String badge;
    private Integer displayOrder;
}
