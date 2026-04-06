package com.alaCarte.config;

import com.alaCarte.jwtUtils.JwtUtils;
import com.alaCarte.middlewares.RestaurantAdminMiddleware;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RestaurantAdminRequestInterceptor implements HandlerInterceptor {

    private final RestaurantAdminMiddleware restaurantAdminMiddleware;
    private final JwtUtils jwtUtils;

    public RestaurantAdminRequestInterceptor(
            RestaurantAdminMiddleware restaurantAdminMiddleware,
            JwtUtils jwtUtils
    ) {
        this.restaurantAdminMiddleware = restaurantAdminMiddleware;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            throw new JwtException("Missing Authorization header");
        }

        restaurantAdminMiddleware.validateRestaurantAdmin(authorization);

        String token = authorization.replace("Bearer ", "").trim();
        Claims claims = jwtUtils.parseClaims(token);

        Object restaurantId = claims.get("restaurantId");
        Object staffId = claims.get("uid");
        Object staffRole = claims.get("staffRole");

        if (restaurantId != null) {
            request.setAttribute("restaurantId", restaurantId.toString());
        }
        if (staffId != null) {
            request.setAttribute("staffId", staffId.toString());
        }
        if (staffRole != null) {
            request.setAttribute("staffRole", staffRole.toString());
        }

        if (restaurantId == null || staffId == null || staffRole == null) {
            throw new JwtException("Required restaurant admin claims are missing from the token");
        }

        return true;
    }
}
