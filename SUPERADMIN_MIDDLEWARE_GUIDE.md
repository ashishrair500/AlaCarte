# SuperAdmin Middleware - Security Guide

## Overview

The `SuperAdminMiddleware` is a centralized security component that protects all SuperAdmin APIs by validating JWT tokens and verifying SUPER_ADMIN role access.

---

## How It Works

### 1. **Middleware Location**
```
src/main/java/com/alaCarte/middlewares/SuperAdminMiddleware.java
```

### 2. **Key Features**

✅ **JWT Token Validation** - Verifies token authenticity and expiration  
✅ **Role-Based Access Control (RBAC)** - Ensures only SUPER_ADMIN role can access  
✅ **Centralized Security** - Single point of authentication for all SuperAdmin endpoints  
✅ **Exception Handling** - Throws `JwtException` for invalid tokens or insufficient permissions  

---

## Middleware Implementation

```java
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
}
```

---

## How It's Used in Controllers

### Step 1: Inject the Middleware

```java
@RestController
@RequestMapping("/api/superadmin/restaurants")
public class RestaurantManagementController {

    private final RestaurantService restaurantService;
    private final JwtUtils jwtUtils;
    private final SuperAdminMiddleware superAdminMiddleware; // Inject middleware

    public RestaurantManagementController(
            RestaurantService restaurantService, 
            JwtUtils jwtUtils, 
            SuperAdminMiddleware middleware) {
        this.restaurantService = restaurantService;
        this.jwtUtils = jwtUtils;
        this.superAdminMiddleware = middleware;
    }
    // ... endpoints
}
```

### Step 2: Use in Every Endpoint

```java
@PostMapping
public ResponseEntity<ApiResponse<RestaurantResponse>> createRestaurant(
        @Valid @RequestBody RestaurantRequest request,
        @RequestHeader("Authorization") String token) {
    try {
        // Validate SuperAdmin access - THIS IS THE KEY LINE
        superAdminMiddleware.validateSuperAdmin(token);

        // Extract SuperAdmin ID from token (for audit trail)
        String jwtToken = token.replace("Bearer ", "");
        UUID superAdminId = jwtUtils.getUserIdFromToken(jwtToken)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        // Business logic continues...
        Restaurant restaurant = mapToEntity(request);
        Restaurant savedRestaurant = restaurantService.createRestaurant(restaurant, superAdminId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Restaurant created successfully", mapToResponse(savedRestaurant)));

    } catch (io.jsonwebtoken.JwtException e) {
        // Handle authentication/authorization failures
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage()));
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create restaurant: " + e.getMessage()));
    }
}
```

---

## Security Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Client sends request with JWT token in Authorization header │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. Controller receives request                                  │
│    - Extract token from @RequestHeader("Authorization")         │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. Call superAdminMiddleware.validateSuperAdmin(token)          │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4. Middleware validates JWT token                               │
│    ✓ Check signature                                            │
│    ✓ Check expiration                                           │
│    ✓ Check format                                               │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 5. Middleware extracts roles from token                         │
│    - Uses jwtUtils.getRolesFromToken()                          │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│ 6. Middleware checks for SUPER_ADMIN role                       │
│    - Must have "SUPER_ADMIN" or "ROLE_SUPER_ADMIN" in roles    │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
           ┌─────────────┴─────────────┐
           │                           │
           ▼                           ▼
    ✅ AUTHORIZED               ❌ UNAUTHORIZED
    Continue to                 Throw JwtException
    business logic              Return 401 error
```

---

## Protected Endpoints

All **14 endpoints** in `RestaurantManagementController` are protected:

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 1 | POST | `/api/superadmin/restaurants` | Create restaurant |
| 2 | GET | `/api/superadmin/restaurants` | Get all restaurants |
| 3 | GET | `/api/superadmin/restaurants/{id}` | Get restaurant by ID |
| 4 | GET | `/api/superadmin/restaurants/search` | Search by name |
| 5 | GET | `/api/superadmin/restaurants/advanced-search` | Advanced search |
| 6 | GET | `/api/superadmin/restaurants/by-city/{city}` | Get by city |
| 7 | GET | `/api/superadmin/restaurants/by-status/{status}` | Get by status |
| 8 | GET | `/api/superadmin/restaurants/popular` | Get popular |
| 9 | PUT | `/api/superadmin/restaurants/{id}` | Full update |
| 10 | PATCH | `/api/superadmin/restaurants/{id}` | Partial update |
| 11 | PATCH | `/api/superadmin/restaurants/{id}/status` | Update status |
| 12 | PATCH | `/api/superadmin/restaurants/{id}/toggle-popular` | Toggle popular |
| 13 | DELETE | `/api/superadmin/restaurants/{id}` | Delete restaurant |
| 14 | GET | `/api/superadmin/restaurants/stats` | Get statistics |

---

## Error Responses

### 1. Invalid or Expired Token

```json
{
  "success": false,
  "message": "Invalid or expired JWT token",
  "data": null
}
```

**HTTP Status**: 401 Unauthorized

### 2. Missing SUPER_ADMIN Role

```json
{
  "success": false,
  "message": "Access denied: SUPER_ADMIN role required",
  "data": null
}
```

**HTTP Status**: 401 Unauthorized

### 3. Missing Authorization Header

```json
{
  "success": false,
  "message": "Required request header 'Authorization' is not present",
  "data": null
}
```

**HTTP Status**: 400 Bad Request

---

## Testing the Middleware

### 1. **Valid SuperAdmin Token** ✅

```bash
curl -X GET http://localhost:8080/api/superadmin/restaurants \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Expected**: 200 OK with restaurant list

