# AlaCarte Restaurant Admin Dashboard API Review

This document is based on the code under `src/main/java/com/alaCarte/menu`. It is intended for the Restaurant Admin dashboard frontend and documents the APIs in the same sequence the frontend will usually consume them, starting from auth.

## 1. Scope

- API base URL: `http://localhost:8080`
- API prefix: `/api/v1`
- Auth scheme: `Authorization: Bearer <accessToken>`
- Content type: `application/json`
- Protected scope: the backend derives `restaurantId` and `staffId` from the authenticated request context for menu-module APIs

## 2. Standard Response Contract

All endpoints return the shared `ApiResponse<T>` envelope:

```json
{
  "success": true,
  "message": "Success",
  "data": {},
  "error": null,
  "errorCode": null,
  "timestamp": "2026-04-06T12:30:00+05:30"
}
```

Error shape:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": null,
  "error": {
    "fieldName": "field level message"
  },
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2026-04-06T12:30:00+05:30"
}
```

Global exception mapping used by the menu module:

| Status | When |
|---|---|
| `400` | validation failures and custom business validation errors |
| `401` | invalid login, invalid token, expired token |
| `403` | cross-restaurant access or missing scoped request context |
| `404` | menu, item, category, variant, combo, tag, addon, or link not found |
| `409` | duplicate menu names, duplicate item names, duplicate tag slugs, duplicate tag links, duplicate addon-group links |
| `500` | unhandled server error |

### Pagination Shape

Paginated endpoints return Spring Data `Page<T>` inside `data`:

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [],
    "pageable": {},
    "totalElements": 0,
    "totalPages": 0,
    "size": 20,
    "number": 0,
    "first": true,
    "last": true,
    "numberOfElements": 0,
    "sort": {}
  }
}
```

Pagination rules from the service layer:

- default `page=0`
- default `size=20`
- max `size=100`
- default sort is module-specific, usually `displayOrder,asc`

## 3. Frontend Review Notes

These are important code-backed findings that the frontend team should know:

- `permissions` in auth and staff payloads is a JSON object, not an array.
- Login response `fullName` is currently populated from `RestaurantStaff.designation`, not from a dedicated full-name field.
- JWT validation for this module always checks for `ROLE_RESTAURANT_ADMIN`; managers can log in, but the generated token still carries that single role.
- `RestaurantStaffRequest` marks `restaurantId` as required on create and full update. The controller overwrites it from JWT scope, but the frontend still needs to send it today to satisfy validation.
- `PATCH /api/v1/restaurant-admin/staff/{id}/status` uses a query parameter named `status`, not a JSON body.
- Menu meta can be updated with `PATCH /api/v1/menus/{menuId}/meta`, but `MenuResponseDTO` does not return the `meta` object afterward.
- `MenuCategoryResponseDTO` includes `isActive`, but category create and update payloads have no field to manage it. There is also no dedicated category status API.
- `AddonGroupResponseDTO` and `AddonGroupDetailResponseDTO` include `isRequired`, but `AddonGroupRequestDTO` has no field to set it. It stays on the entity default unless changed elsewhere.
- Addon-group-to-item link order can be updated, but the read APIs return group-level `displayOrder`, not the mapping row's order value. The frontend cannot fully rehydrate item-specific addon-group ordering from the current response DTOs.
- Menu item detail includes `variants`, `addonGroups`, and `tags`, so item detail can power a rich editor page.
- Menu delete, item delete, variant delete, addon-group delete, addon delete, combo delete, combo-slot delete, and tag-link delete are soft deletes.
- Menu create and menu item create auto-assign a next display order when the request omits a positive value. Most other modules default `displayOrder` to `0`.

## 4. Module and UI Map

| # | Module | Backend Area | Recommended Frontend Pages / Sections |
|---|---|---|---|
| 1 | Auth | login, refresh, validate, me | Login page, session bootstrap, profile widget |
| 2 | Staff Management | staff CRUD | Staff list page, create/edit drawer, staff detail panel |
| 3 | Menu Management | menu CRUD | Menus list page, menu create/edit drawer |
| 4 | Menu Categories | category CRUD per menu | Menu detail -> Categories tab |
| 5 | Menu Items | item CRUD, filters, imports, utility lists | Menu detail -> Items page, bulk import modal, item detail page |
| 6 | Menu Item Variants | variant CRUD per item | Item detail -> Variants section |
| 7 | Addon Groups | reusable addon-group library | Addon groups settings page |
| 8 | Addon Group Links | attach addon groups to items | Item detail -> Addons section |
| 9 | Addons | add-ons inside a group | Addon group detail drawer/page |
| 10 | Combos | combo CRUD per menu | Menu detail -> Combos page |
| 11 | Combo Items | combo slot CRUD | Combo detail -> Slot editor |
| 12 | Tags | reusable tag library | Tags settings page |
| 13 | Item Tag Links | assign tags to items | Item detail -> Tags section |

## 5. Shared cURL Setup

Use these shell variables in examples:

```bash
export BASE_URL="http://localhost:8080"
export ACCESS_TOKEN="<access-token>"
```

For protected APIs:

```bash
-H "Authorization: Bearer $ACCESS_TOKEN"
-H "Content-Type: application/json"
```

## 1. Module: Auth

Base path: `/api/v1/auth/restaurant-admin`

Recommended frontend surfaces:

- Login page
- silent token refresh handler
- boot-time session validation
- top-bar profile bootstrap

### Auth response shapes

`POST /login` returns:

```json
{
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "username": "admin_john",
  "fullName": "Floor Manager",
  "email": "admin@restaurant.com",
  "permissions": {
    "menu": true,
    "staff": true
  },
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "message": "Login successful"
}
```

### 1.1 Login

**POST** `/api/v1/auth/restaurant-admin/login`

Purpose: authenticate a restaurant admin or manager and return tokens plus the scoped dashboard profile payload.

Request body:

```json
{
  "usernameOrEmail": "admin@restaurant.com",
  "password": "SecurePass@123"
}
```

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/auth/restaurant-admin/login" \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin@restaurant.com",
    "password": "SecurePass@123"
  }'
