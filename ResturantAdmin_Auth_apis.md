# Restaurant Admin Authentication APIs

This document provides `curl` examples for all Restaurant Admin authentication endpoints.

---

## 1. Login (POST)

_Authenticate a restaurant admin or manager and obtain access and refresh tokens._

```bash
curl -X POST "http://localhost:8080/api/v1/auth/restaurant-admin/login" \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin_yeti",
    "password": "SecurePassword123"
  }'
```

---

## 2. Refresh Token (POST)

_Obtain a new access token using a valid refresh token._

```bash
curl -X POST "http://localhost:8080/api/v1/auth/restaurant-admin/refresh?refreshToken=<YOUR_REFRESH_TOKEN>" \
  -H "Content-Type: application/json"
```

---

## 3. Validate Token (POST)

_Verify if a given token is valid and belongs to the restaurant-admin role._

```bash
curl -X POST "http://localhost:8080/api/v1/auth/restaurant-admin/validate?token=<YOUR_ACCESS_TOKEN>" \
  -H "Content-Type: application/json"
```

---

## 4. Get Current User Info (GET)

_Retrieve the profile and permissions of the currently authenticated restaurant admin._

```bash
curl -X GET "http://localhost:8080/api/v1/auth/restaurant-admin/me" \
  -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>"
```

---

**Note:**  
Replace `<YOUR_ACCESS_TOKEN>` and `<YOUR_REFRESH_TOKEN>` with the actual JWT tokens returned from the login endpoint.
