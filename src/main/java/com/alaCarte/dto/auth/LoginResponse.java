package com.alaCarte.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * DTO for login response with JWT tokens
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private UUID userId;
    private String username;
    private String fullName;
    private String email;
    private Short roleLevel;
    private Map<String, Object> permissions;
    
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // in seconds
    
    private String message;
}