```

Frontend notes:

- only staff with `staffRole` `RESTAURANT_ADMIN` or `MANAGER`, `status=ACTIVE`, `isActive=true`, and `deletedAt=null` can log in here
- `fullName` is actually populated from `designation`
- `permissions` is a JSON object

Common responses:

- `200` login success
- `401` invalid username/email or password
- `401` user not eligible for restaurant-admin access
- `500` unexpected login failure

### 1.2 Refresh Access Token

**POST** `/api/v1/auth/restaurant-admin/refresh`

Purpose: exchange a refresh token for a new access token.

Query params:

- `refreshToken` required

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/auth/restaurant-admin/refresh?refreshToken=<refresh-token>"
```

Success payload:

```json
{
  "accessToken": "new-jwt-access-token",
  "tokenType": "Bearer",
  "expiresIn": "86400"
}
```

Frontend notes:

- this endpoint uses a query parameter, not a JSON body
- `expiresIn` is returned as a string in the current implementation

### 1.3 Validate Token

**POST** `/api/v1/auth/restaurant-admin/validate`

Purpose: validate a token during bootstrapping or troubleshooting.

Query params:

- `token` required

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/auth/restaurant-admin/validate?token=<access-token>"
```

Success payload:

```json
{
  "valid": true,
  "username": "admin_john",
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "roles": [
    "ROLE_RESTAURANT_ADMIN"
  ]
}
```

Frontend notes:

- role validation is hardcoded to `ROLE_RESTAURANT_ADMIN`
- managers who log in still receive that role in the token

### 1.4 Get Current User

**GET** `/api/v1/auth/restaurant-admin/me`

Purpose: fetch the current authenticated user's dashboard profile.

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/auth/restaurant-admin/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Success payload:

```json
{
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "username": "admin_john",
  "email": "admin@restaurant.com",
  "staffRole": "RESTAURANT_ADMIN",
  "restaurantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "ACTIVE",
  "permissions": {
    "menu": true,
    "staff": true
  }
}
```

Frontend notes:

- send the header as `Authorization: Bearer <token>`
- profile bootstrap can use this payload to drive role-based navigation

## 2. Module: Staff Management

Base path: `/api/v1/restaurant-admin/staff`

Recommended frontend surfaces:

- Staff list page
- create staff form
- edit staff form
- status action modal
- optional staff detail side panel

Known domain values from the entity and auth checks:

- common `staffRole` values: `RESTAURANT_ADMIN`, `MANAGER`, `CASHIER`, `WAITER`, `CHEF`, `DELIVERY`
- current entity `status` values are documented as `ACTIVE`, `SUSPENDED`, `RESIGNED`, `TERMINATED`

### Staff request payload

```json
{
  "restaurantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "employeeCode": "EMP-001",
  "staffRole": "MANAGER",
  "designation": "Floor Manager",
  "reportingTo": "11111111-2222-3333-4444-555555555555",
  "phone": "+91-9876543210",
  "email": "manager@restaurant.com",
  "userName": "manager_01",
  "password": "SecurePass@123",
  "status": "ACTIVE",
  "joinedAt": "2026-04-01T09:00:00+05:30",
  "leftAt": null,
  "shiftMeta": {
    "timezone": "Asia/Kolkata",
    "weeklyOff": "MONDAY"
  },
  "permissions": {
    "menu": true,
    "staff": false
  },
  "meta": {
    "notes": "Opening shift owner"
  },
  "isActive": true
}
```

Critical frontend note:

- `restaurantId` is required by DTO validation on create and full update, but the controller overwrites it with the restaurant from JWT scope. Send the same restaurant ID as the logged-in user to avoid validation failure.

### 2.1 Create Staff Member

**POST** `/api/v1/restaurant-admin/staff`

Purpose: create a staff user for the authenticated restaurant.

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/restaurant-admin/staff" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "employeeCode": "EMP-001",
    "staffRole": "MANAGER",
    "designation": "Floor Manager",
    "reportingTo": null,
    "phone": "+91-9876543210",
    "email": "manager@restaurant.com",
    "userName": "manager_01",
    "password": "SecurePass@123",
    "status": "ACTIVE",
    "joinedAt": "2026-04-01T09:00:00+05:30",
    "leftAt": null,
    "shiftMeta": {
      "timezone": "Asia/Kolkata"
    },
    "permissions": {
      "menu": true,
      "staff": false
    },
    "meta": {
      "notes": "Opening shift owner"
    },
    "isActive": true
  }'
```

Returns: `RestaurantStaffResponse`

Common responses:

- `201` created
- `400` duplicate employee code, username, or email, or validation issue
- `500` server failure

### 2.2 Get All Staff

**GET** `/api/v1/restaurant-admin/staff`

Purpose: load the full staff table for the logged-in restaurant.

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/restaurant-admin/staff" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `List<RestaurantStaffResponse>`

Frontend notes:

- there are no filters on this controller today
- filtering by status, active flag, role, or search must be done client-side unless new APIs are added

### 2.3 Get Staff by ID

**GET** `/api/v1/restaurant-admin/staff/{id}`

Purpose: open a single staff record for editing or read-only inspection.

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/restaurant-admin/staff/3fa85f64-5717-4562-b3fc-2c963f66afa6" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `RestaurantStaffResponse`

Common responses:

- `200` success
- `403` staff belongs to a different restaurant
- `404` staff not found

### 2.4 Update Staff

**PUT** `/api/v1/restaurant-admin/staff/{id}`

Purpose: full replacement-style update for a staff record.

Request body: same schema as create.

cURL:

```bash
curl -X PUT "$BASE_URL/api/v1/restaurant-admin/staff/3fa85f64-5717-4562-b3fc-2c963f66afa6" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "employeeCode": "EMP-001",
    "staffRole": "MANAGER",
    "designation": "Senior Floor Manager",
    "reportingTo": null,
    "phone": "+91-9876543210",
    "email": "manager@restaurant.com",
    "userName": "manager_01",
    "password": "NewSecurePass@123",
    "status": "ACTIVE",
    "joinedAt": "2026-04-01T09:00:00+05:30",
    "leftAt": null,
    "shiftMeta": {
      "timezone": "Asia/Kolkata"
    },
    "permissions": {
      "menu": true,
      "staff": true
    },
    "meta": {
      "notes": "Promoted"
    },
    "isActive": true
  }'
```

Frontend notes:

- if `password` is present and non-empty it is re-hashed and updated
- because this is `@Valid`, missing required fields like `restaurantId`, `staffRole`, or `userName` will fail validation

### 2.5 Patch Staff

**PATCH** `/api/v1/restaurant-admin/staff/{id}`

Purpose: partial update for staff fields.

Example request body:

