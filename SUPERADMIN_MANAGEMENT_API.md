# SuperAdmin Restaurant Management API

This document provides `curl` examples for all SuperAdmin restaurant management endpoints, with a short description for each.

---

## 1. Create a New Restaurant

_Create a new restaurant profile._

```bash
curl -X POST "http://localhost:8080/api/superadmin/restaurants" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Yeti the Himalyan kitchen",
    "description": "A new restaurant",
    "addressLine1": "Pune",
    "addressLine2": "kalyani nagar",
    "city": "New York",
    "state": "NY",
    "pincode": "10001",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "status": "open",
    "restaurantType": "Italian",
    "rating": 4.5,
    "isPopular": true
  }'
```

---

## 2. Get All Restaurants

_Retrieve a list of all restaurants._

```bash
curl -X GET "http://localhost:8080/api/superadmin/restaurants" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 3. Get Restaurant by ID

_Get details of a specific restaurant by its ID._

```bash
curl -X GET "http://localhost:8080/api/superadmin/restaurants/<RESTAURANT_ID>" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 4. Search Restaurants by Name

_Search for restaurants by name._

```bash
curl -X GET "http://localhost:8080/api/superadmin/restaurants/search?name=Pizza" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 5. Advanced Search

_Search restaurants using multiple filters (city, status, type, rating, etc.)._

```bash
curl -X GET "http://localhost:8080/api/superadmin/restaurants/advanced-search?city=Delhi&status=open" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 6. Get Restaurants by City

_Get all restaurants in a specific city._

```bash
curl -X GET "http://localhost:8080/api/superadmin/restaurants/by-city/Delhi" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 7. Get Restaurants by Status

_Get all restaurants with a specific status (e.g., open, closed)._

```bash
curl -X GET "http://localhost:8080/api/superadmin/restaurants/by-status/open" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 8. Get Popular Restaurants

_Retrieve a list of popular restaurants._

```bash
curl -X GET "http://localhost:8080/api/superadmin/restaurants/popular" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 9. Update Restaurant (PUT)

_Update all details of a restaurant._

```bash
curl -X PUT "http://localhost:8080/api/superadmin/restaurants/<RESTAURANT_ID>" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Restaurant Name",
    "description": "Updated description",
    "addressLine1": "123 Main St",
    "addressLine2": "Suite 100",
    "city": "New York",
    "state": "NY",
    "pincode": "10001",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "status": "open",
    "restaurantType": "Italian",
    "rating": 4.5,
    "isPopular": true
  }'
```

---

## 10. Patch Restaurant (PATCH)

_Partially update one or more fields of a restaurant._

```bash
curl -X PATCH "http://localhost:8080/api/superadmin/restaurants/<RESTAURANT_ID>" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Patched Restaurant Name",
    "description": "Patched description",
    "addressLine1": "456 Broadway",
    "addressLine2": "Floor 2",
    "city": "Brooklyn",
    "state": "NY",
    "pincode": "11201",
    "latitude": 40.6782,
    "longitude": -73.9442,
    "status": "closed",
    "restaurantType": "Mexican",
    "rating": 3.9,
    "isPopular": false
  }'
```

---

## 11. Update Restaurant Status

_Update only the status of a restaurant._

```bash
curl -X PATCH "http://localhost:8080/api/superadmin/restaurants/<RESTAURANT_ID>/status?status=closed" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 12. Toggle Popular Status

_Toggle the 'popular' flag for a restaurant._

```bash
curl -X PATCH "http://localhost:8080/api/superadmin/restaurants/<RESTAURANT_ID>/toggle-popular" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 13. Delete Restaurant

_Delete a restaurant by its ID._

```bash
curl -X DELETE "http://localhost:8080/api/superadmin/restaurants/<RESTAURANT_ID>" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 14. Get Statistics

_Get statistics about restaurants (e.g., counts by status, type, etc.)._

```bash
curl -X GET "http://localhost:8080/api/superadmin/restaurants/stats" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 15. Onboard Restaurant Admin (POST)

_Onboard the first restaurant admin staff for a restaurant._

```bash
curl -X POST "http://localhost:8080/api/superadmin/restaurants/staff/onboard-restaurant-admin" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "<RESTAURANT_ID>",
    "employeeCode": "EMP001",
    "designation": "Manager",
    "phone": "9876543210",
    "email": "manager@yeti.com",
    "userName": "admin_yeti",
    "password": "SecurePassword123"
  }'
```

---

## 16. Update Restaurant Staff (PUT)

_Update all details of a restaurant staff member._

```bash
curl -X PUT "http://localhost:8080/api/superadmin/restaurants/staff/<STAFF_ID>" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "<RESTAURANT_ID>",
    "employeeCode": "EMP001",
    "staffRole": "RESTAURANT_ADMIN",
    "designation": "General Manager",
    "phone": "9876543211",
    "email": "gm@yeti.com",
    "userName": "admin_yeti",
    "status": "ACTIVE",
    "isActive": true
  }'
```

---

## 17. Patch Restaurant Staff (PATCH)

_Partially update one or more fields of a restaurant staff member (e.g. changing status or role)._

```bash
curl -X PATCH "http://localhost:8080/api/superadmin/restaurants/staff/<STAFF_ID>" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "designation": "Area Manager",
    "status": "INACTIVE"
  }'
```

---

**Note:**  
Replace `<TOKEN>` with your JWT token, `<RESTAURANT_ID>` with the actual restaurant UUID, and `<STAFF_ID>` with the actual staff UUID.