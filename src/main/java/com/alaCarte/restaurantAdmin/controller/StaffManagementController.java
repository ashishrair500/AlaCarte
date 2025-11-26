package com.alaCarte.restaurantAdmin.controller;

import com.alaCarte.dto.ApiResponse;
import com.alaCarte.dto.RestaurantStaffRequest;
import com.alaCarte.dto.RestaurantStaffResponse;
import com.alaCarte.entity.RestaurantStaff;
import com.alaCarte.jwtUtils.JwtUtils;
import com.alaCarte.middlewares.RestaurantAdminMiddleware;
import com.alaCarte.service.RestaurantStaffService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for RestaurantAdmin to manage staff
 * RestaurantAdmin can perform CRUD operations on staff members
 */
@RestController
@RequestMapping("/api/restaurant-admin/staff")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StaffManagementController {

    private final RestaurantStaffService staffService;
    private final JwtUtils jwtUtils;
    private final RestaurantAdminMiddleware restaurantAdminMiddleware;
    private final PasswordEncoder passwordEncoder;

    public StaffManagementController(RestaurantStaffService staffService, JwtUtils jwtUtils,
                                     RestaurantAdminMiddleware restaurantAdminMiddleware,
                                     PasswordEncoder passwordEncoder) {
        this.staffService = staffService;
        this.jwtUtils = jwtUtils;
        this.restaurantAdminMiddleware = restaurantAdminMiddleware;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new staff member
     * POST /api/restaurant-admin/staff
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantStaffResponse>> createStaff(
            @Valid @RequestBody RestaurantStaffRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            // Convert DTO to Entity
            RestaurantStaff staff = mapToEntity(request);

            // Hash password if provided
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                staff.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }

            // Create staff
            RestaurantStaff savedStaff = staffService.createStaff(staff);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Staff created successfully", mapToResponse(savedStaff)));

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
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
     * Get all staff for a restaurant
     * GET /api/restaurant-admin/staff?restaurantId=...
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantStaffResponse>>> getStaffByRestaurant(
            @RequestParam Long restaurantId,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            List<RestaurantStaff> staffList = staffService.getStaffByRestaurantId(restaurantId);
            List<RestaurantStaffResponse> responses = staffList.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Staff retrieved successfully", responses));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve staff: " + e.getMessage()));
        }
    }

    /**
     * Get staff by ID
     * GET /api/restaurant-admin/staff/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantStaffResponse>> getStaffById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            RestaurantStaff staff = staffService.getStaffById(id)
                    .orElseThrow(() -> new RuntimeException("Staff not found"));

            return ResponseEntity.ok(ApiResponse.success("Staff retrieved successfully", mapToResponse(staff)));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
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
     * Update staff
     * PUT /api/restaurant-admin/staff/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantStaffResponse>> updateStaff(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantStaffRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            RestaurantStaff staffUpdates = mapToEntity(request);

            // Hash password if provided
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                staffUpdates.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }

            RestaurantStaff updatedStaff = staffService.updateStaff(id, staffUpdates);

            return ResponseEntity.ok(ApiResponse.success("Staff updated successfully", mapToResponse(updatedStaff)));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
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
     * Partial update (patch) staff
     * PATCH /api/restaurant-admin/staff/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantStaffResponse>> patchStaff(
            @PathVariable UUID id,
            @RequestBody RestaurantStaffRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            RestaurantStaff updates = mapToEntity(request);

            // Hash password if provided
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                updates.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }

            RestaurantStaff updatedStaff = staffService.patchStaff(id, updates);

            return ResponseEntity.ok(ApiResponse.success("Staff updated successfully", mapToResponse(updatedStaff)));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
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
     * Delete staff
     * DELETE /api/restaurant-admin/staff/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            staffService.deleteStaff(id);
            return ResponseEntity.ok(ApiResponse.success("Staff deleted successfully", null));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
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
     * Update staff status
     * PATCH /api/restaurant-admin/staff/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RestaurantStaffResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            RestaurantStaff staff = staffService.updateStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Status updated successfully", mapToResponse(staff)));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
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

    // Helper methods to map between Entity and DTO
    private RestaurantStaff mapToEntity(RestaurantStaffRequest request) {
        RestaurantStaff staff = new RestaurantStaff();
        staff.setRestaurantId(request.getRestaurantId());
        staff.setEmployeeCode(request.getEmployeeCode());
        staff.setStaffRole(request.getStaffRole());
        staff.setDesignation(request.getDesignation());
        staff.setReportingTo(request.getReportingTo());
        staff.setPhone(request.getPhone());
        staff.setEmail(request.getEmail());
        staff.setUserName(request.getUserName());
        // Password hash is handled separately
        staff.setStatus(request.getStatus());
        staff.setJoinedAt(request.getJoinedAt());
        staff.setLeftAt(request.getLeftAt());
        staff.setShiftMeta(request.getShiftMeta());
        staff.setPermissions(request.getPermissions());
        staff.setMeta(request.getMeta());
        staff.setIsActive(request.getIsActive());
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