```json
{
  "designation": "Night Shift Manager",
  "phone": "+91-9999999999",
  "permissions": {
    "menu": true,
    "staff": true
  },
  "isActive": true
}
```

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/restaurant-admin/staff/3fa85f64-5717-4562-b3fc-2c963f66afa6" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "designation": "Night Shift Manager",
    "phone": "+91-9999999999",
    "permissions": {
      "menu": true,
      "staff": true
    },
    "isActive": true
  }'
```

Frontend notes:

- this endpoint is not annotated with `@Valid`, so partial bodies are allowed
- if you include `password`, it will be hashed and updated

### 2.6 Update Staff Status

**PATCH** `/api/v1/restaurant-admin/staff/{id}/status`

Purpose: change employment status without sending the full object.

Query params:

- `status` required

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/restaurant-admin/staff/3fa85f64-5717-4562-b3fc-2c963f66afa6/status?status=RESIGNED" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Frontend notes:

- this endpoint uses a query parameter, not JSON
- when status becomes `RESIGNED` or `TERMINATED`, the service sets `leftAt`

### 2.7 Delete Staff

**DELETE** `/api/v1/restaurant-admin/staff/{id}`

Purpose: delete a staff record.

cURL:

```bash
curl -X DELETE "$BASE_URL/api/v1/restaurant-admin/staff/3fa85f64-5717-4562-b3fc-2c963f66afa6" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Frontend notes:

- current service method deletes the row through `staffService.deleteStaff(id)`
- use a confirmation modal in the UI because the endpoint is destructive

## 3. Module: Menu Management

Base path: `/api/v1/menus`

Recommended frontend surfaces:

- menus list page
- menu create/edit drawer
- sort/reorder interaction
- activation toggle

Suggested `menuType` examples from the entity comment:

- `BREAKFAST`
- `LUNCH`
- `BAR`
- `CUSTOM`

### Menu request payload

```json
{
  "name": "Lunch Menu",
  "description": "Weekday lunch service",
  "menuType": "LUNCH",
  "isActive": true,
  "validFrom": "2026-04-01T12:00:00+05:30",
  "validTo": "2026-10-01T23:59:59+05:30",
  "displayOrder": 1,
  "coverImage": "https://cdn.example.com/menus/lunch-cover.jpg"
}
```

### Menu response payload

```json
{
  "id": "menu-uuid",
  "name": "Lunch Menu",
  "description": "Weekday lunch service",
  "menuType": "LUNCH",
  "isActive": true,
  "validFrom": "2026-04-01T12:00:00+05:30",
  "validTo": "2026-10-01T23:59:59+05:30",
  "displayOrder": 1,
  "coverImage": "https://cdn.example.com/menus/lunch-cover.jpg",
  "totalCategories": 4,
  "totalItems": 28,
  "createdAt": "2026-04-01T12:00:00+05:30",
  "updatedAt": "2026-04-05T12:00:00+05:30"
}
```

### 3.1 Create Menu

**POST** `/api/v1/menus`

Purpose: create a new menu for the restaurant.

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/menus" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Lunch Menu",
    "description": "Weekday lunch service",
    "menuType": "LUNCH",
    "isActive": true,
    "validFrom": "2026-04-01T12:00:00+05:30",
    "validTo": "2026-10-01T23:59:59+05:30",
    "displayOrder": 1,
    "coverImage": "https://cdn.example.com/menus/lunch-cover.jpg"
  }'
```

Frontend notes:

- duplicate menu names in the same restaurant return `409`
- if `displayOrder` is omitted or not positive, the service assigns the next order automatically

### 3.2 Get Menus

**GET** `/api/v1/menus`

Purpose: list menus for the restaurant with server-side pagination and filtering.

Query params:

- `page` optional, default `0`
- `size` optional, default `20`, max `100`
- `sort` optional, default `displayOrder,asc`
- `isActive` optional
- `menuType` optional
- `search` optional

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/menus?page=0&size=20&sort=displayOrder,asc&isActive=true&menuType=LUNCH&search=lunch" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `Page<MenuResponseDTO>`

### 3.3 Get Menu by ID

**GET** `/api/v1/menus/{menuId}`

Purpose: load one menu before editing or opening its detail workspace.

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 3.4 Update Menu

**PUT** `/api/v1/menus/{menuId}`

Purpose: full update for a menu.

Request body: same schema as create.

cURL:

```bash
curl -X PUT "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Lunch Menu",
    "description": "Updated weekday lunch service",
    "menuType": "LUNCH",
    "isActive": true,
    "validFrom": "2026-04-01T12:00:00+05:30",
    "validTo": "2026-10-01T23:59:59+05:30",
    "displayOrder": 2,
    "coverImage": "https://cdn.example.com/menus/lunch-cover-v2.jpg"
  }'
```

### 3.5 Update Menu Status

**PATCH** `/api/v1/menus/{menuId}/status`

Purpose: activate or deactivate a menu without sending the full menu body.

Request body:

```json
{
  "isActive": false
}
```

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/status" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "isActive": false
  }'
```

### 3.6 Update Menu Display Order

**PATCH** `/api/v1/menus/{menuId}/order`

Purpose: change menu ordering in the dashboard.

Request body:

```json
{
  "displayOrder": 3
}
```

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/order" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "displayOrder": 3
  }'
```

### 3.7 Update Menu Meta

**PATCH** `/api/v1/menus/{menuId}/meta`

Purpose: store arbitrary menu metadata for frontend or operational use.

Request body:

```json
{
  "meta": {
    "theme": "summer",
    "showHeroBanner": true
  }
}
```

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/meta" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "meta": {
      "theme": "summer",
      "showHeroBanner": true
    }
  }'
```

Frontend note:

- the response is still `MenuResponseDTO`, which does not expose the saved `meta`. If the frontend needs to read back meta, the backend currently needs an API enhancement.

### 3.8 Delete Menu

**DELETE** `/api/v1/menus/{menuId}`

Purpose: soft delete a menu and mark it inactive.

cURL:

