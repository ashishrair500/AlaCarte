# Restaurant Admin API Documentation

## Overview
This API allows Restaurant Admins to manage menus, menu items, and staff members. Restaurant Admins can perform full CRUD operations on these resources for their specific restaurant.

**Base URL**: `/api/restaurant-admin`

**Authentication**: All endpoints require a valid JWT token in the `Authorization` header with the `RESTAURANT_ADMIN` role.

---

## Table of Contents
1. [Authentication](#authentication)
    - [Login](#1-login)
    - [Refresh Token](#2-refresh-token)
    - [Validate Token](#3-validate-token)
    - [Get Current User](#4-get-current-user)
2. [Menu Management](#menu-management)
    - [Create Menu](#5-create-menu)
    - [Get All Menus](#6-get-all-menus)
    - [Get Menu by ID](#7-get-menu-by-id)
    - [Update Menu](#8-update-menu)
    - [Patch Menu](#9-patch-menu)
    - [Delete Menu](#10-delete-menu)
3. [Menu Item Management](#menu-item-management)
    - [Create Menu Item](#11-create-menu-item)
    - [Bulk Create Menu Items](#12-bulk-create-menu-items)
    - [Get Menu Items by Menu](#13-get-menu-items-by-menu)
    - [Get Menu Item by ID](#14-get-menu-item-by-id)
    - [Update Menu Item](#15-update-menu-item)
    - [Patch Menu Item](#16-patch-menu-item)
    - [Delete Menu Item](#17-delete-menu-item)
    - [Toggle Availability](#18-toggle-availability)
4. [Staff Management](#staff-management)
    - [Create Staff](#19-create-staff)
    - [Get All Staff](#20-get-all-staff)
    - [Get Staff by ID](#21-get-staff-by-id)
    - [Update Staff](#22-update-staff)
    - [Patch Staff](#23-patch-staff)
    - [Delete Staff](#24-delete-staff)
    - [Update Staff Status](#25-update-staff-status)

---

## Authentication

### 1. Login

**Endpoint**: `POST /api/v1/auth/restaurant-admin/login`

**Description**: Authenticate a restaurant admin and receive access and refresh tokens.

**Request Body**:
```json
{
  "usernameOrEmail": "admin_user",
  "password": "securePassword123"
}
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": "c3d4e5f6-a7b8-9012-3456-789012cdefgh",
    "username": "admin_user",
    "fullName": "Manager",
    "email": "admin@example.com",
    "permissions": ["CAN_MANAGE_MENU", "CAN_MANAGE_STAFF"],
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "def50200...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

### 2. Refresh Token

**Endpoint**: `POST /api/v1/auth/restaurant-admin/refresh`

**Description**: Refresh an expired access token using a valid refresh token.

**Query Parameters**:
- `refreshToken` (required): The refresh token issued during login

**Example**: `POST /api/v1/auth/restaurant-admin/refresh?refreshToken=def50200...`

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "tokenType": "Bearer",
    "expiresIn": "86400"
  }
}
```

### 3. Validate Token

**Endpoint**: `POST /api/v1/auth/restaurant-admin/validate`

**Description**: Validate an access token and retrieve its payload.

**Query Parameters**:
- `token` (required): The JWT token to validate

**Example**: `POST /api/v1/auth/restaurant-admin/validate?token=eyJhbGciOi...`

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Token is valid",
  "data": {
    "valid": true,
    "username": "admin_user",
    "userId": "c3d4e5f6-a7b8-9012-3456-789012cdefgh",
    "roles": ["ROLE_RESTAURANT_ADMIN"]
  }
}
```

### 4. Get Current User

**Endpoint**: `GET /api/v1/auth/restaurant-admin/me`

**Description**: Get information about the currently authenticated restaurant admin.

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "User info retrieved",
  "data": {
    "userId": "c3d4e5f6-a7b8-9012-3456-789012cdefgh",
    "username": "admin_user",
    "email": "admin@example.com",
    "staffRole": "RESTAURANT_ADMIN",
    "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "ACTIVE",
    "permissions": ["CAN_MANAGE_MENU", "CAN_MANAGE_STAFF"]
  }
}
```

---

## Menu Management

### 5. Create Menu

**Endpoint**: `POST /api/restaurant-admin/menus`

**Description**: Create a new menu for the restaurant.

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Summer Special",
  "description": "Seasonal menu for summer",
  "menuType": "seasonal",
  "isActive": true,
  "validFrom": "2023-06-01T00:00:00Z",
  "validTo": "2023-08-31T23:59:59Z",
  "displayOrder": 1,
  "coverImage": "https://example.com/summer-menu.jpg",
  "meta": {
    "theme": "summer"
  }
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/restaurant-admin/menus \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Summer Special",
    "description": "Seasonal menu for summer",
    "menuType": "seasonal",
    "isActive": true,
    "validFrom": "2023-06-01T00:00:00Z",
    "validTo": "2023-08-31T23:59:59Z",
    "displayOrder": 1,
    "coverImage": "https://example.com/summer-menu.jpg",
    "meta": {
      "theme": "summer"
    }
  }'
```

**Success Response** (201 Created):
```json
{
  "success": true,
  "message": "Menu created successfully",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
    "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Summer Special",
    // ... other fields
  }
}
```

### 6. Get All Menus

**Endpoint**: `GET /api/restaurant-admin/menus`

**Description**: Get all menus for a specific restaurant.

**Query Parameters**:
- `restaurantId` (required): ID of the restaurant

**Example**: `GET /api/restaurant-admin/menus?restaurantId=550e8400-e29b-41d4-a716-446655440000`

**cURL Example**:
```bash
curl -X GET "http://localhost:8080/api/restaurant-admin/menus?restaurantId=550e8400-e29b-41d4-a716-446655440000" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Menus retrieved successfully",
  "data": [
    {
      "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "name": "Summer Special",
      // ... other fields
    }
  ]
}
```

### 7. Get Menu by ID

**Endpoint**: `GET /api/restaurant-admin/menus/{id}`

**Description**: Get a specific menu by its UUID.

**Path Parameters**:
- `id`: Menu UUID

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/restaurant-admin/menus/a1b2c3d4-e5f6-7890-1234-567890abcdef \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Menu retrieved successfully",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
    // ... menu details
  }
}
```

### 8. Update Menu

**Endpoint**: `PUT /api/restaurant-admin/menus/{id}`

**Description**: Fully update a menu.

**Request Body**: Same as Create Menu.

**cURL Example**:
```bash
curl -X PUT http://localhost:8080/api/restaurant-admin/menus/a1b2c3d4-e5f6-7890-1234-567890abcdef \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Updated Summer Special",
    "description": "Updated description",
    "menuType": "seasonal",
    "isActive": true,
    "validFrom": "2023-06-01T00:00:00Z",
    "validTo": "2023-08-31T23:59:59Z",
    "displayOrder": 1,
    "coverImage": "https://example.com/summer-menu.jpg",
    "meta": {
      "theme": "summer"
    }
  }'
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Menu updated successfully",
  "data": { ... }
}
```

### 9. Patch Menu

**Endpoint**: `PATCH /api/restaurant-admin/menus/{id}`

**Description**: Partially update a menu.

**Request Body**:
```json
{
  "isActive": false
}
```

**cURL Example**:
```bash
curl -X PATCH http://localhost:8080/api/restaurant-admin/menus/a1b2c3d4-e5f6-7890-1234-567890abcdef \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "isActive": false
  }'
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Menu updated successfully",
  "data": { ... }
}
```

### 10. Delete Menu

**Endpoint**: `DELETE /api/restaurant-admin/menus/{id}`

**Description**: Delete a menu.

**cURL Example**:
```bash
curl -X DELETE http://localhost:8080/api/restaurant-admin/menus/a1b2c3d4-e5f6-7890-1234-567890abcdef \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Menu deleted successfully",
  "data": null
}
```

---

## Menu Item Management

### 11. Create Menu Item

**Endpoint**: `POST /api/restaurant-admin/menu-items`

**Description**: Create a new menu item.

**Request Body**:
```json
{
  "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
  "menuId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Spicy Chicken Burger",
  "description": "Grilled chicken with spicy sauce",
  "category": "Burgers",
  "price": 12.99,
  "discountPrice": 10.99,
  "isAvailable": true,
  "isVeg": false,
  "isSpicy": true,
  "containsEgg": true,
  "variants": [],
  "addons": [],
  "tags": ["spicy", "bestseller"],
  "imageUrl": "https://example.com/burger.jpg",
  "displayOrder": 1
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/restaurant-admin/menu-items \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
    "menuId": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Spicy Chicken Burger",
    "description": "Grilled chicken with spicy sauce",
    "category": "Burgers",
    "price": 12.99,
    "discountPrice": 10.99,
    "isAvailable": true,
    "isVeg": false,
    "isSpicy": true,
    "containsEgg": true,
    "variants": [],
    "addons": [],
    "tags": ["spicy", "bestseller"],
    "imageUrl": "https://example.com/burger.jpg",
    "displayOrder": 1
  }'
```

**Success Response** (201 Created):
```json
{
  "success": true,
  "message": "Menu item created successfully",
  "data": {
    "id": "b2c3d4e5-f6a7-8901-2345-678901bcdefg",
    "name": "Spicy Chicken Burger",
    // ... other fields
  }
}
```

### 12. Bulk Create Menu Items

**Endpoint**: `POST /api/restaurant-admin/menu-items/bulk`

**Description**: Create multiple menu items in a single request. The upload is transactional, so if any item fails validation or conflicts with an existing item, none of the items are created.

**Request Body**:
```json
[
  {
    "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
    "menuId": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Classic Veg Burger",
    "description": "Grilled veg patty with house sauce",
    "category": "Burgers",
    "price": 9.99,
    "isAvailable": true,
    "isVeg": true,
    "isSpicy": false,
    "containsEgg": false,
    "variants": [],
    "addons": [],
    "tags": ["veg"],
    "imageUrl": "https://example.com/classic-veg-burger.jpg"
  },
  {
    "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
    "menuId": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Peri Peri Fries",
    "description": "Crispy fries tossed in peri peri seasoning",
    "category": "Sides",
    "price": 4.99,
    "isAvailable": true,
    "isVeg": true,
    "isSpicy": true,
    "containsEgg": false,
    "variants": [],
    "addons": [],
    "tags": ["spicy", "snack"],
    "imageUrl": "https://example.com/peri-peri-fries.jpg"
  }
]
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/restaurant-admin/menu-items/bulk \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
      "menuId": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Classic Veg Burger",
      "description": "Grilled veg patty with house sauce",
      "category": "Burgers",
      "price": 9.99,
      "isAvailable": true,
      "isVeg": true,
      "isSpicy": false,
      "containsEgg": false,
      "variants": [],
      "addons": [],
      "tags": ["veg"],
      "imageUrl": "https://example.com/classic-veg-burger.jpg"
    },
    {
      "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
      "menuId": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Peri Peri Fries",
      "description": "Crispy fries tossed in peri peri seasoning",
      "category": "Sides",
      "price": 4.99,
      "isAvailable": true,
      "isVeg": true,
      "isSpicy": true,
      "containsEgg": false,
      "variants": [],
      "addons": [],
      "tags": ["spicy", "snack"],
      "imageUrl": "https://example.com/peri-peri-fries.jpg"
    }
  ]'
```

**Success Response** (201 Created):
```json
{
  "success": true,
  "message": "Menu items uploaded successfully",
  "data": [
    {
      "id": "c3d4e5f6-a7b8-9012-3456-789012cdefab",
      "name": "Classic Veg Burger"
    },
    {
      "id": "d4e5f6a7-b8c9-0123-4567-89012defabcd",
      "name": "Peri Peri Fries"
    }
  ]
}
```

### 13. Get Menu Items by Menu

**Endpoint**: `GET /api/restaurant-admin/menu-items`

**Description**: Get all items for a specific menu.

**Query Parameters**:
- `menuId` (required): ID of the menu (UUID)

**Example**: `GET /api/restaurant-admin/menu-items?menuId=123e4567-e89b-12d3-a456-426614174000`

**cURL Example**:
```bash
curl -X GET "http://localhost:8080/api/restaurant-admin/menu-items?menuId=123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Menu items retrieved successfully",
  "data": [ ... ]
}
```

### 14. Get Menu Item by ID

**Endpoint**: `GET /api/restaurant-admin/menu-items/{id}`

**Description**: Get a specific menu item by UUID.

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/restaurant-admin/menu-items/b2c3d4e5-f6a7-8901-2345-678901bcdefg \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Menu item retrieved successfully",
  "data": { ... }
}
```

### 15. Update Menu Item

**Endpoint**: `PUT /api/restaurant-admin/menu-items/{id}`

**Description**: Fully update a menu item.

**cURL Example**:
```bash
curl -X PUT http://localhost:8080/api/restaurant-admin/menu-items/b2c3d4e5-f6a7-8901-2345-678901bcdefg \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
    "menuId": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Updated Spicy Chicken Burger",
    "description": "Updated description",
    "category": "Burgers",
    "price": 13.99,
    "discountPrice": 11.99,
    "isAvailable": true,
    "isVeg": false,
    "isSpicy": true,
    "containsEgg": true,
    "variants": [],
    "addons": [],
    "tags": ["spicy", "bestseller"],
    "imageUrl": "https://example.com/burger.jpg",
    "displayOrder": 1
  }'
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Menu item updated successfully",
  "data": { ... }
}
```

### 16. Patch Menu Item

**Endpoint**: `PATCH /api/restaurant-admin/menu-items/{id}`

**Description**: Partially update a menu item.

**cURL Example**:
```bash
curl -X PATCH http://localhost:8080/api/restaurant-admin/menu-items/b2c3d4e5-f6a7-8901-2345-678901bcdefg \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "price": 14.99
  }'
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Menu item updated successfully",
  "data": { ... }
}
```

### 17. Delete Menu Item

**Endpoint**: `DELETE /api/restaurant-admin/menu-items/{id}`

**Description**: Delete a menu item.

**cURL Example**:
```bash
curl -X DELETE http://localhost:8080/api/restaurant-admin/menu-items/b2c3d4e5-f6a7-8901-2345-678901bcdefg \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Menu item deleted successfully",
  "data": null
}
```

### 18. Toggle Availability

**Endpoint**: `PATCH /api/restaurant-admin/menu-items/{id}/toggle-availability`

**Description**: Toggle the availability status of a menu item.

**cURL Example**:
```bash
curl -X PATCH http://localhost:8080/api/restaurant-admin/menu-items/b2c3d4e5-f6a7-8901-2345-678901bcdefg/toggle-availability \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Availability toggled successfully",
  "data": {
    "id": "...",
    "isAvailable": false,
    // ...
  }
}
```

---

## Staff Management

### 19. Create Staff

**Endpoint**: `POST /api/v1/restaurant-admin/staff`

**Description**: Create a new staff member.

**Request Body**:
```json
{
  "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
  "employeeCode": "EMP001",
  "staffRole": "WAITER",
  "designation": "Senior Waiter",
  "userName": "john_doe",
  "password": "securePassword123",
  "email": "john@example.com",
  "phone": "+1234567890",
  "status": "ACTIVE",
  "isActive": true
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/v1/restaurant-admin/staff \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
    "employeeCode": "EMP001",
    "staffRole": "WAITER",
    "designation": "Senior Waiter",
    "userName": "john_doe",
    "password": "securePassword123",
    "email": "john@example.com",
    "phone": "+1234567890",
    "status": "ACTIVE",
    "isActive": true
  }'
```

**Success Response** (201 Created):
```json
{
  "success": true,
  "message": "Staff created successfully",
  "data": {
    "id": "c3d4e5f6-a7b8-9012-3456-789012cdefgh",
    "userName": "john_doe",
    // ... other fields (password excluded)
  }
}
```

### 20. Get All Staff

**Endpoint**: `GET /api/v1/restaurant-admin/staff`

**Description**: Get all staff members for the authenticated restaurant.

**cURL Example**:
```bash
curl -X GET "http://localhost:8080/api/v1/restaurant-admin/staff" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Staff retrieved successfully",
  "data": [ ... ]
}
```

### 21. Get Staff by ID

**Endpoint**: `GET /api/v1/restaurant-admin/staff/{id}`

**Description**: Get a staff member by UUID.

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/v1/restaurant-admin/staff/c3d4e5f6-a7b8-9012-3456-789012cdefgh \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Staff retrieved successfully",
  "data": { ... }
}
```

### 22. Update Staff

**Endpoint**: `PUT /api/v1/restaurant-admin/staff/{id}`

**Description**: Fully update a staff member.

**cURL Example**:
```bash
curl -X PUT http://localhost:8080/api/v1/restaurant-admin/staff/c3d4e5f6-a7b8-9012-3456-789012cdefgh \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "550e8400-e29b-41d4-a716-446655440000",
    "employeeCode": "EMP001",
    "staffRole": "WAITER",
    "designation": "Head Waiter",
    "userName": "john_doe_updated",
    "email": "john.updated@example.com",
    "phone": "+1234567890",
    "status": "ACTIVE",
    "isActive": true
  }'
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Staff updated successfully",
  "data": { ... }
}
```

### 23. Patch Staff

**Endpoint**: `PATCH /api/v1/restaurant-admin/staff/{id}`

**Description**: Partially update a staff member.

**cURL Example**:
```bash
curl -X PATCH http://localhost:8080/api/v1/restaurant-admin/staff/c3d4e5f6-a7b8-9012-3456-789012cdefgh \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "designation": "Manager"
  }'
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Staff updated successfully",
  "data": { ... }
}
```

### 24. Delete Staff

**Endpoint**: `DELETE /api/v1/restaurant-admin/staff/{id}`

**Description**: Delete a staff member.

**cURL Example**:
```bash
curl -X DELETE http://localhost:8080/api/v1/restaurant-admin/staff/c3d4e5f6-a7b8-9012-3456-789012cdefgh \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Staff deleted successfully",
  "data": null
}
```

### 25. Update Staff Status

**Endpoint**: `PATCH /api/v1/restaurant-admin/staff/{id}/status`

**Description**: Update the status of a staff member.

**Query Parameters**:
- `status` (required): New status (e.g., ACTIVE, SUSPENDED, RESIGNED)

**cURL Example**:
```bash
curl -X PATCH "http://localhost:8080/api/v1/restaurant-admin/staff/c3d4e5f6-a7b8-9012-3456-789012cdefgh/status?status=SUSPENDED" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Status updated successfully",
  "data": {
    "id": "...",
    "status": "SUSPENDED",
    // ...
  }
}
```

---

## Error Handling

All endpoints follow a consistent error response format:

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

### Common HTTP Status Codes:
- **200 OK**: Successful GET, PUT, PATCH, DELETE
- **201 Created**: Successful POST
- **400 Bad Request**: Validation errors, business logic errors
- **401 Unauthorized**: Missing or invalid JWT token
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Unexpected server errors
