# Restaurant Admin Dashboard — API Development Strategy

**Project:** AlaCarte  
**Module:** Restaurant Admin Dashboard  
**Stack:** Spring Boot 3.2 · Java 21 · Hibernate 6.3 · PostgreSQL 16 · JWT Auth  
**Scope:** Complete CRUD API surface for Menu, Categories, Items, Variants, Addons, Tags, Combos  

---

## Table of Contents

1. [Core Principles](#1-core-principles)
2. [Codebase Architecture Convention](#2-codebase-architecture-convention)
3. [RestaurantAdminMiddleware — How JWT Plugs In](#3-restaurantadminmiddleware--how-jwt-plugs-in)
4. [Build Phases — Execution Order](#4-build-phases--execution-order)
5. [Package Structure](#5-package-structure)
6. [Controllers — Full List](#6-controllers--full-list)
7. [DTOs — Request and Response Classes](#7-dtos--request-and-response-classes)
8. [Repository Interfaces — Full List](#8-repository-interfaces--full-list)
9. [Service Classes and Methods](#9-service-classes-and-methods)
10. [Soft Delete Convention](#10-soft-delete-convention)
11. [Error Handling Strategy](#11-error-handling-strategy)
12. [Validation Strategy](#12-validation-strategy)
13. [Response Envelope](#13-response-envelope)
14. [API Versioning and URL Structure](#14-api-versioning-and-url-structure)
15. [Phase-by-Phase Checklist](#15-phase-by-phase-checklist)

---

## 1. Core Principles

These principles apply to every single API in this module without exception.

---

### Principle 1 — `restaurant_id` Always Comes From JWT, Never From the Request Body

The `RestaurantAdminMiddleware` extracts `restaurantId` from the JWT token and makes it available to the request context. Controllers read it from there and pass it down to the service. A manager must never be able to supply or override their own `restaurantId` through the request body or query params.

```
JWT Token → RestaurantAdminMiddleware → RequestContext.restaurantId
                                              ↓
                                         Controller reads it
                                              ↓
                                         Passes to Service
                                         (never from @RequestBody)
```

**Why:** A manager of Restaurant A could otherwise manipulate data belonging to Restaurant B simply by sending a different UUID in the body.

---

### Principle 2 — Flat Writes, Rich Reads

**Write endpoints (POST / PUT / PATCH)** accept flat, simple request DTOs — only the fields being created or updated. No nested objects.

**Read endpoints (GET)** return rich, nested response DTOs — a menu item response includes its category name, variant list, tag list, addon group list, all in one payload so the dashboard doesn't need multiple round trips.

---

### Principle 3 — Two DTOs Per Resource, Always

Every resource has exactly two DTO types:

| DTO Type | Used In | Contains |
|---|---|---|
| `XxxRequestDTO` | POST / PUT body | Flat fields, `@Valid` annotations |
| `XxxResponseDTO` | All GET responses | Rich, nested, computed fields included |

Never return a JPA entity from a controller. Never accept a JPA entity as a request body. Always map through DTOs.

---

### Principle 4 — Soft Delete Only, Never Hard Delete

Every `DELETE` endpoint sets `deleted = 1` on the target row. No endpoint in this module runs a physical `DELETE FROM` SQL statement. This preserves audit history and protects referential integrity with future order data.

---

### Principle 5 — Availability Toggle Is a Separate Endpoint From Delete

`is_available` (item temporarily out of stock) and `deleted = 1` (item permanently removed) are semantically different operations. They must be separate `PATCH` endpoints. A `DELETE` call should never flip `is_available`.

---

### Principle 6 — Pagination on Every List Endpoint

Every `GET` endpoint that returns a list accepts `page`, `size`, and `sort` query parameters. No unbounded list responses. Default page size is 20. Maximum page size is 100, enforced in the service layer.

---

### Principle 7 — Service Layer Owns All Business Logic

Controllers are thin. They only:
1. Extract `restaurantId` from the request context
2. Validate the incoming DTO (via `@Valid`)
3. Call one service method
4. Return the response

All validation logic, ownership checks, business rules, and data transformation live in the service. If a controller method is more than ~15 lines, something is wrong.

---

### Principle 8 — Ownership Check Before Every Write

Before any update or delete, the service must verify that the target entity belongs to the authenticated restaurant. This is the **ownership check** pattern:

```java
// Example in service
MenuItem item = menuItemRepository.findByIdAndDeletedFalse(itemId)
    .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));

if (!item.getRestaurant().getId().equals(restaurantId)) {
    throw new AccessDeniedException("MenuItem does not belong to this restaurant");
}
```

This check applies to every write operation across all services.

---

### Principle 9 — `has_variants` and `has_addons` Are Managed by the Service, Not the DB

When a variant is created or soft-deleted, the `MenuItemService` updates `has_variants` on the parent `MenuItem`. Same for `has_addons` when addon group links are created or removed. The DB never computes these — the service keeps them in sync within the same transaction.

---

### Principle 10 — API Versioning From Day One

All endpoints are prefixed with `/api/v1/`. This costs nothing now and saves a painful migration later.

---

## 2. Codebase Architecture Convention

Your existing codebase follows this layered pattern. Every new feature in this module must follow the same convention without deviation.

```
┌─────────────────────────────────────────────────────┐
│                    Controller                        │
│  • Reads restaurantId from RequestContext            │
│  • Calls @Valid on request DTO                       │
│  • Calls exactly one service method                  │
│  • Returns ApiResponse<XxxResponseDTO>               │
└─────────────────────────┬───────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────┐
│                 Service (Class)                       │
│  • Ownership check on every write                    │
│  • All business logic lives here                     │
│  • Calls repository methods                          │
│  • Maps Entity → ResponseDTO                         │
│  • Manages has_variants / has_addons flags           │
│  • Handles soft delete (deleted = 1)                 │
└─────────────────────────┬───────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────┐
│             Repository (Interface)                    │
│  • Extends JpaRepository<Entity, UUID>               │
│  • All queries filter WHERE deleted = 0              │
│  • Custom @Query methods for complex fetches         │
│  • @EntityGraph for join fetch when needed           │
└─────────────────────────┬───────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────┐
│                Entity (JPA)                           │
│  • @SQLRestriction("deleted = 0") on class           │
│  • @ManyToOne(fetch = LAZY) for all FK fields        │
│  • No @OneToMany collections                         │
│  • deleted field is Short (0 or 1)                   │
└─────────────────────────────────────────────────────┘
```

---

## 3. RestaurantAdminMiddleware — How JWT Plugs In

Your `RestaurantAdminMiddleware` already handles JWT verification. Here is exactly how it feeds into the rest of the chain.

### What the Middleware Does

```
Incoming Request
      │
      ▼
RestaurantAdminMiddleware
  1. Reads Authorization header
  2. Validates JWT signature and expiry
  3. Extracts claims: staffId, restaurantId, role
  4. Verifies role is MANAGER or ADMIN
  5. Sets attributes on HttpServletRequest:
       request.setAttribute("restaurantId", restaurantId)
       request.setAttribute("staffId", staffId)
       request.setAttribute("staffRole", role)
  6. If invalid → 401 Unauthorized immediately
      │
      ▼
Controller receives request with attributes already set
```

### How Controllers Read the Context

Create a single utility method in a `RequestContextUtil` class that every controller uses:

```java
@Component
public class RequestContextUtil {

    public UUID getRestaurantId(HttpServletRequest request) {
        Object restaurantId = request.getAttribute("restaurantId");
        if (restaurantId == null) {
            throw new AccessDeniedException("Restaurant context not found in request");
        }
        return UUID.fromString(restaurantId.toString());
    }

    public UUID getStaffId(HttpServletRequest request) {
        return UUID.fromString(request.getAttribute("staffId").toString());
    }

    public String getStaffRole(HttpServletRequest request) {
        return request.getAttribute("staffRole").toString();
    }
}
```

### How a Controller Uses It

```java
@PostMapping
public ResponseEntity<ApiResponse<MenuResponseDTO>> createMenu(
        @Valid @RequestBody MenuRequestDTO request,
        HttpServletRequest httpRequest) {

    UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
    UUID staffId      = requestContextUtil.getStaffId(httpRequest);

    MenuResponseDTO response = menuService.createMenu(restaurantId, staffId, request);
    return ResponseEntity.status(HttpStatus.CREATED)
                         .body(ApiResponse.success(response));
}
```

`restaurantId` is never in the URL path or request body for write operations. It is always extracted from the JWT via the middleware.

---

## 4. Build Phases — Execution Order

Build in this exact order. Each phase is independently testable before moving to the next. Do not start Phase 2 until all Phase 1 endpoints are working end-to-end including error handling.

```
Phase 1 — Foundation
├── Menu CRUD
├── MenuCategory CRUD
└── MenuItem CRUD (core fields only, no variants/addons yet)

Phase 2 — Item Enrichment
├── MenuItemVariant CRUD
├── MenuItemAddonGroup CRUD
├── MenuItemAddon CRUD
└── Addon-Group ↔ Item linking (MenuItemAddonGroupMap)

Phase 3 — Taxonomy
├── MenuItemTag CRUD
└── Tag ↔ Item linking (MenuItemTagMap)

Phase 4 — Bundles
├── Combo CRUD
└── ComboItem CRUD

Phase 5 — Dashboard Read Layer
├── Full MenuItem detail GET (variants + addons + tags in one response)
├── List endpoints with filters (category, availability, tag, price range)
└── Dashboard summary endpoints (counts, active items, etc.)
```

---

## 5. Package Structure

```
com.alaCarte
└── menu
    ├── controller
    │   ├── MenuController.java
    │   ├── MenuCategoryController.java
    │   ├── MenuItemController.java
    │   ├── MenuItemVariantController.java
    │   ├── AddonGroupController.java
    │   ├── AddonController.java
    │   ├── AddonGroupItemController.java
    │   ├── TagController.java
    │   ├── TagItemController.java
    │   ├── ComboController.java
    │   └── ComboItemController.java
    │
    ├── service
    │   ├── MenuService.java
    │   ├── MenuCategoryService.java
    │   ├── MenuItemService.java
    │   ├── MenuItemVariantService.java
    │   ├── AddonGroupService.java
    │   ├── AddonService.java
    │   ├── AddonGroupItemService.java
    │   ├── TagService.java
    │   ├── TagItemService.java
    │   ├── ComboService.java
    │   └── ComboItemService.java
    │
    ├── repository
    │   ├── MenuRepository.java
    │   ├── MenuCategoryRepository.java
    │   ├── MenuItemRepository.java
    │   ├── MenuItemVariantRepository.java
    │   ├── MenuItemAddonGroupRepository.java
    │   ├── MenuItemAddonRepository.java
    │   ├── MenuItemAddonGroupMapRepository.java
    │   ├── MenuItemTagRepository.java
    │   ├── MenuItemTagMapRepository.java
    │   ├── ComboRepository.java
    │   └── ComboItemRepository.java
    │
    ├── dto
    │   ├── request
    │   │   ├── MenuRequestDTO.java
    │   │   ├── MenuCategoryRequestDTO.java
    │   │   ├── MenuItemRequestDTO.java
    │   │   ├── MenuItemVariantRequestDTO.java
    │   │   ├── AddonGroupRequestDTO.java
    │   │   ├── AddonRequestDTO.java
    │   │   ├── AddonGroupItemRequestDTO.java
    │   │   ├── TagRequestDTO.java
    │   │   ├── TagItemRequestDTO.java
    │   │   ├── ComboRequestDTO.java
    │   │   └── ComboItemRequestDTO.java
    │   │
    │   └── response
    │       ├── MenuResponseDTO.java
    │       ├── MenuCategoryResponseDTO.java
    │       ├── MenuItemResponseDTO.java
    │       ├── MenuItemDetailResponseDTO.java        ← rich version with nested data
    │       ├── MenuItemVariantResponseDTO.java
    │       ├── AddonGroupResponseDTO.java
    │       ├── AddonGroupDetailResponseDTO.java      ← includes addon list
    │       ├── AddonResponseDTO.java
    │       ├── TagResponseDTO.java
    │       ├── ComboResponseDTO.java
    │       ├── ComboDetailResponseDTO.java           ← includes slot list
    │       └── ComboItemResponseDTO.java
    │
    ├── entity
    │   ├── Menu.java
    │   ├── MenuCategory.java
    │   ├── MenuItem.java
    │   ├── MenuItemVariant.java
    │   ├── MenuItemAddonGroup.java
    │   ├── MenuItemAddon.java
    │   ├── MenuItemAddonGroupMap.java
    │   ├── MenuItemTag.java
    │   ├── MenuItemTagMap.java
    │   ├── Combo.java
    │   └── ComboItem.java
    │
    └── util
        ├── RequestContextUtil.java
        └── MenuItemMapper.java                       ← entity → DTO mapping helper
```

---

## 6. Controllers — Full List

There are **11 controllers** total. Each controller handles exactly one resource. No controller handles more than one entity type.

---

### Controller 1 — `MenuController`

**Base URL:** `/api/v1/menus`

| Method | URL | Description | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/api/v1/menus` | Create a new menu | `MenuRequestDTO` | `MenuResponseDTO` |
| `GET` | `/api/v1/menus` | List all menus for restaurant | — | `Page<MenuResponseDTO>` |
| `GET` | `/api/v1/menus/{menuId}` | Get single menu detail | — | `MenuResponseDTO` |
| `PUT` | `/api/v1/menus/{menuId}` | Full update of menu | `MenuRequestDTO` | `MenuResponseDTO` |
| `PATCH` | `/api/v1/menus/{menuId}/status` | Toggle `is_active` | `{ "isActive": true }` | `MenuResponseDTO` |
| `DELETE` | `/api/v1/menus/{menuId}` | Soft delete menu | — | `{ "message": "deleted" }` |

---

### Controller 2 — `MenuCategoryController`

**Base URL:** `/api/v1/menus/{menuId}/categories`

| Method | URL | Description | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/api/v1/menus/{menuId}/categories` | Create category | `MenuCategoryRequestDTO` | `MenuCategoryResponseDTO` |
| `GET` | `/api/v1/menus/{menuId}/categories` | List categories for menu | — | `List<MenuCategoryResponseDTO>` |
| `GET` | `/api/v1/menus/{menuId}/categories/{categoryId}` | Get single category | — | `MenuCategoryResponseDTO` |
| `PUT` | `/api/v1/menus/{menuId}/categories/{categoryId}` | Full update | `MenuCategoryRequestDTO` | `MenuCategoryResponseDTO` |
| `PATCH` | `/api/v1/menus/{menuId}/categories/{categoryId}/order` | Update display order | `{ "displayOrder": 3 }` | `MenuCategoryResponseDTO` |
| `DELETE` | `/api/v1/menus/{menuId}/categories/{categoryId}` | Soft delete | — | `{ "message": "deleted" }` |

---

### Controller 3 — `MenuItemController`

**Base URL:** `/api/v1/menus/{menuId}/items`

| Method | URL | Description | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/api/v1/menus/{menuId}/items` | Create item | `MenuItemRequestDTO` | `MenuItemResponseDTO` |
| `GET` | `/api/v1/menus/{menuId}/items` | List items (paginated + filtered) | — (query params) | `Page<MenuItemResponseDTO>` |
| `GET` | `/api/v1/menus/{menuId}/items/{itemId}` | Full item detail | — | `MenuItemDetailResponseDTO` |
| `PUT` | `/api/v1/menus/{menuId}/items/{itemId}` | Full update | `MenuItemRequestDTO` | `MenuItemResponseDTO` |
| `PATCH` | `/api/v1/menus/{menuId}/items/{itemId}/availability` | Toggle `is_available` | `{ "isAvailable": false }` | `MenuItemResponseDTO` |
| `PATCH` | `/api/v1/menus/{menuId}/items/{itemId}/category` | Reassign category | `{ "categoryId": "uuid" }` | `MenuItemResponseDTO` |
| `PATCH` | `/api/v1/menus/{menuId}/items/{itemId}/order` | Update display order | `{ "displayOrder": 2 }` | `MenuItemResponseDTO` |
| `DELETE` | `/api/v1/menus/{menuId}/items/{itemId}` | Soft delete | — | `{ "message": "deleted" }` |

**Query params for GET list:**  
`?page=0&size=20&sort=displayOrder&categoryId=uuid&isAvailable=true&isVeg=true&search=paneer`

---

### Controller 4 — `MenuItemVariantController`

**Base URL:** `/api/v1/items/{itemId}/variants`

| Method | URL | Description | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/api/v1/items/{itemId}/variants` | Add variant to item | `MenuItemVariantRequestDTO` | `MenuItemVariantResponseDTO` |
| `GET` | `/api/v1/items/{itemId}/variants` | List all variants for item | — | `List<MenuItemVariantResponseDTO>` |
| `PUT` | `/api/v1/items/{itemId}/variants/{variantId}` | Update variant | `MenuItemVariantRequestDTO` | `MenuItemVariantResponseDTO` |
| `PATCH` | `/api/v1/items/{itemId}/variants/{variantId}/availability` | Toggle availability | `{ "isAvailable": true }` | `MenuItemVariantResponseDTO` |
| `PATCH` | `/api/v1/items/{itemId}/variants/{variantId}/default` | Set as default variant | — | `MenuItemVariantResponseDTO` |
| `DELETE` | `/api/v1/items/{itemId}/variants/{variantId}` | Soft delete variant | — | `{ "message": "deleted" }` |

---

### Controller 5 — `AddonGroupController`

**Base URL:** `/api/v1/addon-groups`  
Note: Addon groups are restaurant-scoped, not menu or item scoped. They are defined independently and reused across items.

| Method | URL | Description | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/api/v1/addon-groups` | Create addon group | `AddonGroupRequestDTO` | `AddonGroupResponseDTO` |
| `GET` | `/api/v1/addon-groups` | List all groups for restaurant | — | `Page<AddonGroupResponseDTO>` |
| `GET` | `/api/v1/addon-groups/{groupId}` | Get group with all addons | — | `AddonGroupDetailResponseDTO` |
| `PUT` | `/api/v1/addon-groups/{groupId}` | Update group metadata | `AddonGroupRequestDTO` | `AddonGroupResponseDTO` |
| `PATCH` | `/api/v1/addon-groups/{groupId}/status` | Toggle `is_active` | `{ "isActive": false }` | `AddonGroupResponseDTO` |
| `DELETE` | `/api/v1/addon-groups/{groupId}` | Soft delete group | — | `{ "message": "deleted" }` |

---

### Controller 6 — `AddonController`

**Base URL:** `/api/v1/addon-groups/{groupId}/addons`

| Method | URL | Description | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/api/v1/addon-groups/{groupId}/addons` | Add option to group | `AddonRequestDTO` | `AddonResponseDTO` |
| `GET` | `/api/v1/addon-groups/{groupId}/addons` | List all options in group | — | `List<AddonResponseDTO>` |
| `PUT` | `/api/v1/addon-groups/{groupId}/addons/{addonId}` | Update option | `AddonRequestDTO` | `AddonResponseDTO` |
| `PATCH` | `/api/v1/addon-groups/{groupId}/addons/{addonId}/availability` | Toggle availability | `{ "isAvailable": false }` | `AddonResponseDTO` |
| `DELETE` | `/api/v1/addon-groups/{groupId}/addons/{addonId}` | Soft delete option | — | `{ "message": "deleted" }` |

---

### Controller 7 — `AddonGroupItemController`

**Base URL:** `/api/v1/items/{itemId}/addon-groups`  
Purpose: Manages the link between a menu item and addon groups.

| Method | URL | Description | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/api/v1/items/{itemId}/addon-groups` | Link addon group to item | `AddonGroupItemRequestDTO` | `{ "message": "linked" }` |
| `GET` | `/api/v1/items/{itemId}/addon-groups` | List groups linked to item | — | `List<AddonGroupDetailResponseDTO>` |
| `PATCH` | `/api/v1/items/{itemId}/addon-groups/{groupId}/order` | Update display order | `{ "displayOrder": 2 }` | `{ "message": "updated" }` |
| `DELETE` | `/api/v1/items/{itemId}/addon-groups/{groupId}` | Unlink group from item | — | `{ "message": "unlinked" }` |

---

### Controller 8 — `TagController`

**Base URL:** `/api/v1/tags`

| Method | URL | Description | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/api/v1/tags` | Create custom tag | `TagRequestDTO` | `TagResponseDTO` |
| `GET` | `/api/v1/tags` | List all tags (restaurant + platform) | — | `List<TagResponseDTO>` |
| `GET` | `/api/v1/tags?type=DIETARY` | Filter tags by type | — (query param) | `List<TagResponseDTO>` |
| `PUT` | `/api/v1/tags/{tagId}` | Update tag | `TagRequestDTO` | `TagResponseDTO` |
| `DELETE` | `/api/v1/tags/{tagId}` | Soft delete tag | — | `{ "message": "deleted" }` |

---

### Controller 9 — `TagItemController`

**Base URL:** `/api/v1/items/{itemId}/tags`  
Purpose: Manages which tags are applied to a menu item.

| Method | URL | Description | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/api/v1/items/{itemId}/tags` | Apply tag to item | `TagItemRequestDTO` | `{ "message": "applied" }` |
| `GET` | `/api/v1/items/{itemId}/tags` | List tags on item | — | `List<TagResponseDTO>` |
| `DELETE` | `/api/v1/items/{itemId}/tags/{tagId}` | Remove tag from item | — | `{ "message": "removed" }` |

---

### Controller 10 — `ComboController`

**Base URL:** `/api/v1/menus/{menuId}/combos`

| Method | URL | Description | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/api/v1/menus/{menuId}/combos` | Create combo | `ComboRequestDTO` | `ComboResponseDTO` |
| `GET` | `/api/v1/menus/{menuId}/combos` | List all combos for menu | — | `Page<ComboResponseDTO>` |
| `GET` | `/api/v1/menus/{menuId}/combos/{comboId}` | Get combo with all slots | — | `ComboDetailResponseDTO` |
| `PUT` | `/api/v1/menus/{menuId}/combos/{comboId}` | Full update | `ComboRequestDTO` | `ComboResponseDTO` |
| `PATCH` | `/api/v1/menus/{menuId}/combos/{comboId}/availability` | Toggle `is_available` | `{ "isAvailable": true }` | `ComboResponseDTO` |
| `DELETE` | `/api/v1/menus/{menuId}/combos/{comboId}` | Soft delete | — | `{ "message": "deleted" }` |

---

### Controller 11 — `ComboItemController`

**Base URL:** `/api/v1/combos/{comboId}/items`

| Method | URL | Description | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/api/v1/combos/{comboId}/items` | Add item slot to combo | `ComboItemRequestDTO` | `ComboItemResponseDTO` |
| `GET` | `/api/v1/combos/{comboId}/items` | List all slots in combo | — | `List<ComboItemResponseDTO>` |
| `PUT` | `/api/v1/combos/{comboId}/items/{slotId}` | Update slot | `ComboItemRequestDTO` | `ComboItemResponseDTO` |
| `DELETE` | `/api/v1/combos/{comboId}/items/{slotId}` | Remove slot from combo | — | `{ "message": "deleted" }` |

---

## 7. DTOs — Request and Response Classes

### Request DTOs

---

#### `MenuRequestDTO`
```java
public class MenuRequestDTO {
    @NotBlank String name;
    String description;
    String menuType;           // BREAKFAST / LUNCH / DINNER / BAR / CUSTOM
    Boolean isActive;
    OffsetDateTime validFrom;
    OffsetDateTime validTo;
    Integer displayOrder;
    String coverImage;
}
```

---

#### `MenuCategoryRequestDTO`
```java
public class MenuCategoryRequestDTO {
    @NotBlank String name;
    String description;
    String imageUrl;
    Integer displayOrder;
    UUID parentCategoryId;     // null = top-level category
}
```

---

#### `MenuItemRequestDTO`
```java
public class MenuItemRequestDTO {
    @NotBlank  String name;
    String description;
    String aiDescription;
    UUID categoryId;           // null = uncategorized
    @NotNull @Positive BigDecimal basePrice;
    BigDecimal discountPrice;
    Boolean isVeg;
    Boolean isVegan;
    Boolean isSpicy;
    Boolean containsEgg;
    Boolean containsGluten;
    Boolean containsNuts;
    Boolean containsDairy;
    Integer spiceLevel;        // 1-5
    Integer prepTimeMinutes;
    String imageUrl;
    List<String> gallery;
    List<String> ingredients;
    String cuisineType;
    String courseType;         // STARTER/MAIN/DESSERT/BEVERAGE/SNACK/BREAD/SOUP
    Integer displayOrder;
    String badge;
    Map<String, Object> nutrition;
}
```

---

#### `MenuItemVariantRequestDTO`
```java
public class MenuItemVariantRequestDTO {
    @NotBlank String name;
    @NotBlank String variantType;   // SIZE / PORTION / FLAVOR / CRUST / TEMPERATURE / CUSTOM
    String description;
    @NotNull @Positive BigDecimal price;
    BigDecimal discountPrice;
    Boolean isDefault;
    Integer displayOrder;
}
```

---

#### `AddonGroupRequestDTO`
```java
public class AddonGroupRequestDTO {
    @NotBlank String name;
    String description;
    @NotBlank String selectionType;   // SINGLE / MULTIPLE
    @Min(0) Integer minSelections;
    Integer maxSelections;            // null = unlimited
    Integer displayOrder;
}
```

---

#### `AddonRequestDTO`
```java
public class AddonRequestDTO {
    @NotBlank String name;
    String description;
    @NotNull @PositiveOrZero BigDecimal price;
    Boolean isDefault;
    Integer displayOrder;
}
```

---

#### `AddonGroupItemRequestDTO`
```java
public class AddonGroupItemRequestDTO {
    @NotNull UUID addonGroupId;
    Integer displayOrder;
}
```

---

#### `TagRequestDTO`
```java
public class TagRequestDTO {
    @NotBlank String name;
    @NotBlank String slug;
    @NotBlank String tagType;    // DIETARY / ALLERGEN / CUISINE / BADGE / OCCASION / CUSTOM
    String description;
    String colorHex;
    String iconUrl;
}
```

---

#### `TagItemRequestDTO`
```java
public class TagItemRequestDTO {
    @NotNull UUID tagId;
}
```

---

#### `ComboRequestDTO`
```java
public class ComboRequestDTO {
    @NotBlank String name;
    String description;
    String aiDescription;
    String imageUrl;
    @NotNull @Positive BigDecimal comboPrice;
    BigDecimal originalPrice;
    BigDecimal discountPrice;
    OffsetDateTime validFrom;
    OffsetDateTime validTo;
    List<String> availableDays;   // MON / TUE / WED / THU / FRI / SAT / SUN
    Integer maxQuantityPerOrder;
    String badge;
    Integer displayOrder;
}
```

---

#### `ComboItemRequestDTO`
```java
public class ComboItemRequestDTO {
    @NotNull UUID menuItemId;
    UUID variantId;                  // null = base item, no specific variant
    @Min(1) Integer quantity;
    Boolean isSubstitutable;
    UUID substitutionCategoryId;     // required if isSubstitutable = true
    Integer displayOrder;
    String note;
}
```

---

### Response DTOs

---

#### `MenuResponseDTO`
```java
public class MenuResponseDTO {
    UUID id;
    String name;
    String description;
    String menuType;
    Boolean isActive;
    OffsetDateTime validFrom;
    OffsetDateTime validTo;
    Integer displayOrder;
    String coverImage;
    Integer totalCategories;    // count — computed
    Integer totalItems;         // count — computed
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
```

---

#### `MenuCategoryResponseDTO`
```java
public class MenuCategoryResponseDTO {
    UUID id;
    UUID menuId;
    String name;
    String description;
    String imageUrl;
    Integer displayOrder;
    Boolean isActive;
    UUID parentCategoryId;
    String parentCategoryName;  // denormalized for dashboard display
    Integer itemCount;          // count of active items in this category
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
```

---

#### `MenuItemResponseDTO`  ← Lean version for list views
```java
public class MenuItemResponseDTO {
    UUID id;
    UUID menuId;
    UUID categoryId;
    String categoryName;        // denormalized
    String name;
    String description;
    BigDecimal basePrice;
    BigDecimal discountPrice;
    Boolean isAvailable;
    Boolean isVeg;
    Boolean isVegan;
    Boolean isSpicy;
    Integer spiceLevel;
    String imageUrl;
    String badge;
    Boolean hasVariants;
    Boolean hasAddons;
    Integer displayOrder;
    String courseType;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
```

---

#### `MenuItemDetailResponseDTO` ← Rich version for item detail / edit page
```java
public class MenuItemDetailResponseDTO {
    // All fields from MenuItemResponseDTO, plus:
    UUID id;
    String aiDescription;
    Boolean containsEgg;
    Boolean containsGluten;
    Boolean containsNuts;
    Boolean containsDairy;
    Integer prepTimeMinutes;
    List<String> gallery;
    List<String> ingredients;
    String cuisineType;
    Map<String, Object> nutrition;
    String meta;

    // Nested — loaded in one query via JOIN FETCH
    List<MenuItemVariantResponseDTO> variants;
    List<AddonGroupDetailResponseDTO> addonGroups;
    List<TagResponseDTO> tags;
}
```

---

#### `MenuItemVariantResponseDTO`
```java
public class MenuItemVariantResponseDTO {
    UUID id;
    UUID menuItemId;
    String variantType;
    String name;
    String description;
    BigDecimal price;
    BigDecimal discountPrice;
    Boolean isAvailable;
    Boolean isDefault;
    Integer displayOrder;
}
```

---

#### `AddonGroupResponseDTO` ← Lean version
```java
public class AddonGroupResponseDTO {
    UUID id;
    String name;
    String description;
    String selectionType;
    Integer minSelections;
    Integer maxSelections;
    Boolean isRequired;
    Boolean isActive;
    Integer displayOrder;
    Integer addonCount;         // count of active addons in this group
}
```

---

#### `AddonGroupDetailResponseDTO` ← Includes addon list
```java
public class AddonGroupDetailResponseDTO {
    UUID id;
    String name;
    String description;
    String selectionType;
    Integer minSelections;
    Integer maxSelections;
    Boolean isRequired;
    Boolean isActive;
    Integer displayOrder;
    List<AddonResponseDTO> addons;   // all active addons in this group
}
```

---

#### `AddonResponseDTO`
```java
public class AddonResponseDTO {
    UUID id;
    UUID addonGroupId;
    String name;
    String description;
    BigDecimal price;
    Boolean isAvailable;
    Boolean isDefault;
    Integer displayOrder;
}
```

---

#### `TagResponseDTO`
```java
public class TagResponseDTO {
    UUID id;
    String name;
    String slug;
    String tagType;
    String description;
    String colorHex;
    String iconUrl;
    Boolean isRestaurantSpecific;   // true if restaurant_id is set
}
```

---

#### `ComboResponseDTO` ← Lean version for list
```java
public class ComboResponseDTO {
    UUID id;
    UUID menuId;
    String name;
    String description;
    String imageUrl;
    BigDecimal comboPrice;
    BigDecimal originalPrice;
    BigDecimal discountPrice;
    Boolean isAvailable;
    OffsetDateTime validFrom;
    OffsetDateTime validTo;
    List<String> availableDays;
    String badge;
    Integer displayOrder;
    Integer slotCount;          // number of items in the combo
}
```

---

#### `ComboDetailResponseDTO` ← Includes all slots
```java
public class ComboDetailResponseDTO {
    // All fields from ComboResponseDTO, plus:
    UUID id;
    String aiDescription;
    Integer maxQuantityPerOrder;
    List<ComboItemResponseDTO> items;
}
```

---

#### `ComboItemResponseDTO`
```java
public class ComboItemResponseDTO {
    UUID id;
    UUID comboId;
    UUID menuItemId;
    String menuItemName;        // denormalized for dashboard display
    String menuItemImage;       // denormalized
    UUID variantId;
    String variantName;         // denormalized, null if no variant
    Integer quantity;
    Boolean isSubstitutable;
    UUID substitutionCategoryId;
    String substitutionCategoryName;
    Integer displayOrder;
    String note;
}
```

---

## 8. Repository Interfaces — Full List

All repositories extend `JpaRepository<Entity, UUID>`. Every custom query filters `deleted = 0`. Use `@Query` with JPQL for custom fetches and `@EntityGraph` to avoid N+1 on specific read paths.

---

### `MenuRepository`
```java
public interface MenuRepository extends JpaRepository<Menu, UUID> {

    // List all active menus for a restaurant
    List<Menu> findByRestaurantIdAndDeleted(UUID restaurantId, Short deleted);

    // Check a menu belongs to a restaurant before any write
    Optional<Menu> findByIdAndRestaurantIdAndDeleted(UUID id, UUID restaurantId, Short deleted);
}
```

---

### `MenuCategoryRepository`
```java
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {

    // All active categories for a menu, ordered by display_order
    List<MenuCategory> findByMenuIdAndDeletedOrderByDisplayOrderAsc(UUID menuId, Short deleted);

    // Ownership check
    Optional<MenuCategory> findByIdAndRestaurantIdAndDeleted(UUID id, UUID restaurantId, Short deleted);

    // All top-level categories (no parent) for a menu
    List<MenuCategory> findByMenuIdAndParentCategoryIsNullAndDeleted(UUID menuId, Short deleted);

    // Children of a specific parent category
    List<MenuCategory> findByParentCategoryIdAndDeleted(UUID parentId, Short deleted);

    // Count of active items in a category (for response DTO)
    @Query("SELECT COUNT(mi) FROM MenuItem mi WHERE mi.category.id = :categoryId AND mi.deleted = 0")
    Integer countActiveItemsByCategoryId(@Param("categoryId") UUID categoryId);
}
```

---

### `MenuItemRepository`
```java
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    // Paginated list for a menu with optional filters
    @Query("""
        SELECT mi FROM MenuItem mi
        WHERE mi.menu.id = :menuId
          AND mi.deleted = 0
          AND (:categoryId IS NULL OR mi.category.id = :categoryId)
          AND (:isAvailable IS NULL OR mi.isAvailable = :isAvailable)
          AND (:isVeg IS NULL OR mi.isVeg = :isVeg)
          AND (:search IS NULL OR LOWER(mi.name) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY mi.displayOrder ASC
        """)
    Page<MenuItem> findAllWithFilters(
        @Param("menuId") UUID menuId,
        @Param("categoryId") UUID categoryId,
        @Param("isAvailable") Boolean isAvailable,
        @Param("isVeg") Boolean isVeg,
        @Param("search") String search,
        Pageable pageable
    );

    // Rich fetch for detail page — loads category in one query
    @EntityGraph(attributePaths = {"category", "menu"})
    Optional<MenuItem> findByIdAndRestaurantIdAndDeleted(UUID id, UUID restaurantId, Short deleted);

    // Ownership check
    Optional<MenuItem> findByIdAndMenuIdAndRestaurantIdAndDeleted(
        UUID id, UUID menuId, UUID restaurantId, Short deleted);
}
```

---

### `MenuItemVariantRepository`
```java
public interface MenuItemVariantRepository extends JpaRepository<MenuItemVariant, UUID> {

    // All active variants for an item, ordered
    List<MenuItemVariant> findByMenuItemIdAndDeletedOrderByDisplayOrderAsc(UUID menuItemId, Short deleted);

    // Ownership + item check
    Optional<MenuItemVariant> findByIdAndMenuItemIdAndRestaurantIdAndDeleted(
        UUID id, UUID menuItemId, UUID restaurantId, Short deleted);

    // Count active variants — used to update has_variants flag
    Integer countByMenuItemIdAndDeleted(UUID menuItemId, Short deleted);

    // Clear is_default on all siblings before setting new default
    @Modifying
    @Query("UPDATE MenuItemVariant v SET v.isDefault = false WHERE v.menuItem.id = :menuItemId AND v.deleted = 0")
    void clearDefaultByMenuItemId(@Param("menuItemId") UUID menuItemId);
}
```

---

### `MenuItemAddonGroupRepository`
```java
public interface MenuItemAddonGroupRepository extends JpaRepository<MenuItemAddonGroup, UUID> {

    // All active addon groups for a restaurant
    Page<MenuItemAddonGroup> findByRestaurantIdAndDeleted(UUID restaurantId, Short deleted, Pageable pageable);

    // Ownership check
    Optional<MenuItemAddonGroup> findByIdAndRestaurantIdAndDeleted(UUID id, UUID restaurantId, Short deleted);
}
```

---

### `MenuItemAddonRepository`
```java
public interface MenuItemAddonRepository extends JpaRepository<MenuItemAddon, UUID> {

    // All active addons for a group, ordered
    List<MenuItemAddon> findByAddonGroupIdAndDeletedOrderByDisplayOrderAsc(UUID groupId, Short deleted);

    // Ownership check
    Optional<MenuItemAddon> findByIdAndAddonGroupIdAndRestaurantIdAndDeleted(
        UUID id, UUID groupId, UUID restaurantId, Short deleted);
}
```

---

### `MenuItemAddonGroupMapRepository`
```java
public interface MenuItemAddonGroupMapRepository extends JpaRepository<MenuItemAddonGroupMap, UUID> {

    // All active links for an item with addon group details
    @Query("""
        SELECT m FROM MenuItemAddonGroupMap m
        JOIN FETCH m.addonGroup g
        WHERE m.menuItem.id = :menuItemId
          AND m.deleted = 0
          AND g.deleted = 0
        ORDER BY m.displayOrder ASC
        """)
    List<MenuItemAddonGroupMap> findByMenuItemIdWithGroup(@Param("menuItemId") UUID menuItemId);

    // Check if link already exists (prevent duplicates)
    boolean existsByMenuItemIdAndAddonGroupIdAndDeleted(UUID menuItemId, UUID addonGroupId, Short deleted);

    // Count active links — used to update has_addons flag
    Integer countByMenuItemIdAndDeleted(UUID menuItemId, Short deleted);

    // Find specific link for unlink / reorder
    Optional<MenuItemAddonGroupMap> findByMenuItemIdAndAddonGroupIdAndDeleted(
        UUID menuItemId, UUID addonGroupId, Short deleted);
}
```

---

### `MenuItemTagRepository`
```java
public interface MenuItemTagRepository extends JpaRepository<MenuItemTag, UUID> {

    // All platform-wide tags + restaurant-specific tags visible to this restaurant
    @Query("""
        SELECT t FROM MenuItemTag t
        WHERE (t.restaurant IS NULL OR t.restaurant.id = :restaurantId)
          AND t.deleted = 0
          AND t.isActive = true
        ORDER BY t.tagType, t.name
        """)
    List<MenuItemTag> findAllVisibleToRestaurant(@Param("restaurantId") UUID restaurantId);

    // Filter by tag type
    @Query("""
        SELECT t FROM MenuItemTag t
        WHERE (t.restaurant IS NULL OR t.restaurant.id = :restaurantId)
          AND t.tagType = :tagType
          AND t.deleted = 0
          AND t.isActive = true
        """)
    List<MenuItemTag> findByTagTypeVisibleToRestaurant(
        @Param("restaurantId") UUID restaurantId,
        @Param("tagType") String tagType);

    // Ownership check — only for restaurant-specific tags
    Optional<MenuItemTag> findByIdAndRestaurantIdAndDeleted(UUID id, UUID restaurantId, Short deleted);

    // Slug uniqueness check
    boolean existsBySlugAndRestaurantIdAndDeleted(String slug, UUID restaurantId, Short deleted);
}
```

---

### `MenuItemTagMapRepository`
```java
public interface MenuItemTagMapRepository extends JpaRepository<MenuItemTagMap, UUID> {

    // All active tags applied to an item
    @Query("""
        SELECT m FROM MenuItemTagMap m
        JOIN FETCH m.tag t
        WHERE m.menuItem.id = :menuItemId
          AND m.deleted = 0
          AND t.deleted = 0
          AND t.isActive = true
        """)
    List<MenuItemTagMap> findByMenuItemIdWithTag(@Param("menuItemId") UUID menuItemId);

    // Check if tag already applied
    boolean existsByMenuItemIdAndTagIdAndDeleted(UUID menuItemId, UUID tagId, Short deleted);

    // Find specific map row for removal
    Optional<MenuItemTagMap> findByMenuItemIdAndTagIdAndDeleted(UUID menuItemId, UUID tagId, Short deleted);
}
```

---

### `ComboRepository`
```java
public interface ComboRepository extends JpaRepository<Combo, UUID> {

    Page<Combo> findByMenuIdAndRestaurantIdAndDeleted(
        UUID menuId, UUID restaurantId, Short deleted, Pageable pageable);

    Optional<Combo> findByIdAndMenuIdAndRestaurantIdAndDeleted(
        UUID id, UUID menuId, UUID restaurantId, Short deleted);
}
```

---

### `ComboItemRepository`
```java
public interface ComboItemRepository extends JpaRepository<ComboItem, UUID> {

    // All active slots for a combo with item and variant details
    @Query("""
        SELECT ci FROM ComboItem ci
        JOIN FETCH ci.menuItem mi
        LEFT JOIN FETCH ci.variant v
        WHERE ci.combo.id = :comboId
          AND ci.deleted = 0
        ORDER BY ci.displayOrder ASC
        """)
    List<ComboItem> findByComboIdWithDetails(@Param("comboId") UUID comboId);

    Optional<ComboItem> findByIdAndComboIdAndDeleted(UUID id, UUID comboId, Short deleted);
}
```

---

## 9. Service Classes and Methods

Each service class is a `@Service` `@Transactional` class. Every method that writes data is `@Transactional`. Read-only methods use `@Transactional(readOnly = true)`.

---

### `MenuService`

```
createMenu(UUID restaurantId, UUID staffId, MenuRequestDTO request) → MenuResponseDTO
getAllMenus(UUID restaurantId, Pageable pageable)                    → Page<MenuResponseDTO>
getMenuById(UUID restaurantId, UUID menuId)                         → MenuResponseDTO
updateMenu(UUID restaurantId, UUID menuId, MenuRequestDTO request)  → MenuResponseDTO
toggleMenuStatus(UUID restaurantId, UUID menuId, boolean isActive)  → MenuResponseDTO
deleteMenu(UUID restaurantId, UUID menuId)                          → void
```

Internal helpers:
```
validateMenuOwnership(UUID menuId, UUID restaurantId)               → Menu (or throw)
toResponseDTO(Menu menu)                                            → MenuResponseDTO
```

---

### `MenuCategoryService`

```
createCategory(UUID restaurantId, UUID menuId, UUID staffId, MenuCategoryRequestDTO) → MenuCategoryResponseDTO
getCategories(UUID restaurantId, UUID menuId)                                        → List<MenuCategoryResponseDTO>
getCategoryById(UUID restaurantId, UUID menuId, UUID categoryId)                     → MenuCategoryResponseDTO
updateCategory(UUID restaurantId, UUID menuId, UUID categoryId, MenuCategoryRequestDTO) → MenuCategoryResponseDTO
updateDisplayOrder(UUID restaurantId, UUID menuId, UUID categoryId, int order)       → MenuCategoryResponseDTO
deleteCategory(UUID restaurantId, UUID menuId, UUID categoryId)                      → void
```

---

### `MenuItemService`

```
createItem(UUID restaurantId, UUID menuId, UUID staffId, MenuItemRequestDTO)        → MenuItemResponseDTO
getItems(UUID restaurantId, UUID menuId, MenuItemFilterParams, Pageable)             → Page<MenuItemResponseDTO>
getItemDetail(UUID restaurantId, UUID menuId, UUID itemId)                           → MenuItemDetailResponseDTO
updateItem(UUID restaurantId, UUID menuId, UUID itemId, MenuItemRequestDTO)          → MenuItemResponseDTO
toggleAvailability(UUID restaurantId, UUID menuId, UUID itemId, boolean available)   → MenuItemResponseDTO
reassignCategory(UUID restaurantId, UUID menuId, UUID itemId, UUID categoryId)       → MenuItemResponseDTO
updateDisplayOrder(UUID restaurantId, UUID menuId, UUID itemId, int order)           → MenuItemResponseDTO
deleteItem(UUID restaurantId, UUID menuId, UUID itemId)                              → void

// Internal — called by variant and addon services
refreshHasVariants(UUID itemId)                                                      → void
refreshHasAddons(UUID itemId)                                                        → void
```

---

### `MenuItemVariantService`

```
addVariant(UUID restaurantId, UUID itemId, MenuItemVariantRequestDTO)                → MenuItemVariantResponseDTO
getVariants(UUID restaurantId, UUID itemId)                                          → List<MenuItemVariantResponseDTO>
updateVariant(UUID restaurantId, UUID itemId, UUID variantId, MenuItemVariantRequestDTO) → MenuItemVariantResponseDTO
setDefault(UUID restaurantId, UUID itemId, UUID variantId)                           → MenuItemVariantResponseDTO
toggleAvailability(UUID restaurantId, UUID itemId, UUID variantId, boolean available) → MenuItemVariantResponseDTO
deleteVariant(UUID restaurantId, UUID itemId, UUID variantId)                        → void
```

After any create or delete: calls `menuItemService.refreshHasVariants(itemId)`.  
`setDefault` first calls `variantRepository.clearDefaultByMenuItemId(itemId)` then sets the target variant's `isDefault = true`, all in one transaction.

---

### `AddonGroupService`

```
createGroup(UUID restaurantId, UUID staffId, AddonGroupRequestDTO)                   → AddonGroupResponseDTO
getGroups(UUID restaurantId, Pageable)                                               → Page<AddonGroupResponseDTO>
getGroupDetail(UUID restaurantId, UUID groupId)                                      → AddonGroupDetailResponseDTO
updateGroup(UUID restaurantId, UUID groupId, AddonGroupRequestDTO)                   → AddonGroupResponseDTO
toggleGroupStatus(UUID restaurantId, UUID groupId, boolean isActive)                 → AddonGroupResponseDTO
deleteGroup(UUID restaurantId, UUID groupId)                                         → void
```

---

### `AddonService`

```
addAddon(UUID restaurantId, UUID groupId, AddonRequestDTO)                           → AddonResponseDTO
getAddons(UUID restaurantId, UUID groupId)                                           → List<AddonResponseDTO>
updateAddon(UUID restaurantId, UUID groupId, UUID addonId, AddonRequestDTO)          → AddonResponseDTO
toggleAvailability(UUID restaurantId, UUID groupId, UUID addonId, boolean available) → AddonResponseDTO
deleteAddon(UUID restaurantId, UUID groupId, UUID addonId)                           → void
```

---

### `AddonGroupItemService`

```
linkGroupToItem(UUID restaurantId, UUID itemId, AddonGroupItemRequestDTO)            → void
getGroupsForItem(UUID restaurantId, UUID itemId)                                     → List<AddonGroupDetailResponseDTO>
updateDisplayOrder(UUID restaurantId, UUID itemId, UUID groupId, int order)          → void
unlinkGroupFromItem(UUID restaurantId, UUID itemId, UUID groupId)                    → void
```

After any link or unlink: calls `menuItemService.refreshHasAddons(itemId)`.  
Before linking: calls `addonGroupItemMapRepository.existsByMenuItemIdAndAddonGroupIdAndDeleted(...)` to prevent duplicate links.

---

### `TagService`

```
createTag(UUID restaurantId, UUID staffId, TagRequestDTO)                            → TagResponseDTO
getAllTags(UUID restaurantId, String tagType)                                         → List<TagResponseDTO>
updateTag(UUID restaurantId, UUID tagId, TagRequestDTO)                              → TagResponseDTO
deleteTag(UUID restaurantId, UUID tagId)                                             → void
```

Before creating: checks slug uniqueness via `tagRepository.existsBySlugAndRestaurantIdAndDeleted(...)`.

---

### `TagItemService`

```
applyTag(UUID restaurantId, UUID itemId, TagItemRequestDTO)                          → void
getTagsForItem(UUID restaurantId, UUID itemId)                                       → List<TagResponseDTO>
removeTag(UUID restaurantId, UUID itemId, UUID tagId)                                → void
```

Before applying: checks `tagMapRepository.existsByMenuItemIdAndTagIdAndDeleted(...)` to prevent duplicate application.

---

### `ComboService`

```
createCombo(UUID restaurantId, UUID menuId, UUID staffId, ComboRequestDTO)           → ComboResponseDTO
getCombos(UUID restaurantId, UUID menuId, Pageable)                                  → Page<ComboResponseDTO>
getComboDetail(UUID restaurantId, UUID menuId, UUID comboId)                         → ComboDetailResponseDTO
updateCombo(UUID restaurantId, UUID menuId, UUID comboId, ComboRequestDTO)           → ComboResponseDTO
toggleAvailability(UUID restaurantId, UUID menuId, UUID comboId, boolean available)  → ComboResponseDTO
deleteCombo(UUID restaurantId, UUID menuId, UUID comboId)                            → void
```

---

### `ComboItemService`

```
addSlot(UUID restaurantId, UUID comboId, ComboItemRequestDTO)                        → ComboItemResponseDTO
getSlots(UUID restaurantId, UUID comboId)                                            → List<ComboItemResponseDTO>
updateSlot(UUID restaurantId, UUID comboId, UUID slotId, ComboItemRequestDTO)        → ComboItemResponseDTO
deleteSlot(UUID restaurantId, UUID comboId, UUID slotId)                             → void
```

Validates: if `isSubstitutable = true` in the request, `substitutionCategoryId` must not be null. Enforced in the service, not just the DTO.

---

## 10. Soft Delete Convention

All services follow this exact pattern for delete operations:

```java
// In any service delete method
entity.setDeleted((short) 1);
entity.setUpdatedAt(OffsetDateTime.now());
repository.save(entity);
// Never call repository.delete(entity)
```

All repository queries include `deleted = 0` either via:
- `@SQLRestriction("deleted = 0")` on the entity class (applies globally)
- Explicit `findBy...AndDeleted(...)` method parameters where the restriction needs to be explicit

---

## 11. Error Handling Strategy

Create a single `@RestControllerAdvice` class `GlobalExceptionHandler` that handles all exceptions and maps them to consistent HTTP responses.

| Exception | HTTP Status | When Thrown |
|---|---|---|
| `ResourceNotFoundException` | `404 Not Found` | Entity not found by ID |
| `AccessDeniedException` | `403 Forbidden` | Ownership check fails |
| `DuplicateResourceException` | `409 Conflict` | Duplicate slug, duplicate link |
| `ValidationException` | `400 Bad Request` | Business rule violation |
| `MethodArgumentNotValidException` | `400 Bad Request` | `@Valid` fails on DTO |
| `Exception` (catch-all) | `500 Internal Server Error` | Unexpected errors |

All error responses follow the same envelope:

```json
{
  "success": false,
  "message": "MenuItem not found with id: uuid-xyz",
  "errorCode": "RESOURCE_NOT_FOUND",
  "timestamp": "2026-04-05T10:30:00Z"
}
```

---

## 12. Validation Strategy

**Two layers of validation — both are required:**

**Layer 1 — DTO validation (`@Valid`):**  
All request DTO fields are annotated with `jakarta.validation` constraints (`@NotBlank`, `@NotNull`, `@Positive`, `@Min`, `@Max`, `@Size`, `@Pattern`). The controller `@Valid` annotation triggers this before the service is even called.

**Layer 2 — Business validation (service):**  
Checks that cannot be expressed as simple field annotations:
- Does the `categoryId` in a create item request actually belong to this restaurant?
- If `isSubstitutable = true` on a combo slot, is `substitutionCategoryId` provided?
- Is the slug unique before creating a tag?
- Does the addon group being linked to an item belong to the same restaurant?

Business validation throws typed exceptions caught by `GlobalExceptionHandler`.

---

## 13. Response Envelope

Every controller response is wrapped in a standard `ApiResponse<T>` envelope:

```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private OffsetDateTime timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, OffsetDateTime.now());
    }
}
```

Example successful list response:
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  },
  "timestamp": "2026-04-05T10:30:00Z"
}
```

---

## 14. API Versioning and URL Structure

All endpoints follow this URL pattern:

```
/api/v1/{resource-root}/{resourceId}/{sub-resource}/{subResourceId}/{action}
```

**Examples:**
```
POST   /api/v1/menus
GET    /api/v1/menus/{menuId}/items
GET    /api/v1/menus/{menuId}/items/{itemId}
PATCH  /api/v1/menus/{menuId}/items/{itemId}/availability
POST   /api/v1/items/{itemId}/variants
DELETE /api/v1/items/{itemId}/addon-groups/{groupId}
POST   /api/v1/addon-groups
PATCH  /api/v1/addon-groups/{groupId}/status
```

**Rules:**
- All nouns in URLs are plural and kebab-case
- Actions (`availability`, `status`, `default`, `order`) are sub-paths on PATCH, never verbs in the main path
- `restaurantId` never appears in the URL — it comes from the JWT
- IDs in URL paths are always UUID strings

---

## 15. Phase-by-Phase Checklist

Use this as your development checklist. Do not move to the next phase until every item in the current phase is checked.

---

### Phase 1 — Foundation

- [ ] `Menu` entity with `@SQLRestriction("deleted = 0")`
- [ ] `MenuRepository` interface with ownership methods
- [ ] `MenuService` — all 6 methods implemented and tested
- [ ] `MenuRequestDTO` and `MenuResponseDTO` defined
- [ ] `MenuController` — all 6 endpoints wired and tested via Postman
- [ ] `RestaurantAdminMiddleware` confirmed working with `RequestContextUtil`
- [ ] `GlobalExceptionHandler` set up with `ResourceNotFoundException` and `AccessDeniedException`
- [ ] `ApiResponse<T>` envelope working on all responses
- [ ] `MenuCategory` entity, repository, service, DTOs, controller (6 endpoints)
- [ ] `MenuItem` entity, repository, service, DTOs, controller (8 endpoints — basic fields only)
- [ ] Soft delete verified: `DELETE` sets `deleted = 1`, row not returned in subsequent GETs

---

### Phase 2 — Item Enrichment

- [ ] `MenuItemVariant` entity, repository, service, DTOs, controller (6 endpoints)
- [ ] `refreshHasVariants` in `MenuItemService` called on variant create/delete — verified
- [ ] `clearDefaultByMenuItemId` called before setting new default variant — verified
- [ ] `MenuItemAddonGroup` entity, repository, service, DTOs, controller (6 endpoints)
- [ ] `MenuItemAddon` entity, repository, service, DTOs, controller (5 endpoints)
- [ ] `MenuItemAddonGroupMap` entity, repository, service, controller (4 endpoints)
- [ ] Duplicate link prevention verified (same group can't be linked to same item twice)
- [ ] `refreshHasAddons` in `MenuItemService` called on link/unlink — verified

---

### Phase 3 — Taxonomy

- [ ] `MenuItemTag` entity, repository, service, DTOs, controller (5 endpoints)
- [ ] Slug uniqueness check working on tag create
- [ ] Platform-wide tags (restaurant_id IS NULL) visible to all restaurants in GET
- [ ] `MenuItemTagMap` entity, repository, service, controller (3 endpoints)
- [ ] Duplicate tag application prevention verified

---

### Phase 4 — Bundles

- [ ] `Combo` entity, repository, service, DTOs, controller (6 endpoints)
- [ ] `ComboItem` entity, repository, service, DTOs, controller (4 endpoints)
- [ ] `isSubstitutable = true` requires `substitutionCategoryId` — business validation verified
- [ ] `variant_id` nullable — combo item works with and without variant specified

---

### Phase 5 — Dashboard Read Layer

- [ ] `MenuItemDetailResponseDTO` returns variants + addon groups + tags in one response
- [ ] N+1 verified absent — single query using `JOIN FETCH` or `@EntityGraph`
- [ ] `MenuItemController` GET list filters working: `categoryId`, `isAvailable`, `isVeg`, `search`
- [ ] Pagination working on all list endpoints: `page`, `size`, `sort` params
- [ ] Item count on `MenuCategoryResponseDTO` — verified accurate
- [ ] Slot count on `ComboResponseDTO` — verified accurate
- [ ] Addon count on `AddonGroupResponseDTO` — verified accurate

---

*End of Strategy Document*