```bash
curl -X DELETE "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Frontend notes:

- delete sets `deleted=1` and `isActive=false`
- the menu will disappear from standard read APIs

## 4. Module: Menu Categories

Base path: `/api/v1/menus/{menuId}/categories`

Recommended frontend surfaces:

- Categories tab inside menu detail
- create/edit category drawer
- drag-and-drop or numeric ordering UI

### Category request payload

```json
{
  "name": "Starters",
  "description": "Soups and small plates",
  "imageUrl": "https://cdn.example.com/categories/starters.jpg",
  "displayOrder": 1,
  "parentCategoryId": null
}
```

### 4.1 Create Category

**POST** `/api/v1/menus/{menuId}/categories`

Purpose: create a category inside a menu.

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/categories" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Starters",
    "description": "Soups and small plates",
    "imageUrl": "https://cdn.example.com/categories/starters.jpg",
    "displayOrder": 1,
    "parentCategoryId": null
  }'
```

Frontend notes:

- `parentCategoryId` must belong to the same menu
- if `displayOrder` is omitted, category defaults to `0`

### 4.2 Get Categories

**GET** `/api/v1/menus/{menuId}/categories`

Purpose: load all categories for a menu ordered by `displayOrder`.

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/categories" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `List<MenuCategoryResponseDTO>`

Frontend note:

- this endpoint is not paginated

### 4.3 Get Category by ID

**GET** `/api/v1/menus/{menuId}/categories/{categoryId}`

Purpose: fetch one category for edit prefill.

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/categories/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 4.4 Update Category

**PUT** `/api/v1/menus/{menuId}/categories/{categoryId}`

Purpose: full update for a category.

Request body: same schema as create.

cURL:

```bash
curl -X PUT "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/categories/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Starters",
    "description": "Soups, salads, and small plates",
    "imageUrl": "https://cdn.example.com/categories/starters-v2.jpg",
    "displayOrder": 2,
    "parentCategoryId": null
  }'
```

Frontend note:

- there is no category status patch API even though the response contains `isActive`

### 4.5 Update Category Display Order

**PATCH** `/api/v1/menus/{menuId}/categories/{categoryId}/order`

Purpose: reorder categories within a menu.

Request body:

```json
{
  "displayOrder": 5
}
```

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/categories/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee/order" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "displayOrder": 5
  }'
```

### 4.6 Delete Category

**DELETE** `/api/v1/menus/{menuId}/categories/{categoryId}`

Purpose: soft delete a category.

cURL:

```bash
curl -X DELETE "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/categories/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

## 5. Module: Menu Items

Base path: `/api/v1/menus/{menuId}/items`

Recommended frontend surfaces:

- Items table with filters
- item create/edit drawer or full page
- bulk upload modal
- item detail page with tabs for variants, addons, and tags
- special collections widgets for popular, new, with variants, with addons, with nutrition

### Menu item request payload

```json
{
  "name": "Paneer Tikka",
  "description": "Char-grilled cottage cheese skewers",
  "aiDescription": "Smoky grilled paneer with Indian spices",
  "categoryId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
  "basePrice": 249.00,
  "discountPrice": 219.00,
  "isVeg": true,
  "isVegan": false,
  "isSpicy": true,
  "containsEgg": false,
  "containsGluten": false,
  "containsNuts": false,
  "containsDairy": true,
  "spiceLevel": 2,
  "prepTimeMinutes": 15,
  "imageUrl": "https://cdn.example.com/items/paneer-tikka.jpg",
  "gallery": [
    "https://cdn.example.com/items/paneer-tikka-1.jpg",
    "https://cdn.example.com/items/paneer-tikka-2.jpg"
  ],
  "ingredients": [
    "Paneer",
    "Yogurt",
    "Capsicum"
  ],
  "cuisineType": "Indian",
  "courseType": "Starter",
  "displayOrder": 1,
  "badge": "BESTSELLER",
  "nutrition": {
    "calories": 310,
    "protein": "18g"
  },
  "meta": {
    "recommendedPairing": "Mint Chutney"
  }
}
```

### 5.1 Create Menu Item

**POST** `/api/v1/menus/{menuId}/items`

Purpose: create one menu item.

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Paneer Tikka",
    "description": "Char-grilled cottage cheese skewers",
    "aiDescription": "Smoky grilled paneer with Indian spices",
    "categoryId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "basePrice": 249.00,
    "discountPrice": 219.00,
    "isVeg": true,
    "isVegan": false,
    "isSpicy": true,
    "containsEgg": false,
    "containsGluten": false,
    "containsNuts": false,
    "containsDairy": true,
    "spiceLevel": 2,
    "prepTimeMinutes": 15,
    "imageUrl": "https://cdn.example.com/items/paneer-tikka.jpg",
    "gallery": [
      "https://cdn.example.com/items/paneer-tikka-1.jpg",
      "https://cdn.example.com/items/paneer-tikka-2.jpg"
    ],
    "ingredients": [
      "Paneer",
      "Yogurt",
      "Capsicum"
    ],
    "cuisineType": "Indian",
    "courseType": "Starter",
    "displayOrder": 1,
    "badge": "BESTSELLER",
    "nutrition": {
      "calories": 310,
      "protein": "18g"
    },
    "meta": {
      "recommendedPairing": "Mint Chutney"
    }
  }'
```

Frontend notes:

- duplicate item names are checked within the same menu
- if `displayOrder` is omitted or not positive, the next order is auto-assigned
- `categoryId` is optional; if sent, it must belong to the same menu

### 5.2 Bulk Create Menu Items

**POST** `/api/v1/menus/{menuId}/items/bulk`

Purpose: bulk upload multiple menu items in one request.

Request body:

```json
[
  {
    "name": "Paneer Tikka",
    "description": "Char-grilled cottage cheese skewers",
    "categoryId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "basePrice": 249.00,
    "isVeg": true
  },
  {
    "name": "Veg Hakka Noodles",
    "description": "Street-style wok tossed noodles",
    "categoryId": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff",
    "basePrice": 199.00,
    "isVeg": true
  }
]
```

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items/bulk" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "name": "Paneer Tikka",
      "description": "Char-grilled cottage cheese skewers",
      "categoryId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
      "basePrice": 249.00,
      "isVeg": true
    },
    {
      "name": "Veg Hakka Noodles",
      "description": "Street-style wok tossed noodles",
      "categoryId": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff",
      "basePrice": 199.00,
      "isVeg": true
    }
  ]'
```

Frontend notes:

- empty arrays return `400`
- duplicate names inside the same upload return `409`
- duplicate names against existing menu items also return `409`
- missing or non-positive display order values are auto-sequenced during bulk upload

### 5.3 Get Items

**GET** `/api/v1/menus/{menuId}/items`

Purpose: list items with server-side pagination and filters.

Query params:

- `page` optional, default `0`
- `size` optional, default `20`, max `100`
- `sort` optional, default `displayOrder,asc`
- `categoryId` optional
- `isAvailable` optional
- `isVeg` optional
- `search` optional
- `minPrice` optional
- `maxPrice` optional
- `badge` optional
- `tag` optional
- `hasVariants` optional
- `hasAddons` optional

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items?page=0&size=20&sort=displayOrder,asc&categoryId=aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee&isAvailable=true&isVeg=true&search=paneer&minPrice=100&maxPrice=400&badge=BESTSELLER&tag=veg&hasVariants=false&hasAddons=true" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `Page<MenuItemResponseDTO>`

### 5.4 Get Item Detail

**GET** `/api/v1/menus/{menuId}/items/{itemId}`

Purpose: load a full item detail payload with nested variants, addon groups, and tags.

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items/99999999-8888-7777-6666-555555555555" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `MenuItemDetailResponseDTO`

Frontend notes:

- use this endpoint to power the item detail editor page
- addon groups are returned as group details with nested addons

### 5.5 Update Item

**PUT** `/api/v1/menus/{menuId}/items/{itemId}`

Purpose: full update for a menu item.

Request body: same schema as create.

cURL:

```bash
curl -X PUT "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items/99999999-8888-7777-6666-555555555555" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Paneer Tikka Deluxe",
    "description": "Char-grilled cottage cheese skewers with peppers",
    "aiDescription": "Smoky grilled paneer with Indian spices",
    "categoryId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "basePrice": 269.00,
    "discountPrice": 239.00,
    "isVeg": true,
    "isVegan": false,
    "isSpicy": true,
    "containsEgg": false,
    "containsGluten": false,
    "containsNuts": false,
    "containsDairy": true,
    "spiceLevel": 2,
    "prepTimeMinutes": 15,
    "imageUrl": "https://cdn.example.com/items/paneer-tikka-deluxe.jpg",
    "gallery": [
      "https://cdn.example.com/items/paneer-tikka-deluxe-1.jpg"
    ],
    "ingredients": [
      "Paneer",
      "Yogurt",
      "Capsicum"
    ],
    "cuisineType": "Indian",
    "courseType": "Starter",
    "displayOrder": 2,
    "badge": "CHEF_SPECIAL",
    "nutrition": {
      "calories": 330
    },
    "meta": {
      "recommendedPairing": "Mint Chutney"
    }
  }'
```

### 5.6 Update Item Availability

**PATCH** `/api/v1/menus/{menuId}/items/{itemId}/availability`

Purpose: toggle whether the item can be sold.

Request body:

```json
{
  "isAvailable": false
}
```

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items/99999999-8888-7777-6666-555555555555/availability" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "isAvailable": false
  }'
```

### 5.7 Reassign Item Category

**PATCH** `/api/v1/menus/{menuId}/items/{itemId}/category`

Purpose: move an item to another category in the same menu.

Request body:

```json
{
  "categoryId": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff"
}
```

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items/99999999-8888-7777-6666-555555555555/category" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff"
  }'
```

### 5.8 Update Item Display Order

**PATCH** `/api/v1/menus/{menuId}/items/{itemId}/order`

Purpose: reorder an item within the menu.

Request body:

```json
{
  "displayOrder": 4
}
```

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items/99999999-8888-7777-6666-555555555555/order" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "displayOrder": 4
  }'
```

### 5.9 Update Nutrition

**PATCH** `/api/v1/menus/{menuId}/items/{itemId}/nutrition`

Purpose: update only the nutrition JSON payload.

Request body:

```json
{
  "nutrition": {
    "calories": 320,
    "protein": "18g",
    "fat": "14g"
  }
}
```

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items/99999999-8888-7777-6666-555555555555/nutrition" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nutrition": {
      "calories": 320,
      "protein": "18g",
      "fat": "14g"
    }
  }'
```

### 5.10 Restore Soft-Deleted Item

**PATCH** `/api/v1/menus/{menuId}/items/{itemId}/restore`

Purpose: restore an item that was previously soft-deleted.

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items/99999999-8888-7777-6666-555555555555/restore" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Frontend notes:

- no request body
- this only works for items already marked deleted

### 5.11 Delete Item

**DELETE** `/api/v1/menus/{menuId}/items/{itemId}`

Purpose: soft delete an item and mark it unavailable.

cURL:

```bash
curl -X DELETE "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items/99999999-8888-7777-6666-555555555555" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 5.12 Special Item Collection Endpoints

These endpoints are useful for dashboard widgets or quick filter chips. All return `List<MenuItemResponseDTO>` and are not paginated.

**GET** `/api/v1/menus/{menuId}/items/popular`

Purpose: fetch popular items for spotlight widgets.

```bash
curl -X GET "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items/popular" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**GET** `/api/v1/menus/{menuId}/items/new`

Purpose: fetch newly created items.

```bash
curl -X GET "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items/new" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**GET** `/api/v1/menus/{menuId}/items/with-variants`

Purpose: fetch items that currently have variants.

```bash
curl -X GET "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items/with-variants" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**GET** `/api/v1/menus/{menuId}/items/with-addons`

Purpose: fetch items that currently have linked addon groups.

```bash
curl -X GET "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items/with-addons" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**GET** `/api/v1/menus/{menuId}/items/with-nutrition`

Purpose: fetch items with nutrition data populated.

```bash
curl -X GET "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/items/with-nutrition" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

## 6. Module: Menu Item Variants

Base path: `/api/v1/items/{itemId}/variants`

Recommended frontend surfaces:

- Item detail -> Variants table
- create/edit variant modal
- default variant toggle

### Variant request payload

```json
{
  "name": "Regular",
  "variantType": "SIZE",
  "description": "Standard portion",
  "price": 249.00,
  "discountPrice": 229.00,
  "isDefault": true,
  "displayOrder": 1
}
```

### 6.1 Create Variant

**POST** `/api/v1/items/{itemId}/variants`

Purpose: add a variant to an item.

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/items/99999999-8888-7777-6666-555555555555/variants" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Regular",
    "variantType": "SIZE",
    "description": "Standard portion",
    "price": 249.00,
    "discountPrice": 229.00,
    "isDefault": true,
    "displayOrder": 1
  }'