### 2. **Invalid Token** ❌

```bash
curl -X GET http://localhost:8080/api/superadmin/restaurants \
  -H "Authorization: Bearer invalid_token_here"
```

**Expected**: 401 Unauthorized - "Invalid or expired JWT token"

### 3. **Non-SuperAdmin Token** ❌

```bash
# Token with RESTAURANT_ADMIN or CUSTOMER role
curl -X GET http://localhost:8080/api/superadmin/restaurants \
  -H "Authorization: Bearer <token_with_different_role>"
```

**Expected**: 401 Unauthorized - "Access denied: SUPER_ADMIN role required"

### 4. **No Token** ❌

```bash
curl -X GET http://localhost:8080/api/superadmin/restaurants
```

**Expected**: 400 Bad Request - "Required request header 'Authorization' is not present"

---

## Benefits of Centralized Middleware

### 1. **Consistency**
- All SuperAdmin endpoints use the same authentication logic
- No code duplication
- Easy to maintain

### 2. **Security**
- Single point of failure/success
- Changes to auth logic only need to be made in one place
- Reduces risk of security vulnerabilities

### 3. **Maintainability**
- Easy to add logging
- Easy to add rate limiting
- Easy to update JWT validation logic

### 4. **Testability**
- Mock the middleware once
- Test authentication logic independently
- Easier unit testing

---

## How to Use in New SuperAdmin Controllers

When creating new SuperAdmin controllers, follow this pattern:

```java
@RestController
@RequestMapping("/api/superadmin/your-resource")
@CrossOrigin(origins = "*", maxAge = 3600)
public class YourSuperAdminController {

    private final YourService yourService;
    private final JwtUtils jwtUtils;
    private final SuperAdminMiddleware superAdminMiddleware; // 1. Inject

    public YourSuperAdminController(
            YourService yourService,
            JwtUtils jwtUtils,
            SuperAdminMiddleware middleware) {
        this.yourService = yourService;
        this.jwtUtils = jwtUtils;
        this.superAdminMiddleware = middleware; // 2. Store
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<YourEntity>>> getAll(
            @RequestHeader("Authorization") String token) { // 3. Add token param
        try {
            // 4. ALWAYS call this first
            superAdminMiddleware.validateSuperAdmin(token);

            // 5. Your business logic
            List<YourEntity> entities = yourService.getAll();
            return ResponseEntity.ok(ApiResponse.success("Success", entities));

        } catch (io.jsonwebtoken.JwtException e) {
            // 6. Handle auth errors
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed: " + e.getMessage()));
        }
    }
}
```

---

## User Roles Enum

The system uses the following roles (defined in `UserRoles` enum):

```java
public enum UserRoles {
    SUPER_ADMIN,        // Full system access
    RESTAURANT_ADMIN,   // Restaurant owner/manager
    RESTAURANT_STAFF,   // Restaurant staff
    CUSTOMER            // End users
}
```

Only users with `SUPER_ADMIN` role can access SuperAdmin APIs.

---

## JWT Token Structure

A valid JWT token for SuperAdmin contains:

```json
{
  "sub": "superadmin_username",
  "uid": "123e4567-e89b-12d3-a456-426614174000",
  "roles": ["SUPER_ADMIN"],
  "iss": "alaCarte",
  "iat": 1705392000,
  "exp": 1705478400
}
```

**Key Claims**:
- `sub`: Username
- `uid`: User ID (UUID)
- `roles`: Array of user roles (must include "SUPER_ADMIN")
- `iss`: Issuer (alaCarte)
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp

---

## Best Practices

1. **Always call middleware first** in every endpoint
2. **Handle JwtException separately** from other exceptions
3. **Return 401 for auth failures**, not 403 or 500
4. **Log authentication failures** for security auditing
5. **Never skip middleware** even for "read-only" operations
6. **Use meaningful error messages** to help debugging

---

## Security Checklist

✅ All SuperAdmin endpoints require `Authorization` header  
✅ All endpoints call `superAdminMiddleware.validateSuperAdmin(token)`  
✅ All endpoints catch and handle `JwtException`  
✅ All 401 errors return appropriate error messages  
✅ Middleware checks both token validity AND role  
✅ No hardcoded bypass mechanisms  
✅ Tokens are validated on every request (stateless)  

---

## Future Enhancements

Consider adding:

1. **Rate Limiting**: Prevent brute force attacks
2. **IP Whitelisting**: Restrict SuperAdmin access to specific IPs
3. **Audit Logging**: Log all SuperAdmin actions
4. **Multi-Factor Authentication (MFA)**: Additional security layer
5. **Token Refresh**: Automatic token renewal
6. **Permission Levels**: Different SuperAdmin permission levels

---

## Summary

The `SuperAdminMiddleware` provides:

- 🔐 **JWT Token Validation**
- 👮 **Role-Based Access Control**
- 🎯 **Centralized Security**
- 🚫 **Unauthorized Access Prevention**
- 📝 **Consistent Error Handling**

All 14 restaurant management endpoints are protected and will reject any requests without a valid SuperAdmin JWT token.

