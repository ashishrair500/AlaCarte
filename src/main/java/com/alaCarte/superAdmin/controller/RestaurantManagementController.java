package com.alaCarte.superAdmin.controller;

import com.alaCarte.dto.common.ApiResponse;
import com.alaCarte.dto.restaurant.RestaurantRequest;
import com.alaCarte.dto.restaurant.RestaurantResponse;
import com.alaCarte.dto.staff.RestaurantStaffRequest;
import com.alaCarte.dto.staff.RestaurantStaffResponse;
import com.alaCarte.entity.Restaurant;
import com.alaCarte.entity.RestaurantStaff;
import com.alaCarte.jwtUtils.JwtUtils;
import com.alaCarte.middlewares.SuperAdminMiddleware;
import com.alaCarte.service.RestaurantService;
import com.alaCarte.service.RestaurantStaffService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.alaCarte.enums.UserRoles.RESTAURANT_ADMIN;

/**
 * Controller for SuperAdmin to manage restaurant onboarding and profiles
 * SuperAdmin can perform full CRUD operations on restaurants
 */
@RestController
@RequestMapping("/api/superadmin/restaurants")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RestaurantManagementController {

    private final RestaurantService restaurantService;
    private final RestaurantStaffService restaurantStaffService;
    private final JwtUtils jwtUtils;
    private final SuperAdminMiddleware superAdminMiddleware;
    private final PasswordEncoder passwordEncoder;

    public RestaurantManagementController(
            RestaurantService restaurantService,
            RestaurantStaffService restaurantStaffService,
            JwtUtils jwtUtils,
            SuperAdminMiddleware middleware,
            PasswordEncoder passwordEncoder) {
        this.restaurantService = restaurantService;
        this.restaurantStaffService = restaurantStaffService;
        this.jwtUtils = jwtUtils;
        this.superAdminMiddleware = middleware;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new restaurant
     * POST /api/superadmin/restaurants
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantResponse>> createRestaurant(
            @Valid @RequestBody RestaurantRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            // Extract SuperAdmin ID from token
            String jwtToken = token.replace("Bearer ", "");
            UUID superAdminId = jwtUtils.getUserIdFromToken(jwtToken)
                    .orElseThrow(() -> new RuntimeException("Invalid token"));

            // Convert DTO to Entity
            Restaurant restaurant = mapToEntity(request);

            // Create restaurant
            Restaurant savedRestaurant = restaurantService.createRestaurant(restaurant, superAdminId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Restaurant created successfully", mapToResponse(savedRestaurant)));

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
                    .body(ApiResponse.error("Failed to create restaurant: " + e.getMessage()));
        }
    }

    /**
     * Onboard first restaurant admin staff for any restaurant.
     * POST /api/superadmin/restaurants/staff/onboard-restaurant-admin
     */
    @PostMapping("/staff/onboard-restaurant-admin")
    public ResponseEntity<ApiResponse<RestaurantStaffResponse>> onboardRestaurantAdmin(
            @Valid @RequestBody RestaurantStaffRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            superAdminMiddleware.validateSuperAdmin(token);

            if (request.getPassword() == null || request.getPassword().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Password is required"));
            }

            RestaurantStaff staff = new RestaurantStaff();
            staff.setRestaurantId(request.getRestaurantId());
            staff.setEmployeeCode(request.getEmployeeCode());
            // Force onboarding role as requested
            staff.setStaffRole(RESTAURANT_ADMIN.toString());
            staff.setDesignation(request.getDesignation() != null ? request.getDesignation() : "Restaurant Admin");
            staff.setReportingTo(request.getReportingTo());
            staff.setPhone(request.getPhone());
            staff.setEmail(request.getEmail());
            staff.setUserName(request.getUserName());
            staff.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            staff.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
            staff.setJoinedAt(request.getJoinedAt() != null ? request.getJoinedAt() : OffsetDateTime.now());
            staff.setLeftAt(request.getLeftAt());
            staff.setShiftMeta(request.getShiftMeta());
            staff.setPermissions(request.getPermissions());
            staff.setMeta(request.getMeta());
            staff.setIsActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE);

            RestaurantStaff saved = restaurantStaffService.createStaff(staff);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Restaurant admin onboarded successfully", mapToStaffResponse(saved)));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to onboard restaurant admin: " + e.getMessage()));
        }
    }

    /**
     * Update restaurant staff
     * PUT /api/superadmin/restaurants/staff/{id}
     */
    @PutMapping("/staff/{id}")
    public ResponseEntity<ApiResponse<RestaurantStaffResponse>> updateStaff(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantStaffRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            // Convert to Entity
            RestaurantStaff staffUpdates = new RestaurantStaff();
            staffUpdates.setRestaurantId(request.getRestaurantId());
            staffUpdates.setEmployeeCode(request.getEmployeeCode());
            staffUpdates.setStaffRole(request.getStaffRole());
            staffUpdates.setDesignation(request.getDesignation());
            staffUpdates.setReportingTo(request.getReportingTo());
            staffUpdates.setPhone(request.getPhone());
            staffUpdates.setEmail(request.getEmail());
            staffUpdates.setUserName(request.getUserName());
            staffUpdates.setStatus(request.getStatus());
            staffUpdates.setJoinedAt(request.getJoinedAt());
            staffUpdates.setLeftAt(request.getLeftAt());
            staffUpdates.setShiftMeta(request.getShiftMeta());
            staffUpdates.setPermissions(request.getPermissions());
            staffUpdates.setMeta(request.getMeta());
            staffUpdates.setIsActive(request.getIsActive());

            // Hash password if provided
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                staffUpdates.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }

            RestaurantStaff updatedStaff = restaurantStaffService.updateStaff(id, staffUpdates);

            return ResponseEntity.ok(ApiResponse.success("Staff updated successfully", mapToStaffResponse(updatedStaff)));
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
     * Partial update (patch) restaurant staff
     * PATCH /api/superadmin/restaurants/staff/{id}
     */
    @PatchMapping("/staff/{id}")
    public ResponseEntity<ApiResponse<RestaurantStaffResponse>> patchStaff(
            @PathVariable UUID id,
            @RequestBody RestaurantStaffRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            RestaurantStaff updates = new RestaurantStaff();
            updates.setRestaurantId(request.getRestaurantId());
            updates.setEmployeeCode(request.getEmployeeCode());
            updates.setStaffRole(request.getStaffRole());
            updates.setDesignation(request.getDesignation());
            updates.setReportingTo(request.getReportingTo());
            updates.setPhone(request.getPhone());
            updates.setEmail(request.getEmail());
            updates.setUserName(request.getUserName());
            updates.setStatus(request.getStatus());
            updates.setJoinedAt(request.getJoinedAt());
            updates.setLeftAt(request.getLeftAt());
            updates.setShiftMeta(request.getShiftMeta());
            updates.setPermissions(request.getPermissions());
            updates.setMeta(request.getMeta());
            updates.setIsActive(request.getIsActive());

            // Hash password if provided
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                updates.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }

            RestaurantStaff updatedStaff = restaurantStaffService.patchStaff(id, updates);

            return ResponseEntity.ok(ApiResponse.success("Staff patched successfully", mapToStaffResponse(updatedStaff)));
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
                    .body(ApiResponse.error("Failed to patch staff: " + e.getMessage()));
        }
    }

    /**
     * Get all restaurants
     * GET /api/superadmin/restaurants
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAllRestaurants(
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            List<Restaurant> restaurants = restaurantService.getAllRestaurants();
            List<RestaurantResponse> responses = restaurants.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Restaurants retrieved successfully", responses));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve restaurants: " + e.getMessage()));
        }
    }

    /**
     * Get restaurant by ID
     * GET /api/superadmin/restaurants/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getRestaurantById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            Restaurant restaurant = restaurantService.getRestaurantById(id)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));

            return ResponseEntity.ok(ApiResponse.success("Restaurant retrieved successfully", mapToResponse(restaurant)));
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
                    .body(ApiResponse.error("Failed to retrieve restaurant: " + e.getMessage()));
        }
    }

    /**
     * Search restaurants by name
     * GET /api/superadmin/restaurants/search?name=...
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> searchRestaurants(
            @RequestParam String name,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            List<Restaurant> restaurants = restaurantService.searchByName(name);
            List<RestaurantResponse> responses = restaurants.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Search results", responses));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }

    /**
     * Advanced search with multiple filters
     * GET /api/superadmin/restaurants/advanced-search
     */
    @GetMapping("/advanced-search")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> advancedSearch(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String restaurantType,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) String searchTerm,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            List<Restaurant> restaurants = restaurantService.searchRestaurants(
                    city, status, restaurantType, minRating, searchTerm);
            List<RestaurantResponse> responses = restaurants.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Search results", responses));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Advanced search failed: " + e.getMessage()));
        }
    }

    /**
     * Get restaurants by city
     * GET /api/superadmin/restaurants/by-city/{city}
     */
    @GetMapping("/by-city/{city}")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getRestaurantsByCity(
            @PathVariable String city,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            List<Restaurant> restaurants = restaurantService.getRestaurantsByCity(city);
            List<RestaurantResponse> responses = restaurants.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Restaurants in " + city, responses));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve restaurants: " + e.getMessage()));
        }
    }

    /**
     * Get restaurants by status
     * GET /api/superadmin/restaurants/by-status/{status}
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getRestaurantsByStatus(
            @PathVariable String status,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            List<Restaurant> restaurants = restaurantService.getRestaurantsByStatus(status);
            List<RestaurantResponse> responses = restaurants.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Restaurants with status: " + status, responses));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve restaurants: " + e.getMessage()));
        }
    }

    /**
     * Get popular restaurants
     * GET /api/superadmin/restaurants/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getPopularRestaurants(
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            List<Restaurant> restaurants = restaurantService.getPopularRestaurants();
            List<RestaurantResponse> responses = restaurants.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Popular restaurants", responses));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve popular restaurants: " + e.getMessage()));
        }
    }

    /**
     * Update restaurant
     * PUT /api/superadmin/restaurants/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateRestaurant(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            Restaurant restaurant = mapToEntity(request);
            Restaurant updatedRestaurant = restaurantService.updateRestaurant(id, restaurant);

            return ResponseEntity.ok(ApiResponse.success("Restaurant updated successfully", mapToResponse(updatedRestaurant)));
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
                    .body(ApiResponse.error("Failed to update restaurant: " + e.getMessage()));
        }
    }

    /**
     * Partial update (patch) restaurant
     * PATCH /api/superadmin/restaurants/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> patchRestaurant(
            @PathVariable UUID id,
            @RequestBody RestaurantRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            Restaurant updates = mapToEntity(request);
            Restaurant updatedRestaurant = restaurantService.patchRestaurant(id, updates);

            return ResponseEntity.ok(ApiResponse.success("Restaurant updated successfully", mapToResponse(updatedRestaurant)));
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
                    .body(ApiResponse.error("Failed to update restaurant: " + e.getMessage()));
        }
    }

    /**
     * Update restaurant status
     * PATCH /api/superadmin/restaurants/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            Restaurant restaurant = restaurantService.updateStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Status updated successfully", mapToResponse(restaurant)));
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

    /**
     * Toggle popular status
     * PATCH /api/superadmin/restaurants/{id}/toggle-popular
     */
    @PatchMapping("/{id}/toggle-popular")
    public ResponseEntity<ApiResponse<RestaurantResponse>> togglePopular(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            Restaurant restaurant = restaurantService.togglePopular(id);
            return ResponseEntity.ok(ApiResponse.success("Popular status toggled", mapToResponse(restaurant)));
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
                    .body(ApiResponse.error("Failed to toggle popular status: " + e.getMessage()));
        }
    }

    /**
     * Delete restaurant
     * DELETE /api/superadmin/restaurants/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            restaurantService.deleteRestaurant(id);
            return ResponseEntity.ok(ApiResponse.success("Restaurant deleted successfully", null));
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
                    .body(ApiResponse.error("Failed to delete restaurant: " + e.getMessage()));
        }
    }

    /**
     * Get statistics
     * GET /api/superadmin/restaurants/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<java.util.Map<String, Long>>> getStatistics(
            @RequestHeader("Authorization") String token) {
        try {
            // Validate SuperAdmin access
            superAdminMiddleware.validateSuperAdmin(token);

            java.util.Map<String, Long> stats = new java.util.HashMap<>();
            stats.put("total", restaurantService.countRestaurants());
            stats.put("open", restaurantService.countByStatus("open"));
            stats.put("closed", restaurantService.countByStatus("closed"));
            stats.put("temp_closed", restaurantService.countByStatus("temp_closed"));

            return ResponseEntity.ok(ApiResponse.success("Statistics retrieved", stats));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve statistics: " + e.getMessage()));
        }
    }

    // Helper methods to map between Entity and DTO
    private Restaurant mapToEntity(RestaurantRequest request) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddressLine1(request.getAddressLine1());
        restaurant.setAddressLine2(request.getAddressLine2());
        restaurant.setCity(request.getCity());
        restaurant.setState(request.getState());
        restaurant.setPincode(request.getPincode());
        restaurant.setLatitude(request.getLatitude());
        restaurant.setLongitude(request.getLongitude());
        restaurant.setPhone(request.getPhone());
        restaurant.setEmail(request.getEmail());
        restaurant.setCuisineTypes(request.getCuisineTypes());
        restaurant.setPriceRange(request.getPriceRange());
        restaurant.setAvgCostForTwo(request.getAvgCostForTwo());
        restaurant.setOpeningTime(request.getOpeningTime());
        restaurant.setClosingTime(request.getClosingTime());
        restaurant.setIsVegOnly(request.getIsVegOnly());
        restaurant.setHasParking(request.getHasParking());
        restaurant.setIsPopular(request.getIsPopular());
        restaurant.setRestaurantType(request.getRestaurantType());
        restaurant.setHotelStarCategory(request.getHotelStarCategory());
        restaurant.setServiceFee(request.getServiceFee());
        restaurant.setReservationRequired(request.getReservationRequired());
        restaurant.setDressCode(request.getDressCode());
        restaurant.setMenuQrUrl(request.getMenuQrUrl());
        restaurant.setFeaturedImageUrl(request.getFeaturedImageUrl());
        restaurant.setGalleryImages(request.getGalleryImages());
        restaurant.setStatus(request.getStatus());
        return restaurant;
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .addressLine1(restaurant.getAddressLine1())
                .addressLine2(restaurant.getAddressLine2())
                .city(restaurant.getCity())
                .state(restaurant.getState())
                .pincode(restaurant.getPincode())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .cuisineTypes(restaurant.getCuisineTypes())
                .priceRange(restaurant.getPriceRange())
                .avgCostForTwo(restaurant.getAvgCostForTwo())
                .openingTime(restaurant.getOpeningTime())
                .closingTime(restaurant.getClosingTime())
                .rating(restaurant.getRating())
                .totalRatings(restaurant.getTotalRatings())
                .isVegOnly(restaurant.getIsVegOnly())
                .hasParking(restaurant.getHasParking())
                .isPopular(restaurant.getIsPopular())
                .restaurantType(restaurant.getRestaurantType())
                .hotelStarCategory(restaurant.getHotelStarCategory())
                .serviceFee(restaurant.getServiceFee())
                .reservationRequired(restaurant.getReservationRequired())
                .dressCode(restaurant.getDressCode())
                .menuQrUrl(restaurant.getMenuQrUrl())
                .featuredImageUrl(restaurant.getFeaturedImageUrl())
                .galleryImages(restaurant.getGalleryImages())
                .status(restaurant.getStatus())
                .createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt())
                .createdBy(restaurant.getCreatedBy())
                .build();
    }

    private RestaurantStaffResponse mapToStaffResponse(RestaurantStaff staff) {
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