```

Frontend notes:

- if `isDefault=true`, the service clears any previous default variant for this item
- creating or deleting variants refreshes the parent item's `hasVariants`

### 6.2 Get Variants

**GET** `/api/v1/items/{itemId}/variants`

Purpose: load all variants for one item ordered by `displayOrder`.

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/items/99999999-8888-7777-6666-555555555555/variants" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 6.3 Update Variant

**PUT** `/api/v1/items/{itemId}/variants/{variantId}`

Purpose: full update for a variant.

Request body: same schema as create.

cURL:

```bash
curl -X PUT "$BASE_URL/api/v1/items/99999999-8888-7777-6666-555555555555/variants/12121212-3434-5656-7878-909090909090" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Large",
    "variantType": "SIZE",
    "description": "Large portion",
    "price": 299.00,
    "discountPrice": 279.00,
    "isDefault": true,
    "displayOrder": 2
  }'
```

### 6.4 Update Variant Availability

**PATCH** `/api/v1/items/{itemId}/variants/{variantId}/availability`

Purpose: toggle a variant on or off.

Request body:

```json
{
  "isAvailable": false
}
```

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/items/99999999-8888-7777-6666-555555555555/variants/12121212-3434-5656-7878-909090909090/availability" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "isAvailable": false
  }'
```

### 6.5 Set Default Variant

**PATCH** `/api/v1/items/{itemId}/variants/{variantId}/default`

Purpose: make one variant the default choice.

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/items/99999999-8888-7777-6666-555555555555/variants/12121212-3434-5656-7878-909090909090/default" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 6.6 Delete Variant

**DELETE** `/api/v1/items/{itemId}/variants/{variantId}`

Purpose: soft delete a variant.

cURL:

```bash
curl -X DELETE "$BASE_URL/api/v1/items/99999999-8888-7777-6666-555555555555/variants/12121212-3434-5656-7878-909090909090" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

## 7. Module: Addon Groups

Base path: `/api/v1/addon-groups`

Recommended frontend surfaces:

- reusable addon-groups settings page
- addon-group detail drawer with nested addon list

### Addon group request payload

```json
{
  "name": "Choose Your Dip",
  "description": "Optional dip selection",
  "selectionType": "SINGLE",
  "minSelections": 0,
  "maxSelections": 1,
  "displayOrder": 1
}
```

Important frontend note:

- request payload has no `isRequired` field even though response DTOs include `isRequired`

### 7.1 Create Addon Group

**POST** `/api/v1/addon-groups`

Purpose: create a reusable addon group in the restaurant's addon library.

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/addon-groups" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Choose Your Dip",
    "description": "Optional dip selection",
    "selectionType": "SINGLE",
    "minSelections": 0,
    "maxSelections": 1,
    "displayOrder": 1
  }'
```

### 7.2 Get Addon Groups

**GET** `/api/v1/addon-groups`

Purpose: load paginated addon groups for management screens.

Query params:

- `page` optional, default `0`
- `size` optional, default `20`, max `100`
- `sort` optional, default `displayOrder,asc`

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/addon-groups?page=0&size=20&sort=displayOrder,asc" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `Page<AddonGroupResponseDTO>`

### 7.3 Get Addon Group Detail

**GET** `/api/v1/addon-groups/{groupId}`

Purpose: fetch one addon group with its nested addons.

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/addon-groups/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `AddonGroupDetailResponseDTO`

### 7.4 Update Addon Group

**PUT** `/api/v1/addon-groups/{groupId}`

Purpose: full update for an addon group.

Request body: same schema as create.

cURL:

```bash
curl -X PUT "$BASE_URL/api/v1/addon-groups/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Choose Your Sauce",
    "description": "Optional sauce selection",
    "selectionType": "MULTIPLE",
    "minSelections": 0,
    "maxSelections": 2,
    "displayOrder": 2
  }'
```

### 7.5 Update Addon Group Status

**PATCH** `/api/v1/addon-groups/{groupId}/status`

Purpose: activate or deactivate an addon group.

Request body:

```json
{
  "isActive": true
}
```

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/addon-groups/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee/status" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "isActive": true
  }'
```

### 7.6 Delete Addon Group

**DELETE** `/api/v1/addon-groups/{groupId}`

Purpose: soft delete an addon group.

cURL:

```bash
curl -X DELETE "$BASE_URL/api/v1/addon-groups/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

## 8. Module: Addon Group Links on Menu Items

Base path: `/api/v1/items/{itemId}/addon-groups`

Recommended frontend surfaces:

- Item detail -> linked addon groups list
- order controls for linked groups
- attach/detach group modal

### Link request payload

```json
{
  "addonGroupId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
  "displayOrder": 1
}
```

### 8.1 Link Addon Group to Item

**POST** `/api/v1/items/{itemId}/addon-groups`

Purpose: attach a reusable addon group to a menu item.

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/items/99999999-8888-7777-6666-555555555555/addon-groups" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "addonGroupId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "displayOrder": 1
  }'
```

Frontend notes:

- duplicate links return `409`
- linking or unlinking refreshes the parent item's `hasAddons`

### 8.2 Get Linked Addon Groups for Item

**GET** `/api/v1/items/{itemId}/addon-groups`

Purpose: load all addon groups attached to an item.

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/items/99999999-8888-7777-6666-555555555555/addon-groups" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `List<AddonGroupDetailResponseDTO>`

Frontend note:

- the repository sorts by link order, but the response object only exposes the group's own `displayOrder`, not the mapping row's order. If the UI needs exact persisted link order, the API needs a DTO enhancement.

### 8.3 Update Linked Group Display Order

**PATCH** `/api/v1/items/{itemId}/addon-groups/{groupId}/order`

Purpose: update the item-specific order of a linked addon group.

Request body:

```json
{
  "displayOrder": 2
}
```

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/items/99999999-8888-7777-6666-555555555555/addon-groups/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee/order" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "displayOrder": 2
  }'
```

### 8.4 Unlink Addon Group from Item

**DELETE** `/api/v1/items/{itemId}/addon-groups/{groupId}`

Purpose: detach an addon group from an item.

cURL:

```bash
curl -X DELETE "$BASE_URL/api/v1/items/99999999-8888-7777-6666-555555555555/addon-groups/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

## 9. Module: Addons

Base path: `/api/v1/addon-groups/{groupId}/addons`

Recommended frontend surfaces:

- Addon group detail -> addon rows
- create/edit addon modal

### Addon request payload

```json
{
  "name": "Mint Dip",
  "description": "Fresh mint yogurt dip",
  "price": 25.00,
  "isDefault": false,
  "displayOrder": 1
}
```

### 9.1 Create Addon

**POST** `/api/v1/addon-groups/{groupId}/addons`

Purpose: add an addon to a reusable addon group.

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/addon-groups/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee/addons" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mint Dip",
    "description": "Fresh mint yogurt dip",
    "price": 25.00,
    "isDefault": false,
    "displayOrder": 1
  }'
```

