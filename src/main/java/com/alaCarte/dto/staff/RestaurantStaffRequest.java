package com.alaCarte.dto.staff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantStaffRequest {

    @NotNull(message = "Restaurant ID is required")
    private UUID restaurantId;

    private String employeeCode;

    @NotBlank(message = "Staff role is required")
    private String staffRole;

    private String designation;

    private UUID reportingTo;

    private String phone;

    private String email;

    @NotBlank(message = "Username is required")
    private String userName;

    private String password; // Raw password, to be hashed

    private String status;

    private OffsetDateTime joinedAt;

    private OffsetDateTime leftAt;

    private Map<String, Object> shiftMeta;

    private Map<String, Object> permissions;

    private Map<String, Object> meta;

    private Boolean isActive;
}
