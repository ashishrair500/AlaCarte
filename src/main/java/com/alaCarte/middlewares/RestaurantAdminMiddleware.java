package com.alaCarte.middlewares;

import com.alaCarte.enums.UserRoles;
import com.alaCarte.jwtUtils.JwtUtils;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Middleware to validate RestaurantAdmin access
 * Checks JWT token validity and RESTAURANT_ADMIN role
 */
@Component
public class RestaurantAdminMiddleware {

    private final JwtUtils jwtUtils;

    public RestaurantAdminMiddleware(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * Validates JWT token and checks for RESTAURANT_ADMIN role
     * @param token JWT token (with or without "Bearer " prefix)
     * @throws JwtException if token is invalid or user doesn't have RESTAURANT_ADMIN role
     */
    public void validateRestaurantAdmin(String token) {
        // Remove "Bearer " prefix if present
        String jwtToken = token.replace("Bearer ", "").trim();

        // Validate JWT token
        if (!jwtUtils.validateJwtToken(jwtToken)) {
            throw new JwtException("Invalid or expired JWT token");
        }

        // Extract roles from token
        List<String> roles = jwtUtils.getRolesFromToken(jwtToken);

        // Check if user has RESTAURANT_ADMIN role
        boolean isRestaurantAdmin = roles.stream()
                .anyMatch(role -> role.equalsIgnoreCase(UserRoles.RESTAURANT_ADMIN.name()) ||
                                 role.equalsIgnoreCase("ROLE_RESTAURANT_ADMIN"));

        if (!isRestaurantAdmin) {
            throw new JwtException("Access denied: RESTAURANT_ADMIN role required");
        }
    }

    /**
     * Alias method for backward compatibility
     */
    public void middleware(String token) {
        validateRestaurantAdmin(token);
    }
}