### 9.2 Get Addons in Group

**GET** `/api/v1/addon-groups/{groupId}/addons`

Purpose: list all addons inside a group.

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/addon-groups/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee/addons" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `List<AddonResponseDTO>`

### 9.3 Update Addon

**PUT** `/api/v1/addon-groups/{groupId}/addons/{addonId}`

Purpose: full update for an addon.

Request body: same schema as create.

cURL:

```bash
curl -X PUT "$BASE_URL/api/v1/addon-groups/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee/addons/bbbbbbbb-cccc-dddd-eeee-ffffffffffff" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mint Dip",
    "description": "Fresh mint yogurt dip",
    "price": 30.00,
    "isDefault": true,
    "displayOrder": 1
  }'
```

### 9.4 Update Addon Availability

**PATCH** `/api/v1/addon-groups/{groupId}/addons/{addonId}/availability`

Purpose: toggle addon availability.

Request body:

```json
{
  "isAvailable": false
}
```

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/addon-groups/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee/addons/bbbbbbbb-cccc-dddd-eeee-ffffffffffff/availability" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "isAvailable": false
  }'
```

### 9.5 Delete Addon

**DELETE** `/api/v1/addon-groups/{groupId}/addons/{addonId}`

Purpose: soft delete an addon.

cURL:

```bash
curl -X DELETE "$BASE_URL/api/v1/addon-groups/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee/addons/bbbbbbbb-cccc-dddd-eeee-ffffffffffff" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

## 10. Module: Combos

Base path: `/api/v1/menus/{menuId}/combos`

Recommended frontend surfaces:

- Combos tab inside menu detail
- combo create/edit page or drawer
- combo detail slot editor

### Combo request payload

```json
{
  "name": "Lunch Combo A",
  "description": "Main, side, and drink",
  "aiDescription": "Balanced lunch combo",
  "imageUrl": "https://cdn.example.com/combos/lunch-a.jpg",
  "comboPrice": 399.00,
  "originalPrice": 449.00,
  "discountPrice": 399.00,
  "validFrom": "2026-04-01T12:00:00+05:30",
  "validTo": "2026-10-01T23:59:59+05:30",
  "availableDays": [
    "MONDAY",
    "TUESDAY",
    "WEDNESDAY"
  ],
  "maxQuantityPerOrder": 2,
  "badge": "VALUE_MEAL",
  "displayOrder": 1
}
```

### 10.1 Create Combo

**POST** `/api/v1/menus/{menuId}/combos`

Purpose: create a combo in a menu.

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/combos" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Lunch Combo A",
    "description": "Main, side, and drink",
    "aiDescription": "Balanced lunch combo",
    "imageUrl": "https://cdn.example.com/combos/lunch-a.jpg",
    "comboPrice": 399.00,
    "originalPrice": 449.00,
    "discountPrice": 399.00,
    "validFrom": "2026-04-01T12:00:00+05:30",
    "validTo": "2026-10-01T23:59:59+05:30",
    "availableDays": [
      "MONDAY",
      "TUESDAY",
      "WEDNESDAY"
    ],
    "maxQuantityPerOrder": 2,
    "badge": "VALUE_MEAL",
    "displayOrder": 1
  }'
```

### 10.2 Get Combos

**GET** `/api/v1/menus/{menuId}/combos`

Purpose: list combos for a menu with pagination.

Query params:

- `page` optional, default `0`
- `size` optional, default `20`, max `100`
- `sort` optional, default `displayOrder,asc`

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/combos?page=0&size=20&sort=displayOrder,asc" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `Page<ComboResponseDTO>`

### 10.3 Get Combo Detail

**GET** `/api/v1/menus/{menuId}/combos/{comboId}`

Purpose: load one combo with nested slot items.

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/combos/12121212-3434-5656-7878-909090909090" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `ComboDetailResponseDTO`

### 10.4 Update Combo

**PUT** `/api/v1/menus/{menuId}/combos/{comboId}`

Purpose: full update for a combo.

Request body: same schema as create.

cURL:

```bash
curl -X PUT "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/combos/12121212-3434-5656-7878-909090909090" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Lunch Combo A Plus",
    "description": "Main, side, drink, and dessert",
    "aiDescription": "Balanced lunch combo",
    "imageUrl": "https://cdn.example.com/combos/lunch-a-plus.jpg",
    "comboPrice": 449.00,
    "originalPrice": 499.00,
    "discountPrice": 449.00,
    "validFrom": "2026-04-01T12:00:00+05:30",
    "validTo": "2026-10-01T23:59:59+05:30",
    "availableDays": [
      "MONDAY",
      "TUESDAY",
      "WEDNESDAY"
    ],
    "maxQuantityPerOrder": 2,
    "badge": "VALUE_MEAL",
    "displayOrder": 2
  }'
```

### 10.5 Update Combo Availability

**PATCH** `/api/v1/menus/{menuId}/combos/{comboId}/availability`

Purpose: enable or disable a combo for sale.

Request body:

```json
{
  "isAvailable": false
}
```

cURL:

```bash
curl -X PATCH "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/combos/12121212-3434-5656-7878-909090909090/availability" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "isAvailable": false
  }'
```

### 10.6 Delete Combo

**DELETE** `/api/v1/menus/{menuId}/combos/{comboId}`

Purpose: soft delete a combo.

cURL:

```bash
curl -X DELETE "$BASE_URL/api/v1/menus/11111111-2222-3333-4444-555555555555/combos/12121212-3434-5656-7878-909090909090" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

## 11. Module: Combo Items

Base path: `/api/v1/combos/{comboId}/items`

Recommended frontend surfaces:

- Combo detail -> slots table
- add slot modal
- substitution configuration

### Combo item request payload

```json
{
  "menuItemId": "99999999-8888-7777-6666-555555555555",
  "variantId": "12121212-3434-5656-7878-909090909090",
  "quantity": 1,
  "isSubstitutable": true,
  "substitutionCategoryId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
  "displayOrder": 1,
  "note": "Customer can swap main item"
}
```

