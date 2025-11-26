# SuperAdmin Authentication API Documentation

This document describes the authentication endpoints for SuperAdmin users with JWT token support.

---

## 🔐 Authentication Endpoints

Base URL: `http://localhost:8080/api/auth/superadmin`

---

## 1. Signup / Register

Create a new SuperAdmin account.

**Endpoint:** `POST /api/auth/superadmin/signup`

**Request Body:**
```json
{
  "username": "admin123",
  "password": "SecurePass123!",
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "roleLevel": 1
}
```

**Field Validations:**
- `username`: Required, 3-50 characters
- `password`: Required, minimum 6 characters
- `fullName`: Required
- `email`: Required, valid email format
- `phone`: Optional
- `roleLevel`: Optional (defaults to 1)

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "admin123",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "roleLevel": 1,
    "permissions": null,
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "message": "Registration successful"
  },
  "error": null
}
```

**Error Responses:**

**400 Bad Request** - Username already exists:
```json
{
  "success": false,
  "message": "Username already exists",
  "data": null,
  "error": null
}
```

**400 Bad Request** - Email already exists:
```json
{
  "success": false,
  "message": "Email already exists",
  "data": null,
  "error": null
}
```

**400 Bad Request** - Validation error:
```json
{
  "success": false,
  "message": "Registration failed: Password must be at least 6 characters",
  "data": null,
  "error": null
}
```

---

## 2. Login

Authenticate and get JWT tokens.

**Endpoint:** `POST /api/auth/superadmin/login`

**Request Body:**
```json
{
  "usernameOrEmail": "admin123",
  "password": "SecurePass123!"
}
```

You can use either username or email in the `usernameOrEmail` field.

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "admin123",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "roleLevel": 1,
    "permissions": {
      "canCreateUsers": true,
      "canEditMenu": true
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "message": "Login successful"
  },
  "error": null
}
```

**Error Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Invalid username/email or password",
  "data": null,
  "error": null
}
```

---

## 3. Validate Token

Validate if a JWT token is valid and not expired.

**Endpoint:** `POST /api/auth/superadmin/validate`

**Query Parameters:**
- `token`: JWT access token

**Example:**
```
POST /api/auth/superadmin/validate?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Token is valid",
  "data": {
    "valid": true,
    "username": "admin123",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "roles": ["ROLE_SUPER_ADMIN"]
  },
  "error": null
}
```

**Error Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Invalid or expired token",
  "data": null,
  "error": null
}
```

---

## 4. Refresh Token

Get a new access token using a refresh token.

**Endpoint:** `POST /api/auth/superadmin/refresh`

**Query Parameters:**
- `refreshToken`: JWT refresh token

**Example:**
```
POST /api/auth/superadmin/refresh?refreshToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": "86400"
  },
  "error": null
}
```

**Error Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Invalid or expired refresh token",
  "data": null,
  "error": null
}
```

---

## 5. Get Current User Info

Get information about the currently authenticated user.

**Endpoint:** `GET /api/auth/superadmin/me`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "User info retrieved",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "admin123",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "roleLevel": 1,
    "permissions": {
      "canCreateUsers": true,
      "canEditMenu": true
    },
    "isActive": true
  },
  "error": null
}
```

**Error Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Invalid or expired token",
  "data": null,
  "error": null
}
```

---

## 🔑 JWT Token Structure

### Access Token
- **Type:** JWT (JSON Web Token)
- **Algorithm:** HS256 (HMAC-SHA256)
- **Expiration:** 24 hours (86400 seconds)
- **Claims:**
  - `sub`: Username
  - `uid`: User ID (UUID)
  - `roles`: ["ROLE_SUPER_ADMIN"]
  - `roleLevel`: Role level number
  - `fullName`: User's full name
  - `email`: User's email
  - `iss`: Issuer (alaCarte)
  - `iat`: Issued at timestamp
  - `exp`: Expiration timestamp

### Refresh Token
- **Type:** JWT (JSON Web Token)
- **Algorithm:** HS256 (HMAC-SHA256)
- **Expiration:** 7 days (604800 seconds)
- **Claims:**
  - `sub`: Username
  - `uid`: User ID (UUID)
  - `iss`: Issuer (alaCarte)
  - `iat`: Issued at timestamp
  - `exp`: Expiration timestamp

---

## 🔨 Using JWT Tokens in API Requests

Once you have an access token, include it in the `Authorization` header for protected endpoints:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Example with cURL:
```bash
curl -X GET http://localhost:8080/api/auth/superadmin/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Example with JavaScript (Fetch):
```javascript
fetch('http://localhost:8080/api/auth/superadmin/me', {
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

### Example with Postman:
1. Go to **Authorization** tab
2. Select **Type:** Bearer Token
3. Paste your access token in the **Token** field

---

## 📝 Complete Usage Flow

### 1. Register a new SuperAdmin
```bash
curl -X POST http://localhost:8080/api/auth/superadmin/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin123",
    "password": "SecurePass123!",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/superadmin/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin123",
    "password": "SecurePass123!"
  }'
