package com.alaCarte.menu.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RequestContextUtil {

    public UUID getRestaurantId(HttpServletRequest request) {
        return getRequiredUuid(request, "restaurantId");
    }

    public UUID getStaffId(HttpServletRequest request) {
        return getRequiredUuid(request, "staffId");
    }

    public String getStaffRole(HttpServletRequest request) {
        Object staffRole = request.getAttribute("staffRole");
        if (staffRole == null) {
            throw new AccessDeniedException("Staff role context not found in request");
        }
        return staffRole.toString();
    }

    private UUID getRequiredUuid(HttpServletRequest request, String attributeName) {
        Object value = request.getAttribute(attributeName);
        if (value == null) {
            throw new AccessDeniedException(attributeName + " context not found in request");
        }
        return UUID.fromString(value.toString());
    }
}