### 11.1 Add Combo Slot

**POST** `/api/v1/combos/{comboId}/items`

Purpose: add a slot row to a combo.

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/combos/12121212-3434-5656-7878-909090909090/items" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "menuItemId": "99999999-8888-7777-6666-555555555555",
    "variantId": "12121212-3434-5656-7878-909090909090",
    "quantity": 1,
    "isSubstitutable": true,
    "substitutionCategoryId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "displayOrder": 1,
    "note": "Customer can swap main item"
  }'
```

Frontend notes:

- if `isSubstitutable=true`, `substitutionCategoryId` becomes mandatory and missing it returns `400`
- if `quantity` is omitted, the service defaults it to `1`
- if `variantId` is provided, it must belong to the same `menuItemId`

### 11.2 Get Combo Slots

**GET** `/api/v1/combos/{comboId}/items`

Purpose: fetch all slots for a combo with item and variant labels.

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/combos/12121212-3434-5656-7878-909090909090/items" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `List<ComboItemResponseDTO>`

### 11.3 Update Combo Slot

**PUT** `/api/v1/combos/{comboId}/items/{slotId}`

Purpose: full update for a combo slot.

Request body: same schema as create.

cURL:

```bash
curl -X PUT "$BASE_URL/api/v1/combos/12121212-3434-5656-7878-909090909090/items/43434343-5656-7878-9090-121212121212" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "menuItemId": "99999999-8888-7777-6666-555555555555",
    "variantId": null,
    "quantity": 2,
    "isSubstitutable": false,
    "substitutionCategoryId": null,
    "displayOrder": 2,
    "note": "Two pieces"
  }'
```

### 11.4 Delete Combo Slot

**DELETE** `/api/v1/combos/{comboId}/items/{slotId}`

Purpose: soft delete a slot from a combo.

cURL:

```bash
curl -X DELETE "$BASE_URL/api/v1/combos/12121212-3434-5656-7878-909090909090/items/43434343-5656-7878-9090-121212121212" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

## 12. Module: Tags

Base path: `/api/v1/tags`

Recommended frontend surfaces:

- Tag settings page
- create/edit tag modal
- dietary or marketing label management

### Tag request payload

```json
{
  "name": "Vegetarian",
  "slug": "vegetarian",
  "tagType": "DIETARY",
  "description": "Suitable for vegetarians",
  "colorHex": "#2E7D32",
  "iconUrl": "https://cdn.example.com/tags/vegetarian.svg"
}
```

### 12.1 Create Tag

**POST** `/api/v1/tags`

Purpose: create a restaurant-visible tag.

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/tags" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Vegetarian",
    "slug": "vegetarian",
    "tagType": "DIETARY",
    "description": "Suitable for vegetarians",
    "colorHex": "#2E7D32",
    "iconUrl": "https://cdn.example.com/tags/vegetarian.svg"
  }'
```

Frontend notes:

- slug uniqueness is enforced per restaurant and duplicate slugs return `409`

### 12.2 Get Tags

**GET** `/api/v1/tags`

Purpose: list visible tags for the current restaurant.

Query params:

- `type` optional, filters by `tagType`

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/tags?type=DIETARY" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `List<TagResponseDTO>`

Frontend note:

- the query parameter name is `type`, not `tagType`

### 12.3 Update Tag

**PUT** `/api/v1/tags/{tagId}`

Purpose: full update for a tag.

Request body: same schema as create.

cURL:

```bash
curl -X PUT "$BASE_URL/api/v1/tags/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Vegetarian",
    "slug": "veg",
    "tagType": "DIETARY",
    "description": "Suitable for vegetarians",
    "colorHex": "#2E7D32",
    "iconUrl": "https://cdn.example.com/tags/veg.svg"
  }'
```

### 12.4 Delete Tag

**DELETE** `/api/v1/tags/{tagId}`

Purpose: soft delete a tag from the restaurant's tag library.

cURL:

```bash
curl -X DELETE "$BASE_URL/api/v1/tags/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

## 13. Module: Item Tag Links

Base path: `/api/v1/items/{itemId}/tags`

Recommended frontend surfaces:

- Item detail -> tags multi-select
- quick apply/remove chips

### Tag link request payload

```json
{
  "tagId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
}
```

### 13.1 Apply Tag to Item

**POST** `/api/v1/items/{itemId}/tags`

Purpose: attach a tag to a menu item.

cURL:

```bash
curl -X POST "$BASE_URL/api/v1/items/99999999-8888-7777-6666-555555555555/tags" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "tagId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
  }'
```

Frontend notes:

- duplicate tag links return `409`
- only tags visible to this restaurant can be linked

### 13.2 Get Tags for Item

**GET** `/api/v1/items/{itemId}/tags`

Purpose: load all tags currently linked to a menu item.

cURL:

```bash
curl -X GET "$BASE_URL/api/v1/items/99999999-8888-7777-6666-555555555555/tags" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Returns: `List<TagResponseDTO>`

### 13.3 Remove Tag from Item

**DELETE** `/api/v1/items/{itemId}/tags/{tagId}`

Purpose: remove a tag link from a menu item.

cURL:

```bash
curl -X DELETE "$BASE_URL/api/v1/items/99999999-8888-7777-6666-555555555555/tags/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

## 14. Suggested Frontend Page Breakdown

This is the cleanest page and section split based on the current API surface:

| Area | Recommended UI |
|---|---|
| Auth | Login page, session refresh handler, profile bootstrap |
| Staff | 1 list page, 1 create/edit form, 1 status modal |
| Menus | 1 menus list page, 1 create/edit drawer |
| Menu Detail | 1 detail shell with tabs for Categories, Items, Combos |
| Item Detail | 1 detail shell with tabs for Core Info, Variants, Addon Groups, Tags |
| Addon Groups | 1 reusable library page, 1 detail drawer |
| Tags | 1 reusable tag settings page |

Suggested main screens:

1. Login
2. Dashboard shell
3. Staff management
4. Menus list
5. Menu detail
6. Item detail
7. Addon groups library
8. Tags settings

## 15. Final Review Summary

The main corrections made in this review are:

- aligned every route with the actual controller mappings
- documented true request DTO shapes instead of guessed payloads
- corrected auth and staff `permissions` to JSON objects
- called out request quirks that affect the frontend immediately
- grouped APIs by frontend module so page planning is clearer
- preserved full cURL coverage for all endpoints
