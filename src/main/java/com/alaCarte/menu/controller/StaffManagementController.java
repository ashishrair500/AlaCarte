package com.alaCarte.menu.controller;

import com.alaCarte.dto.common.ApiResponse;
import com.alaCarte.dto.staff.RestaurantStaffRequest;
import com.alaCarte.dto.staff.RestaurantStaffResponse;
import com.alaCarte.entity.RestaurantStaff;
import com.alaCarte.menu.util.RequestContextUtil;
import com.alaCarte.service.RestaurantStaffService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for restaurant-admin staff management inside the v1 module.
 */
@RestController
@RequestMapping("/api/v1/restaurant-admin/staff")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StaffManagementController {

    private final RestaurantStaffService staffService;
    private final PasswordEncoder passwordEncoder;
    private final RequestContextUtil requestContextUtil;

    public StaffManagementController(
            RestaurantStaffService staffService,
            PasswordEncoder passwordEncoder,
            RequestContextUtil requestContextUtil
    ) {
        this.staffService = staffService;
        this.passwordEncoder = passwordEncoder;
        this.requestContextUtil = requestContextUtil;
    }

    /**
     * Create a new staff member.
     * POST /api/v1/restaurant-admin/staff
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantStaffResponse>> createStaff(
            @Valid @RequestBody RestaurantStaffRequest request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);

        try {
            RestaurantStaff staff = mapToEntity(request, restaurantId);

            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                staff.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }

            RestaurantStaff savedStaff = staffService.createStaff(staff);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Staff created successfully", mapToResponse(savedStaff)));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create staff: " + e.getMessage()));
        }
    }

    /**
     * Get all staff for the authenticated restaurant.
     * GET /api/v1/restaurant-admin/staff
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantStaffResponse>>> getStaffByRestaurant(
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);

        try {
            List<RestaurantStaff> staffList = staffService.getStaffByRestaurantId(restaurantId);
            List<RestaurantStaffResponse> responses = staffList.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Staff retrieved successfully", responses));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve staff: " + e.getMessage()));
        }
    }

    /**
     * Get staff by ID.
     * GET /api/v1/restaurant-admin/staff/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantStaffResponse>> getStaffById(
            @PathVariable UUID id,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);

        try {
            RestaurantStaff staff = getScopedStaff(id, restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Staff retrieved successfully", mapToResponse(staff)));
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve staff: " + e.getMessage()));
        }
    }

    /**
     * Update staff.
     * PUT /api/v1/restaurant-admin/staff/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantStaffResponse>> updateStaff(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantStaffRequest request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);

        try {
            getScopedStaff(id, restaurantId);

            RestaurantStaff staffUpdates = mapToEntity(request, restaurantId);
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                staffUpdates.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }

            RestaurantStaff updatedStaff = staffService.updateStaff(id, staffUpdates);
            return ResponseEntity.ok(ApiResponse.success("Staff updated successfully", mapToResponse(updatedStaff)));
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update staff: " + e.getMessage()));
        }
    }

    /**
     * Partial update staff.
     * PATCH /api/v1/restaurant-admin/staff/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantStaffResponse>> patchStaff(
            @PathVariable UUID id,
            @RequestBody RestaurantStaffRequest request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);

        try {
            getScopedStaff(id, restaurantId);

            RestaurantStaff updates = mapToEntity(request, restaurantId);
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                updates.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }

            RestaurantStaff updatedStaff = staffService.patchStaff(id, updates);
            return ResponseEntity.ok(ApiResponse.success("Staff updated successfully", mapToResponse(updatedStaff)));
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update staff: " + e.getMessage()));
        }
    }

    /**
     * Delete staff.
     * DELETE /api/v1/restaurant-admin/staff/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(
            @PathVariable UUID id,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);

        try {
            getScopedStaff(id, restaurantId);
            staffService.deleteStaff(id);
            return ResponseEntity.ok(ApiResponse.success("Staff deleted successfully", null));
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete staff: " + e.getMessage()));
        }
    }

    /**
     * Update staff status.
     * PATCH /api/v1/restaurant-admin/staff/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RestaurantStaffResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);

        try {
            getScopedStaff(id, restaurantId);
            RestaurantStaff staff = staffService.updateStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Status updated successfully", mapToResponse(staff)));
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update status: " + e.getMessage()));
        }
    }

    private RestaurantStaff mapToEntity(RestaurantStaffRequest request, UUID restaurantId) {
        RestaurantStaff staff = new RestaurantStaff();
        staff.setRestaurantId(restaurantId);
        staff.setEmployeeCode(request.getEmployeeCode());
        staff.setStaffRole(request.getStaffRole());
        staff.setDesignation(request.getDesignation());
        staff.setReportingTo(request.getReportingTo());
        staff.setPhone(request.getPhone());
        staff.setEmail(request.getEmail());
        staff.setUserName(request.getUserName());
        staff.setStatus(request.getStatus());
        staff.setJoinedAt(request.getJoinedAt());
        staff.setLeftAt(request.getLeftAt());
        staff.setShiftMeta(request.getShiftMeta());
        staff.setPermissions(request.getPermissions());
        staff.setMeta(request.getMeta());
        staff.setIsActive(request.getIsActive());
        return staff;
    }

    private RestaurantStaff getScopedStaff(UUID staffId, UUID restaurantId) {
        RestaurantStaff staff = staffService.getStaffById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        if (!restaurantId.equals(staff.getRestaurantId())) {
            throw new AccessDeniedException("Staff does not belong to this restaurant");
        }

        return staff;
    }

    private RestaurantStaffResponse mapToResponse(RestaurantStaff staff) {
        return RestaurantStaffResponse.builder()
                .id(staff.getId())
                .restaurantId(staff.getRestaurantId())
                .employeeCode(staff.getEmployeeCode())
                .staffRole(staff.getStaffRole())
                .designation(staff.getDesignation())
                .reportingTo(staff.getReportingTo())
                .phone(staff.getPhone())
                .email(staff.getEmail())
                .userName(staff.getUserName())
                .status(staff.getStatus())
                .joinedAt(staff.getJoinedAt())
                .leftAt(staff.getLeftAt())
                .shiftMeta(staff.getShiftMeta())
                .permissions(staff.getPermissions())
                .meta(staff.getMeta())
                .isActive(staff.getIsActive())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .deletedAt(staff.getDeletedAt())
                .build();
    }
}
