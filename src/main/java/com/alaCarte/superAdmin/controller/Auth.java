package com.alaCarte.superAdmin.controller;

import com.alaCarte.dto.ApiResponse;
import com.alaCarte.dto.LoginRequest;
import com.alaCarte.dto.LoginResponse;
import com.alaCarte.dto.SignupRequest;
import com.alaCarte.entity.SuperAdmin;
import com.alaCarte.eums.UserRoles;
import com.alaCarte.jwtUtils.JwtUtils;
import com.alaCarte.service.SuperAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication Controller for SuperAdmin
 * Handles login and JWT token generation
 */
@RestController("superAdminAuthController")
@RequestMapping("/api/auth/superadmin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class Auth {

    private final SuperAdminService superAdminService;
    private final JwtUtils jwtUtils;

    public Auth(SuperAdminService superAdminService, JwtUtils jwtUtils) {
        this.superAdminService = superAdminService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Signup/Register endpoint for SuperAdmin
     * 
     * @param signupRequest user registration details
     * @return JWT tokens and user info
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<LoginResponse>> signup(@Valid @RequestBody SignupRequest signupRequest) {
      //  try {
            // Check if username already exists
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Signing up is not allowed"));

//            if (superAdminService.usernameExists(signupRequest.getUsername())) {
//                return ResponseEntity
//                        .status(HttpStatus.BAD_REQUEST)
//                        .body(ApiResponse.error("Username already exists"));
//            }
//
//            // Check if email already exists
//            if (superAdminService.emailExists(signupRequest.getEmail())) {
//                return ResponseEntity
//                        .status(HttpStatus.BAD_REQUEST)
//                        .body(ApiResponse.error("Email already exists"));
//            }
//
//            // Create new SuperAdmin
//            SuperAdmin newSuperAdmin = new SuperAdmin();
//            newSuperAdmin.setUsername(signupRequest.getUsername());
//            newSuperAdmin.setFullName(signupRequest.getFullName());
//            newSuperAdmin.setEmail(signupRequest.getEmail());
//            newSuperAdmin.setPhone(signupRequest.getPhone());
//            newSuperAdmin.setRoleLevel(signupRequest.getRoleLevel() != null ? signupRequest.getRoleLevel() : (short) 1);
//            newSuperAdmin.setIsActive(true);
//
//            // Save with encrypted password
//            SuperAdmin savedSuperAdmin = superAdminService.createSuperAdmin(
//                    newSuperAdmin,
//                    signupRequest.getPassword()
//            );
//
//            // Generate tokens
//            String accessToken = jwtUtils.generateToken(
//                    savedSuperAdmin.getUsername(),
//                    savedSuperAdmin.getId(),
//                    Collections.singletonList("ROLE_SUPER_ADMIN"),
//                    createExtraClaims(savedSuperAdmin)
//            );
//
//            String refreshToken = jwtUtils.generateRefreshToken(
//                    savedSuperAdmin.getUsername(),
//                    savedSuperAdmin.getId()
//            );
//
//            // Build response
//            LoginResponse response = LoginResponse.builder()
//                    .userId(savedSuperAdmin.getId())
//                    .username(savedSuperAdmin.getUsername())
//                    .fullName(savedSuperAdmin.getFullName())
//                    .email(savedSuperAdmin.getEmail())
//                    .roleLevel(savedSuperAdmin.getRoleLevel())
//                    .permissions(savedSuperAdmin.getPermissions())
//                    .accessToken(accessToken)
//                    .refreshToken(refreshToken)
//                    .tokenType("Bearer")
//                    .expiresIn(86400L) // 24 hours in seconds
//                    .message("Registration successful")
//                    .build();
//
//            return ResponseEntity
//                    .status(HttpStatus.CREATED)
//                    .body(ApiResponse.success("Registration successful", response));
//
//        } catch (RuntimeException e) {
//            return ResponseEntity
//                    .status(HttpStatus.BAD_REQUEST)
//                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
  //      }
    }

    /**
     * Login endpoint for SuperAdmin
     * 
     * @param loginRequest username/email and password
     * @return JWT tokens and user info
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user
            Optional<SuperAdmin> superAdminOpt = superAdminService.authenticate(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword()
            );

            if (superAdminOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid username/email or password"));
            }

            SuperAdmin superAdmin = superAdminOpt.get();

            // Generate tokens
            String accessToken = jwtUtils.generateToken(
                    superAdmin.getUsername(),
                    superAdmin.getId(),
                    Collections.singletonList(UserRoles.SUPER_ADMIN.name()),
                    createExtraClaims(superAdmin)
            );

            String refreshToken = jwtUtils.generateRefreshToken(
                    superAdmin.getUsername(),
                    superAdmin.getId()
            );

            // Build response
            LoginResponse response = LoginResponse.builder()
                    .userId(superAdmin.getId())
                    .username(superAdmin.getUsername())
                    .fullName(superAdmin.getFullName())
                    .email(superAdmin.getEmail())
                    .roleLevel(superAdmin.getRoleLevel())
                    .permissions(superAdmin.getPermissions())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400L) // 24 hours in seconds
                    .message("Login successful")
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    /**
     * Validate token endpoint
     * 
     * @param token JWT token to validate
     * @return validation result
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(@RequestParam String token) {
        try {
            boolean isValid = jwtUtils.validateJwtToken(token);
            
            if (isValid) {
                String username = jwtUtils.getUsernameFromToken(token);
                
                Map<String, Object> data = new HashMap<>();
                data.put("valid", true);
                data.put("username", username);
                data.put("userId", jwtUtils.getUserIdFromToken(token).orElse(null));
                data.put("roles", jwtUtils.getRolesFromToken(token));
                
                return ResponseEntity.ok(ApiResponse.success("Token is valid", data));
            } else {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid or expired token"));
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token validation failed: " + e.getMessage()));
        }
    }

    /**
     * Refresh token endpoint
     * 
     * @param refreshToken refresh token
     * @return new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(@RequestParam String refreshToken) {
        try {
            // Validate refresh token
            if (!jwtUtils.validateJwtToken(refreshToken)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid or expired refresh token"));
            }

            // Extract user info from refresh token
            String username = jwtUtils.getUsernameFromToken(refreshToken);
            Optional<SuperAdmin> superAdminOpt = superAdminService.findActiveByUsername(username);

            if (superAdminOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("User not found or inactive"));
            }

            SuperAdmin superAdmin = superAdminOpt.get();

            // Generate new access token
            String newAccessToken = jwtUtils.generateToken(
                    superAdmin.getUsername(),
                    superAdmin.getId(),
                    Collections.singletonList("ROLE_SUPER_ADMIN"),
                    createExtraClaims(superAdmin)
            );

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            tokens.put("tokenType", "Bearer");
            tokens.put("expiresIn", "86400");

            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", tokens));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Token refresh failed: " + e.getMessage()));
        }
    }

    /**
     * Get current user info from token
     * 
     * @param token JWT token
     * @return user information
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix
            String jwtToken = token.replace("Bearer ", "");

            if (!jwtUtils.validateJwtToken(jwtToken)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid or expired token"));
            }

            String username = jwtUtils.getUsernameFromToken(jwtToken);
            Optional<SuperAdmin> superAdminOpt = superAdminService.findActiveByUsername(username);

            if (superAdminOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found"));
            }

            SuperAdmin superAdmin = superAdminOpt.get();
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", superAdmin.getId());
            userData.put("username", superAdmin.getUsername());
            userData.put("fullName", superAdmin.getFullName());
            userData.put("email", superAdmin.getEmail());
            userData.put("roleLevel", superAdmin.getRoleLevel());
            userData.put("permissions", superAdmin.getPermissions());
            userData.put("isActive", superAdmin.getIsActive());

            return ResponseEntity.ok(ApiResponse.success("User info retrieved", userData));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get user info: " + e.getMessage()));
        }
    }

    /**
     * Create extra claims for JWT token
     */
    private Map<String, Object> createExtraClaims(SuperAdmin superAdmin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roleLevel", superAdmin.getRoleLevel());
        if (superAdmin.getFullName() != null) {
            claims.put("fullName", superAdmin.getFullName());
        }
        if (superAdmin.getEmail() != null) {
            claims.put("email", superAdmin.getEmail());
        }
        return claims;
    }
}
