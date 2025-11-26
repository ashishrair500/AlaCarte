# Restaurant Admin API Documentation

## Overview
This API allows Restaurant Admins to manage menus, menu items, and staff members. Restaurant Admins can perform full CRUD operations on these resources for their specific restaurant.

**Base URL**: `/api/restaurant-admin`

**Authentication**: All endpoints require a valid JWT token in the `Authorization` header with the `RESTAURANT_ADMIN` role.

---

## Table of Contents
1. [Menu Management](#menu-management)
    - [Create Menu](#1-create-menu)
    - [Get All Menus](#2-get-all-menus)
    - [Get Menu by ID](#3-get-menu-by-id)
    - [Update Menu](#4-update-menu)
    - [Patch Menu](#5-patch-menu)
    - [Delete Menu](#6-delete-menu)
2. [Menu Item Management](#menu-item-management)
    - [Create Menu Item](#7-create-menu-item)
    - [Get Menu Items by Menu](#8-get-menu-items-by-menu)
    - [Get Menu Item by ID](#9-get-menu-item-by-id)
    - [Update Menu Item](#10-update-menu-item)
    - [Patch Menu Item](#11-patch-menu-item)
    - [Delete Menu Item](#12-delete-menu-item)
    - [Toggle Availability](#13-toggle-availability)
3. [Staff Management](#staff-management)
    - [Create Staff](#14-create-staff)
    - [Get All Staff](#15-get-all-staff)
    - [Get Staff by ID](#16-get-staff-by-id)
    - [Update Staff](#17-update-staff)
    - [Patch Staff](#18-patch-staff)
    - [Delete Staff](#19-delete-staff)
    - [Update Staff Status](#20-update-staff-status)

---

## Menu Management

### 1. Create Menu

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
  "restaurantId": 1,
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
    "restaurantId": 1,
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
    "restaurantId": 1,
    "name": "Summer Special",
    // ... other fields
  }
}
```

### 2. Get All Menus

**Endpoint**: `GET /api/restaurant-admin/menus`

**Description**: Get all menus for a specific restaurant.

**Query Parameters**:
- `restaurantId` (required): ID of the restaurant

**Example**: `GET /api/restaurant-admin/menus?restaurantId=1`

**cURL Example**:
```bash
curl -X GET "http://localhost:8080/api/restaurant-admin/menus?restaurantId=1" \
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

### 3. Get Menu by ID

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

### 4. Update Menu

**Endpoint**: `PUT /api/restaurant-admin/menus/{id}`

**Description**: Fully update a menu.

**Request Body**: Same as Create Menu.

**cURL Example**:
```bash
curl -X PUT http://localhost:8080/api/restaurant-admin/menus/a1b2c3d4-e5f6-7890-1234-567890abcdef \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": 1,
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

### 5. Patch Menu

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

### 6. Delete Menu

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

### 7. Create Menu Item

**Endpoint**: `POST /api/restaurant-admin/menu-items`

**Description**: Create a new menu item.

**Request Body**:
```json
{
  "restaurantId": 1,
  "menuId": 10,
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
    "restaurantId": 1,
    "menuId": 10,
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

### 8. Get Menu Items by Menu

**Endpoint**: `GET /api/restaurant-admin/menu-items`

**Description**: Get all items for a specific menu.

**Query Parameters**:
- `menuId` (required): ID of the menu (Long)

**Example**: `GET /api/restaurant-admin/menu-items?menuId=10`

**cURL Example**:
```bash
curl -X GET "http://localhost:8080/api/restaurant-admin/menu-items?menuId=10" \
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

### 9. Get Menu Item by ID

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

### 10. Update Menu Item

**Endpoint**: `PUT /api/restaurant-admin/menu-items/{id}`

**Description**: Fully update a menu item.

**cURL Example**:
```bash
curl -X PUT http://localhost:8080/api/restaurant-admin/menu-items/b2c3d4e5-f6a7-8901-2345-678901bcdefg \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": 1,
    "menuId": 10,
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

### 11. Patch Menu Item

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

### 12. Delete Menu Item

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

### 13. Toggle Availability

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

### 14. Create Staff

**Endpoint**: `POST /api/restaurant-admin/staff`

**Description**: Create a new staff member.

**Request Body**:
```json
{
  "restaurantId": 1,
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
curl -X POST http://localhost:8080/api/restaurant-admin/staff \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": 1,
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

### 15. Get All Staff

**Endpoint**: `GET /api/restaurant-admin/staff`

**Description**: Get all staff members for a restaurant.

**Query Parameters**:
- `restaurantId` (required): ID of the restaurant

**cURL Example**:
```bash
curl -X GET "http://localhost:8080/api/restaurant-admin/staff?restaurantId=1" \
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

### 16. Get Staff by ID

**Endpoint**: `GET /api/restaurant-admin/staff/{id}`

**Description**: Get a staff member by UUID.

**cURL Example**:
```bash
curl -X GET http://localhost:8080/api/restaurant-admin/staff/c3d4e5f6-a7b8-9012-3456-789012cdefgh \
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

### 17. Update Staff

**Endpoint**: `PUT /api/restaurant-admin/staff/{id}`

**Description**: Fully update a staff member.

**cURL Example**:
```bash
curl -X PUT http://localhost:8080/api/restaurant-admin/staff/c3d4e5f6-a7b8-9012-3456-789012cdefgh \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": 1,
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

### 18. Patch Staff

**Endpoint**: `PATCH /api/restaurant-admin/staff/{id}`

**Description**: Partially update a staff member.

**cURL Example**:
```bash
curl -X PATCH http://localhost:8080/api/restaurant-admin/staff/c3d4e5f6-a7b8-9012-3456-789012cdefgh \
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

### 19. Delete Staff

**Endpoint**: `DELETE /api/restaurant-admin/staff/{id}`

**Description**: Delete a staff member.

**cURL Example**:
```bash
curl -X DELETE http://localhost:8080/api/restaurant-admin/staff/c3d4e5f6-a7b8-9012-3456-789012cdefgh \
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

### 20. Update Staff Status

**Endpoint**: `PATCH /api/restaurant-admin/staff/{id}/status`

**Description**: Update the status of a staff member.

**Query Parameters**:
- `status` (required): New status (e.g., ACTIVE, SUSPENDED, RESIGNED)

**cURL Example**:
```bash
curl -X PATCH "http://localhost:8080/api/restaurant-admin/staff/c3d4e5f6-a7b8-9012-3456-789012cdefgh/status?status=SUSPENDED" \
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
