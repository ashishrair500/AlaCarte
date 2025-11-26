# Restaurant Management API Documentation

## Overview
This API allows SuperAdmins to manage restaurant onboarding and profiles. SuperAdmins can perform full CRUD operations on restaurants.

**Base URL**: `/api/superadmin/restaurants`

**Authentication**: All endpoints require a valid JWT token in the `Authorization` header.

---

## Table of Contents
1. [Create Restaurant](#1-create-restaurant)
2. [Get All Restaurants](#2-get-all-restaurants)
3. [Get Restaurant by ID](#3-get-restaurant-by-id)
4. [Search Restaurants by Name](#4-search-restaurants-by-name)
5. [Advanced Search](#5-advanced-search)
6. [Get Restaurants by City](#6-get-restaurants-by-city)
7. [Get Restaurants by Status](#7-get-restaurants-by-status)
8. [Get Popular Restaurants](#8-get-popular-restaurants)
9. [Update Restaurant (Full)](#9-update-restaurant-full)
10. [Update Restaurant (Partial)](#10-update-restaurant-partial)
11. [Update Restaurant Status](#11-update-restaurant-status)
12. [Toggle Popular Status](#12-toggle-popular-status)
13. [Delete Restaurant](#13-delete-restaurant)
14. [Get Statistics](#14-get-statistics)

---

## 1. Create Restaurant

**Endpoint**: `POST /api/superadmin/restaurants`

**Description**: Create a new restaurant profile. SuperAdmin ID is extracted from the JWT token.

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "The Grand Hotel Restaurant",
  "description": "Fine dining experience with international cuisine",
  "addressLine1": "123 Main Street",
  "addressLine2": "Tower B, 5th Floor",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400001",
  "latitude": 19.0760,
  "longitude": 72.8777,
  "phone": "+91-9876543210",
  "email": "contact@grandhotel.com",
  "cuisineTypes": ["Indian", "Continental", "Chinese"],
  "priceRange": 3,
  "avgCostForTwo": 2500.00,
  "openingTime": "11:00:00",
  "closingTime": "23:00:00",
  "isVegOnly": false,
  "hasParking": true,
  "isPopular": false,
  "restaurantType": "fine_dine",
  "hotelStarCategory": 5,
  "serviceFee": 50.00,
  "reservationRequired": true,
  "dressCode": "Smart Casual",
  "menuQrUrl": "https://example.com/menu/qr",
  "featuredImageUrl": "https://example.com/images/featured.jpg",
  "galleryImages": [
    "https://example.com/images/gallery1.jpg",
    "https://example.com/images/gallery2.jpg"
  ],
  "status": "open"
}
```

**Field Descriptions**:
- `name` (required): Restaurant name (max 255 chars)
- `description`: Detailed description
- `addressLine1` (required): Primary address
- `addressLine2`: Secondary address
- `city` (required): City name
- `state` (required): State/Province
- `pincode` (required): Postal/ZIP code
- `latitude`, `longitude`: GPS coordinates
- `phone`: Contact phone number
- `email`: Contact email (validated format)
- `cuisineTypes`: Array of cuisine types
- `priceRange`: 1-3 (1=budget, 2=moderate, 3=expensive)
- `avgCostForTwo`: Average cost for two people
- `openingTime`, `closingTime`: Operating hours (HH:mm:ss)
- `isVegOnly`: Vegetarian-only restaurant
- `hasParking`: Parking availability
- `isPopular`: Featured/popular restaurant
- `restaurantType`: "cafe", "fine_dine", "hotel"
- `hotelStarCategory`: 1-5 star rating
- `serviceFee`: Service charge amount
- `reservationRequired`: Requires advance booking
- `dressCode`: Dress code requirements
- `menuQrUrl`: QR code URL for menu
- `featuredImageUrl`: Main image URL
- `galleryImages`: Array of gallery image URLs
- `status`: "open", "closed", "temp_closed"

**Success Response** (201 Created):
```json
{
  "success": true,
  "message": "Restaurant created successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "The Grand Hotel Restaurant",
    "description": "Fine dining experience with international cuisine",
    "addressLine1": "123 Main Street",
    "addressLine2": "Tower B, 5th Floor",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pincode": "400001",
    "latitude": 19.0760,
    "longitude": 72.8777,
    "phone": "+91-9876543210",
    "email": "contact@grandhotel.com",
    "cuisineTypes": ["Indian", "Continental", "Chinese"],
    "priceRange": 3,
    "avgCostForTwo": 2500.00,
    "openingTime": "11:00:00",
    "closingTime": "23:00:00",
    "rating": null,
    "totalRatings": 0,
    "isVegOnly": false,
    "hasParking": true,
    "isPopular": false,
    "restaurantType": "fine_dine",
    "hotelStarCategory": 5,
    "serviceFee": 50.00,
    "reservationRequired": true,
    "dressCode": "Smart Casual",
    "menuQrUrl": "https://example.com/menu/qr",
    "featuredImageUrl": "https://example.com/images/featured.jpg",
    "galleryImages": [
      "https://example.com/images/gallery1.jpg",
      "https://example.com/images/gallery2.jpg"
    ],
    "status": "open",
    "createdAt": "2025-01-15T10:30:00+00:00",
    "updatedAt": "2025-01-15T10:30:00+00:00",
    "createdBy": "987e6543-e21b-12d3-a456-426614174111"
  }
}
```

**Error Responses**:

400 Bad Request - Duplicate name:
```json
{
  "success": false,
  "message": "Restaurant with name 'The Grand Hotel Restaurant' already exists",
  "data": null
}
```

400 Bad Request - Duplicate email:
```json
{
  "success": false,
  "message": "Restaurant with email 'contact@grandhotel.com' already exists",
  "data": null
}
```

400 Bad Request - Validation error:
```json
{
  "success": false,
  "message": "Restaurant name is required",
  "data": null
}
```

---

## 2. Get All Restaurants

**Endpoint**: `GET /api/superadmin/restaurants`

**Description**: Retrieve all restaurants in the system.

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Restaurants retrieved successfully",
  "data": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "The Grand Hotel Restaurant",
      "city": "Mumbai",
      "status": "open",
      "rating": 4.5,
      "totalRatings": 120,
      "isPopular": true,
      // ... other fields
    },
    {
      "id": "223e4567-e89b-12d3-a456-426614174001",
      "name": "Cafe Mocha",
      "city": "Delhi",
      "status": "open",
      "rating": 4.2,
      // ... other fields
    }
  ]
}
```

---

## 3. Get Restaurant by ID

**Endpoint**: `GET /api/superadmin/restaurants/{id}`

**Description**: Retrieve a specific restaurant by its UUID.

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parameters**:
- `id`: Restaurant UUID

**Example**: `GET /api/superadmin/restaurants/123e4567-e89b-12d3-a456-426614174000`

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Restaurant retrieved successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "The Grand Hotel Restaurant",
    // ... all restaurant fields
  }
}
```

**Error Response** (404 Not Found):
```json
{
  "success": false,
  "message": "Restaurant not found",
  "data": null
}
```

---

## 4. Search Restaurants by Name

**Endpoint**: `GET /api/superadmin/restaurants/search`

**Description**: Search restaurants by name (case-insensitive, partial match).

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters**:
- `name` (required): Search term

**Example**: `GET /api/superadmin/restaurants/search?name=grand`

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Search results",
  "data": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "The Grand Hotel Restaurant",
      // ... other fields
    },
    {
      "id": "223e4567-e89b-12d3-a456-426614174001",
      "name": "Grand Cafe",
      // ... other fields
    }
  ]
}
```

---

## 5. Advanced Search

**Endpoint**: `GET /api/superadmin/restaurants/advanced-search`

**Description**: Search restaurants with multiple filters.

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters** (all optional):
- `city`: Filter by city
- `status`: Filter by status (open, closed, temp_closed)
- `restaurantType`: Filter by type (cafe, fine_dine, hotel)
- `minRating`: Minimum rating (decimal)
- `searchTerm`: Search in restaurant name

**Example**: 
```
GET /api/superadmin/restaurants/advanced-search?city=Mumbai&status=open&minRating=4.0&searchTerm=hotel
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Search results",
  "data": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "The Grand Hotel Restaurant",
      "city": "Mumbai",
      "status": "open",
      "rating": 4.5,
      // ... other fields
    }
  ]
}
```

---

## 6. Get Restaurants by City

**Endpoint**: `GET /api/superadmin/restaurants/by-city/{city}`

**Description**: Get all restaurants in a specific city.

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parameters**:
- `city`: City name

**Example**: `GET /api/superadmin/restaurants/by-city/Mumbai`

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Restaurants in Mumbai",
  "data": [
    // ... array of restaurants
  ]
}
```

---

## 7. Get Restaurants by Status

**Endpoint**: `GET /api/superadmin/restaurants/by-status/{status}`

**Description**: Get all restaurants with a specific status.

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parameters**:
- `status`: Status value (open, closed, temp_closed)

**Example**: `GET /api/superadmin/restaurants/by-status/open`

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Restaurants with status: open",
  "data": [
    // ... array of restaurants
  ]
}
```

---

## 8. Get Popular Restaurants

**Endpoint**: `GET /api/superadmin/restaurants/popular`

**Description**: Get all restaurants marked as popular.

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Popular restaurants",
  "data": [
    // ... array of popular restaurants
  ]
}
```

---

## 9. Update Restaurant (Full)

**Endpoint**: `PUT /api/superadmin/restaurants/{id}`

**Description**: Fully update a restaurant (all fields required).

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Path Parameters**:
- `id`: Restaurant UUID

**Request Body**: Same as Create Restaurant

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Restaurant updated successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    // ... updated restaurant data
  }
}
```

**Error Responses**:

400 Bad Request - Name conflict:
```json
{
  "success": false,
  "message": "Restaurant name already exists",
  "data": null
}
```

---

## 10. Update Restaurant (Partial)

**Endpoint**: `PATCH /api/superadmin/restaurants/{id}`

**Description**: Partially update a restaurant (only provided fields are updated).

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Path Parameters**:
- `id`: Restaurant UUID

**Request Body** (example - only updating phone and status):
```json
{
  "phone": "+91-9999999999",
  "status": "temp_closed"
}
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Restaurant updated successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "phone": "+91-9999999999",
    "status": "temp_closed",
    // ... other fields unchanged
  }
}
```

---

## 11. Update Restaurant Status

**Endpoint**: `PATCH /api/superadmin/restaurants/{id}/status`

**Description**: Update only the restaurant status.

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parameters**:
- `id`: Restaurant UUID

**Query Parameters**:
- `status` (required): New status (open, closed, temp_closed)

**Example**: 
```
PATCH /api/superadmin/restaurants/123e4567-e89b-12d3-a456-426614174000/status?status=closed
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Status updated successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "status": "closed",
    // ... other fields
  }
}
```

---

## 12. Toggle Popular Status

**Endpoint**: `PATCH /api/superadmin/restaurants/{id}/toggle-popular`

**Description**: Toggle the popular status of a restaurant (true ↔ false).

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parameters**:
- `id`: Restaurant UUID

**Example**: 
```
PATCH /api/superadmin/restaurants/123e4567-e89b-12d3-a456-426614174000/toggle-popular
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Popular status toggled",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "isPopular": true,
    // ... other fields
  }
}
```

---

## 13. Delete Restaurant

**Endpoint**: `DELETE /api/superadmin/restaurants/{id}`

**Description**: Permanently delete a restaurant from the system.

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parameters**:
- `id`: Restaurant UUID

**Example**: 
```
DELETE /api/superadmin/restaurants/123e4567-e89b-12d3-a456-426614174000
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Restaurant deleted successfully",
  "data": null
}
```

**Error Response** (404 Not Found):
```json
{
  "success": false,
  "message": "Restaurant not found with id: 123e4567-e89b-12d3-a456-426614174000",
  "data": null
}
```

---

## 14. Get Statistics

**Endpoint**: `GET /api/superadmin/restaurants/stats`

**Description**: Get statistical summary of restaurants.

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Statistics retrieved",
  "data": {
    "total": 150,
    "open": 120,
    "closed": 20,
    "temp_closed": 10
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

---

## Authentication

All endpoints require a valid JWT token in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

The token should be obtained from the SuperAdmin login endpoint (`/api/auth/superadmin/login`).

---

## Use Cases

### 1. Onboard a New Restaurant
```bash
POST /api/superadmin/restaurants
# Provide full restaurant details
```

### 2. Search for Restaurants
```bash
# By name
GET /api/superadmin/restaurants/search?name=cafe

# Advanced search
GET /api/superadmin/restaurants/advanced-search?city=Mumbai&minRating=4.0
```

### 3. Update Restaurant Profile
```bash
# Full update
PUT /api/superadmin/restaurants/{id}

# Partial update (e.g., just phone number)
PATCH /api/superadmin/restaurants/{id}
```

### 4. Manage Restaurant Status
```bash
# Temporarily close a restaurant
PATCH /api/superadmin/restaurants/{id}/status?status=temp_closed

# Reopen
PATCH /api/superadmin/restaurants/{id}/status?status=open
```

### 5. Feature Popular Restaurants
```bash
# Mark as popular
PATCH /api/superadmin/restaurants/{id}/toggle-popular
```

### 6. Monitor System
```bash
# Get statistics
GET /api/superadmin/restaurants/stats

# View all restaurants
GET /api/superadmin/restaurants
```

---

## Notes

1. **SuperAdmin Tracking**: The `createdBy` field automatically tracks which SuperAdmin created the restaurant.

2. **Validation**: All required fields are validated. Email format is checked automatically.

3. **Uniqueness**: Restaurant names and emails must be unique across the system.

4. **Timestamps**: `createdAt` and `updatedAt` are automatically managed by the system.

5. **Ratings**: `rating` and `totalRatings` are calculated fields that will be updated by the review system (future implementation).

6. **Images**: Gallery images are stored as an array of URLs. Actual image upload/storage should be handled separately.

7. **Operating Hours**: Use 24-hour format (HH:mm:ss) for `openingTime` and `closingTime`.

---

## Example Workflow: Complete Restaurant Onboarding

```bash
# 1. SuperAdmin logs in
POST /api/auth/superadmin/login
# Save the returned JWT token

# 2. Create restaurant
POST /api/superadmin/restaurants
Authorization: Bearer <token>
# Provide restaurant details

# 3. Verify creation
GET /api/superadmin/restaurants/{id}
Authorization: Bearer <token>

# 4. Mark as popular (if needed)
PATCH /api/superadmin/restaurants/{id}/toggle-popular
Authorization: Bearer <token>

# 5. Check statistics
GET /api/superadmin/restaurants/stats
Authorization: Bearer <token>
```

