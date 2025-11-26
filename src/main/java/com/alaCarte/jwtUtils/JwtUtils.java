package com.alaCarte.jwtUtils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

/**
 * JwtUtils - responsible for generating and validating JWT tokens.
 *
 * - Uses HS256 symmetric signing (HMAC-SHA256).
 * - Exposes generateToken, generateRefreshToken, extractClaim, validate, getUsername.
 * - Adds useful claims: sub (username), roles (list), uid (user id) and any custom claims you provide.
 *
 * Important: Keep secret in env var or secret manager. Do not commit secrets.
 */
@Component
public class JwtUtils {

    private final Key key;
    private final long jwtExpirationMs;
    private final long refreshExpirationMs;
    private final String issuer;

    public JwtUtils(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long jwtExpirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs,
            @Value("${jwt.issuer}") String issuer
    ) {
        // decode Base64 if provided; if plain text, derive key bytes directly
        byte[] keyBytes = decodeSecret(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationMs = jwtExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.issuer = issuer;
    }

    // Helper: accept base64 or raw string secret
    private static byte[] decodeSecret(String secret) {
        // If secret looks like base64 (contains '=' or it's long) try decode, else use UTF-8 bytes.
        try {
            // Try base64 decoding
            return Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            return secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    // ========== Token generation ==========

    /**
     * Generate access token with standard and custom claims.
     *
     * @param username principal name (subject)
     * @param userId id of user (optional)
     * @param roles list of role strings (optional)
     * @param extraClaims map of additional claims (optional)
     * @return signed JWT compact string
     */
    public String generateToken(String username, UUID userId, Collection<String> roles, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>();
        if (roles != null && !roles.isEmpty()) {
            claims.put("roles", roles);
        }
        if (userId != null) {
            claims.put("uid", userId.toString());
        }
        if (extraClaims != null && !extraClaims.isEmpty()) {
            claims.putAll(extraClaims);
        }
        return buildToken(claims, username, jwtExpirationMs);
    }

    /**
     * Generate a refresh token (no roles by default, can include uid).
     */
    public String generateRefreshToken(String username, UUID userId) {
        Map<String, Object> claims = new HashMap<>();
        if (userId != null) claims.put("uid", userId.toString());
        return buildToken(claims, username, refreshExpirationMs);
    }

    // internal builder
    private String buildToken(Map<String, Object> claims, String subject, long ttlMillis) {
        long now = System.currentTimeMillis();
        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMillis))
                .signWith(key, SignatureAlgorithm.HS256);
        return builder.compact();
    }

    // ========== Claim extraction helpers ==========

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = parseClaims(token);
        Object roles = claims.get("roles");
        if (roles instanceof List) {
            List<?> raw = (List<?>) roles;
            List<String> result = new ArrayList<>();
            for (Object o : raw) {
                result.add(String.valueOf(o));
            }
            return result;
        }
        return Collections.emptyList();
    }

    public Optional<UUID> getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        Object uid = claims.get("uid");
        if (uid != null) {
            try {
                return Optional.of(UUID.fromString(uid.toString()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    // ========== Validation ==========

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // token expired
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            // invalid token
            return false;
        }
    }

    // Throws runtime exception if invalid or expired. Use parseClaims for more details.
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException ex) {
            // wrap or rethrow as needed - for convenience we rethrow a runtime exception
            throw new RuntimeException("Invalid JWT token: " + ex.getMessage(), ex);
        }
    }
}