```

### 3. Use Access Token
```bash
curl -X GET http://localhost:8080/api/auth/superadmin/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 4. Refresh Token (when access token expires)
```bash
curl -X POST "http://localhost:8080/api/auth/superadmin/refresh?refreshToken=YOUR_REFRESH_TOKEN"
```

---

## ⚙️ Configuration

JWT settings are configured in `application.properties`:

```properties
# JWT Configuration
jwt.secret=dGhpc2lzYXZlcnlzZWNyZXRrZXlmb3JhbGFDYXJ0ZW1lbnVhZ2VudGFwcGxpY2F0aW9uMTIzNDU2Nzg5MA==
jwt.expiration-ms=86400000          # 24 hours in milliseconds
jwt.refresh-expiration-ms=604800000 # 7 days in milliseconds
jwt.issuer=alaCarte
```

**⚠️ Security Note:** Change the `jwt.secret` value in production and use environment variables instead of hardcoding it.

---

## 🛡️ Security Features

1. **Password Encryption:** BCrypt hashing with salt
2. **Stateless Authentication:** JWT tokens (no server-side sessions)
3. **Token Expiration:** Access tokens expire after 24 hours
4. **Refresh Tokens:** Long-lived tokens for obtaining new access tokens
5. **CORS Enabled:** Cross-origin requests allowed (configure as needed)
6. **Active User Check:** Only active users can login
7. **Role-Based Access:** Tokens include role information

---

## 🚨 Error Handling

All endpoints follow a consistent error response format:

```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "error": "Additional error details (optional)"
}
```

Common HTTP Status Codes:
- `200 OK`: Successful request
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid input data
- `401 Unauthorized`: Invalid or expired credentials/token
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

---

## 📦 Response Wrapper

All API responses are wrapped in a consistent format:

```json
{
  "success": boolean,
  "message": "string",
  "data": object | null,
  "error": object | null
}
```

---

## 🧪 Testing

You can test these endpoints using:
- **Postman**
- **cURL**
- **Insomnia**
- **Your frontend application**

Example Postman collection structure:
```
AlaCarte SuperAdmin Auth
├── Signup
├── Login
├── Validate Token
├── Refresh Token
└── Get Current User
```

---

## 🔄 Token Refresh Flow

```
1. User logs in → Receives access_token + refresh_token
2. Use access_token for API requests
3. Access token expires after 24 hours
4. Use refresh_token to get new access_token
5. Continue using new access_token
6. Refresh token expires after 7 days → User must login again
```

---

## 📱 Frontend Integration Example

```javascript
// Store tokens
localStorage.setItem('accessToken', response.data.accessToken);
localStorage.setItem('refreshToken', response.data.refreshToken);

// API request with token
const apiCall = async (url) => {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch(url, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  
  if (response.status === 401) {
    // Token expired, try to refresh
    await refreshAccessToken();
    // Retry the original request
  }
  
  return response.json();
};

// Refresh token function
const refreshAccessToken = async () => {
  const refreshToken = localStorage.getItem('refreshToken');
  
  const response = await fetch(
    `http://localhost:8080/api/auth/superadmin/refresh?refreshToken=${refreshToken}`,
    { method: 'POST' }
  );
  
  if (response.ok) {
    const data = await response.json();
    localStorage.setItem('accessToken', data.data.accessToken);
  } else {
    // Refresh token expired, redirect to login
    window.location.href = '/login';
  }
};
```

---

## ✅ Next Steps

After authentication is set up, you can:

1. **Protect Other APIs:** Add JWT validation to other endpoints
2. **Add Role-Based Authorization:** Check user roles/permissions
3. **Implement Logout:** Clear tokens on client side (stateless JWT)
4. **Add Password Reset:** Implement forgot password flow
5. **Email Verification:** Add email verification for new signups
6. **Audit Logging:** Log authentication events

---

**🎉 Your SuperAdmin authentication system is ready to use!**

