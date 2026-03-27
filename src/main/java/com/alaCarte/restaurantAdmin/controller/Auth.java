package com.alaCarte.restaurantAdmin.controller;

import com.alaCarte.dto.ApiResponse;
import com.alaCarte.dto.LoginRequest;
import com.alaCarte.dto.LoginResponse;
import com.alaCarte.entity.RestaurantStaff;
import com.alaCarte.jwtUtils.JwtUtils;
import com.alaCarte.service.RestaurantStaffService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Authentication controller for Restaurant Admin role.
 */
@RestController("restaurantAdminAuthController")
@RequestMapping("/api/auth/restaurant-admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class Auth {

    private static final String ROLE_RESTAURANT_ADMIN = "ROLE_RESTAURANT_ADMIN";

    private final RestaurantStaffService restaurantStaffService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public Auth(RestaurantStaffService restaurantStaffService, JwtUtils jwtUtils, PasswordEncoder passwordEncoder) {
        this.restaurantStaffService = restaurantStaffService;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Optional<RestaurantStaff> staffOpt =
                    restaurantStaffService.findByUsernameOrEmail(loginRequest.getUsernameOrEmail());

            if (staffOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid username/email or password"));
            }

            RestaurantStaff staff = staffOpt.get();
            if (!isEligibleRestaurantAdmin(staff)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("User is not eligible for restaurant-admin access"));
            }

            if (staff.getPasswordHash() == null ||
                    !passwordEncoder.matches(loginRequest.getPassword(), staff.getPasswordHash())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid username/email or password"));
            }

            String accessToken = jwtUtils.generateToken(
                    staff.getUserName(),
                    staff.getId(),
                    Collections.singletonList(ROLE_RESTAURANT_ADMIN),
                    createExtraClaims(staff)
            );
            String refreshToken = jwtUtils.generateRefreshToken(staff.getUserName(), staff.getId());

            LoginResponse response = LoginResponse.builder()
                    .userId(staff.getId())
                    .username(staff.getUserName())
                    .fullName(staff.getDesignation())
                    .email(staff.getEmail())
                    .permissions(staff.getPermissions())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400L)
                    .message("Login successful")
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(@RequestParam String refreshToken) {
        try {
            if (!jwtUtils.validateJwtToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid or expired refresh token"));
            }

            String username = jwtUtils.getUsernameFromToken(refreshToken);
            Optional<RestaurantStaff> staffOpt = restaurantStaffService.getStaffByUsername(username);
            if (staffOpt.isEmpty() || !isEligibleRestaurantAdmin(staffOpt.get())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("User not found or unauthorized"));
            }

            RestaurantStaff staff = staffOpt.get();
            String newAccessToken = jwtUtils.generateToken(
                    staff.getUserName(),
                    staff.getId(),
                    Collections.singletonList(ROLE_RESTAURANT_ADMIN),
                    createExtraClaims(staff)
            );

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            tokens.put("tokenType", "Bearer");
            tokens.put("expiresIn", "86400");

            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", tokens));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Token refresh failed: " + e.getMessage()));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(@RequestParam String token) {
        try {
            boolean isValid = jwtUtils.validateJwtToken(token);
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid or expired token"));
            }

            List<String> roles = jwtUtils.getRolesFromToken(token);
            if (!roles.contains(ROLE_RESTAURANT_ADMIN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token does not belong to restaurant-admin role"));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("valid", true);
            data.put("username", jwtUtils.getUsernameFromToken(token));
            data.put("userId", jwtUtils.getUserIdFromToken(token).orElse(null));
            data.put("roles", roles);
            return ResponseEntity.ok(ApiResponse.success("Token is valid", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token validation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            if (!jwtUtils.validateJwtToken(jwtToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid or expired token"));
            }

            String username = jwtUtils.getUsernameFromToken(jwtToken);
            Optional<RestaurantStaff> staffOpt = restaurantStaffService.getStaffByUsername(username);
            if (staffOpt.isEmpty() || !isEligibleRestaurantAdmin(staffOpt.get())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("User not found or unauthorized"));
            }

            RestaurantStaff staff = staffOpt.get();
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", staff.getId());
            userData.put("username", staff.getUserName());
            userData.put("email", staff.getEmail());
            userData.put("staffRole", staff.getStaffRole());
            userData.put("restaurantId", staff.getRestaurantId());
            userData.put("status", staff.getStatus());
            userData.put("permissions", staff.getPermissions());

            return ResponseEntity.ok(ApiResponse.success("User info retrieved", userData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get user info: " + e.getMessage()));
        }
    }

    private boolean isEligibleRestaurantAdmin(RestaurantStaff staff) {
        if (staff == null) return false;
        boolean active = Boolean.TRUE.equals(staff.getIsActive());
        boolean notDeleted = staff.getDeletedAt() == null;
        boolean statusActive = "ACTIVE".equalsIgnoreCase(staff.getStatus());
        boolean roleAllowed = "RESTAURANT_ADMIN".equalsIgnoreCase(staff.getStaffRole())
                || "MANAGER".equalsIgnoreCase(staff.getStaffRole());
        return active && notDeleted && statusActive && roleAllowed;
    }

    private Map<String, Object> createExtraClaims(RestaurantStaff staff) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("restaurantId", staff.getRestaurantId());
        claims.put("staffRole", staff.getStaffRole());
        return claims;
    }
}
