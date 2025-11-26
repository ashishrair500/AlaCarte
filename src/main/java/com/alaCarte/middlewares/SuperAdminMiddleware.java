package com.alaCarte.middlewares;

import com.alaCarte.jwtUtils.JwtUtils;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Middleware to validate SuperAdmin access
 * Checks JWT token validity and SUPER_ADMIN role
 */
@Component
public class SuperAdminMiddleware {

    private final JwtUtils jwtUtils;

    public SuperAdminMiddleware(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * Validates JWT token and checks for SUPER_ADMIN role
     * @param token JWT token (with or without "Bearer " prefix)
     * @throws JwtException if token is invalid or user doesn't have SUPER_ADMIN role
     */
    public void validateSuperAdmin(String token) {
        // Remove "Bearer " prefix if present
        String jwtToken = token.replace("Bearer ", "").trim();

        // Validate JWT token
        if (!jwtUtils.validateJwtToken(jwtToken)) {
            throw new JwtException("Invalid or expired JWT token");
        }

        // Extract roles from token
        List<String> roles = jwtUtils.getRolesFromToken(jwtToken);

        // Check if user has SUPER_ADMIN role
        boolean isSuperAdmin = roles.stream()
                .anyMatch(role -> role.equalsIgnoreCase("SUPER_ADMIN") || 
                                 role.equalsIgnoreCase("ROLE_SUPER_ADMIN"));

        if (!isSuperAdmin) {
            throw new JwtException("Access denied: SUPER_ADMIN role required");
        }
    }

    /**
     * Alias method for backward compatibility
     */
    public void middleware(String token) {
        validateSuperAdmin(token);
    }
}
