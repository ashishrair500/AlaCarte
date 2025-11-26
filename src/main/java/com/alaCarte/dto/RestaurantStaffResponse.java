package com.alaCarte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantStaffResponse {
    private UUID id;
    private Long restaurantId;
    private String employeeCode;
    private String staffRole;
    private String designation;
    private UUID reportingTo;
    private String phone;
    private String email;
    private String userName;
    private String status;
    private OffsetDateTime joinedAt;
    private OffsetDateTime leftAt;
    private Map<String, Object> shiftMeta;
    private Map<String, Object> permissions;
    private Map<String, Object> meta;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
}
