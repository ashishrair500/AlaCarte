# AlaCarte - Current Database Schema

**Database:** PostgreSQL  
**Schema Management:** Hibernate `ddl-auto=update` (no Flyway/Liquibase migrations)  
**Primary Key Strategy:** UUID with `GenerationType.AUTO`

---

## Entity Relationship Overview

```
SuperAdmin
    │
    └── creates ──→ Restaurant (1)
                        │
                        ├──→ (N) Menu
                        │        │
                        │        └──→ (N) MenuItem
                        │
                        ├──→ (N) RestaurantStaff
                        │
                        └──→ (N) Customer
```

---

## Tables

### 1. `restaurants`

**Entity:** `Restaurant.java`  
**Description:** Core restaurant entity. Managed by SuperAdmin during onboarding.

| Column | DB Type | Nullable | Default | Notes |
|--------|---------|----------|---------|-------|
| `id` | UUID (PK) | NO | auto-generated | |
| `name` | VARCHAR(255) | NO | | |
| `description` | TEXT | YES | | |
| `address_line1` | VARCHAR(255) | NO | | |
| `address_line2` | VARCHAR(255) | YES | | |
| `city` | VARCHAR(100) | NO | | |
| `state` | VARCHAR(100) | NO | | |
| `pincode` | VARCHAR(20) | NO | | |
| `latitude` | DECIMAL(10,7) | YES | | |
| `longitude` | DECIMAL(10,7) | YES | | |
| `phone` | VARCHAR(20) | YES | | |
| `email` | VARCHAR(100) | YES | | |
| `cuisine_types` | TEXT[] | YES | | PostgreSQL array |
| `price_range` | INTEGER | YES | | 1-3 scale |
| `avg_cost_for_two` | DECIMAL(10,2) | YES | | |
| `opening_time` | TIME | YES | | |
| `closing_time` | TIME | YES | | |
| `rating` | DECIMAL(2,1) | YES | | |
| `total_ratings` | INTEGER | YES | `0` | |
| `is_veg_only` | BOOLEAN | YES | `false` | |
| `has_parking` | BOOLEAN | YES | `false` | |
| `is_popular` | BOOLEAN | YES | `false` | |
| `restaurant_type` | VARCHAR(50) | YES | | cafe, fine_dine, hotel, etc. |
| `hotel_star_category` | INTEGER | YES | | |
| `service_fee` | DECIMAL(10,2) | YES | | |
| `reservation_required` | BOOLEAN | YES | `false` | |
| `dress_code` | VARCHAR(100) | YES | | |
| `menu_qr_url` | TEXT | YES | | |
| `featured_image_url` | TEXT | YES | | |
| `gallery_images` | TEXT[] | YES | | PostgreSQL array |
| `status` | VARCHAR(30) | YES | `'open'` | open / closed / temp_closed |
| `created_at` | TIMESTAMPTZ | NO | now() | immutable after insert |
| `updated_at` | TIMESTAMPTZ | YES | now() | auto-updated |
| `created_by` | UUID | YES | | FK to `super_admin.id` (logical, not enforced) |

**Indexes:** None defined beyond PK.

---

### 2. `menu`

**Entity:** `Menu.java`  
**Description:** A menu belongs to a restaurant (e.g., Breakfast, Lunch, Dinner, Bar Menu). Supports seasonal validity windows.

| Column | DB Type | Nullable | Default | Notes |
|--------|---------|----------|---------|-------|
| `id` | UUID (PK) | NO | auto-generated | |
| `restaurant_id` | UUID | NO | | FK to `restaurants.id` (logical, not enforced) |
| `name` | TEXT | NO | | e.g., "Breakfast", "Lunch" |
| `description` | TEXT | YES | | |
| `menu_type` | TEXT | YES | | BREAKFAST / LUNCH / BAR / CUSTOM |
| `is_active` | BOOLEAN | YES | `true` | soft toggle |
| `valid_from` | TIMESTAMPTZ | YES | | seasonal start |
| `valid_to` | TIMESTAMPTZ | YES | | seasonal end |
| `display_order` | INTEGER | YES | `0` | UI ordering |
| `cover_image` | TEXT | YES | | banner image URL |
| `meta` | JSONB | YES | | extensible metadata |
| `created_at` | TIMESTAMPTZ | NO | now() | immutable after insert |
| `updated_at` | TIMESTAMPTZ | NO | now() | auto-updated |
| `created_by` | UUID | YES | | who created |
| `updated_by` | UUID | YES | | who last updated |

**Indexes:**
- `idx_menu_restaurant` on `(restaurant_id)`
- `idx_menu_active` on `(restaurant_id, is_active)`

---

### 3. `menu_item`

**Entity:** `MenuItem.java`  
**Description:** Individual food/drink items within a menu. Contains pricing, dietary flags, variants (JSONB), addons (JSONB), nutrition info, and tags.

| Column | DB Type | Nullable | Default | Notes |
|--------|---------|----------|---------|-------|
| `id` | UUID (PK) | NO | auto-generated | |
| `restaurant_id` | UUID | NO | | FK to `restaurants.id` (logical) |
| `menu_id` | UUID | NO | | FK to `menu.id` (logical) |
| `name` | TEXT | NO | | |
| `description` | TEXT | YES | | |
| `category` | TEXT | YES | | plain string — e.g., "Pizza", "Starters" |
| `price` | DECIMAL(10,2) | NO | | base price |
| `discount_price` | DECIMAL(10,2) | YES | | optional sale price |
| `is_available` | BOOLEAN | YES | `true` | staff toggles daily |
| `is_veg` | BOOLEAN | YES | | |
| `is_spicy` | BOOLEAN | YES | | |
| `contains_egg` | BOOLEAN | YES | | |
| `variants` | JSONB | YES | | untyped — sizes, flavors, etc. |
| `addons` | JSONB | YES | | untyped — toppings, extras |
| `tags` | TEXT[] | YES | | PostgreSQL array — gluten-free, vegan, etc. |
| `image_url` | TEXT | YES | | primary image |
| `gallery` | JSONB | YES | | list of image URLs |
| `nutrition` | JSONB | YES | | calories, protein, carbs, etc. |
| `display_order` | INTEGER | YES | `0` | ordering within menu/category |
| `badge` | TEXT | YES | | "Chef's Special", "New", "Bestseller" |
| `meta` | JSONB | YES | | extensible metadata |
| `created_at` | TIMESTAMPTZ | YES | now() | immutable after insert |
| `updated_at` | TIMESTAMPTZ | YES | now() | auto-updated |
| `deleted_at` | TIMESTAMPTZ | YES | | soft delete timestamp |

**Indexes:** None defined beyond PK.

---

### 4. `restaurant_staff`

**Entity:** `RestaurantStaff.java`  
**Description:** Staff members linked to a restaurant. Includes role, credentials, reporting hierarchy, shift metadata, and granular permissions.

| Column | DB Type | Nullable | Default | Notes |
|--------|---------|----------|---------|-------|
| `id` | UUID (PK) | NO | auto-generated | |
| `restaurant_id` | UUID | NO | | FK to `restaurants.id` (logical) |
| `employee_code` | TEXT | YES | | unique within a restaurant |
| `staff_role` | TEXT | NO | `'STAFF'` | CHEF, WAITER, MANAGER, BARTENDER, etc. |
| `designation` | TEXT | YES | | free-form job title |
| `reporting_to` | UUID | YES | | self-ref to another staff member |
| `phone` | TEXT | YES | | contact snapshot |
| `email` | TEXT | YES | | contact snapshot |
| `user_name` | TEXT | YES | | login username |
| `password_hash` | TEXT | YES | | hashed password |
| `status` | TEXT | NO | `'ACTIVE'` | ACTIVE / SUSPENDED / RESIGNED / TERMINATED |
| `joined_at` | TIMESTAMPTZ | YES | | |
| `left_at` | TIMESTAMPTZ | YES | | |
| `shift_meta` | JSONB | YES | | recurring shifts, timezone, weekly hours |
| `permissions` | JSONB | YES | | staff-specific permission overrides |
| `meta` | JSONB | YES | | extra metadata |
| `is_active` | BOOLEAN | NO | `true` | |
| `created_at` | TIMESTAMPTZ | NO | now() | immutable after insert |
| `updated_at` | TIMESTAMPTZ | NO | now() | auto-updated |
| `deleted_at` | TIMESTAMPTZ | YES | | soft delete |

**Indexes:** None defined beyond PK.

---

### 5. `customer`

**Entity:** `Customer.java`  
**Description:** End customers linked to a restaurant. Stores dietary preferences, allergies, visit history, and QR-based identification.

| Column | DB Type | Nullable | Default | Notes |
|--------|---------|----------|---------|-------|
| `id` | UUID (PK) | NO | auto-generated | |
| `restaurant_id` | UUID | NO | | FK to `restaurants.id` (logical) |
| `phone` | TEXT | YES | | UNIQUE constraint |
| `email` | TEXT | YES | | UNIQUE constraint |
| `full_name` | TEXT | YES | | |
| `dietary_pref` | TEXT | YES | | |
| `allergies` | TEXT[] | YES | | PostgreSQL array |
| `cuisine_preferences` | JSONB | YES | | |
| `health_goals` | JSONB | YES | | |
| `current_session_id` | UUID | YES | | active session reference |
| `last_visit_at` | TIMESTAMPTZ | YES | | |
| `visit_count` | INTEGER | YES | `0` | |
| `password_hash` | TEXT | YES | | optional login |
| `unique_qr_id` | TEXT | YES | | unique customer QR identifier |
| `is_active` | BOOLEAN | YES | `true` | |
| `deleted_at` | TIMESTAMPTZ | YES | | soft delete |
| `created_at` | TIMESTAMPTZ | YES | now() | immutable after insert |
| `updated_at` | TIMESTAMPTZ | YES | now() | auto-updated |
| `created_by` | UUID | YES | | |
| `updated_by` | UUID | YES | | |
| `meta` | JSONB | YES | | extensible metadata |
| `qr_code` | JSONB | YES | | QR code data |

**Indexes:** Unique constraints on `phone` and `email`.

---

### 6. `super_admin`

**Entity:** `SuperAdmin.java`  
**Description:** Platform-level administrators with the highest access. Manage restaurant onboarding and global operations.

| Column | DB Type | Nullable | Default | Notes |
|--------|---------|----------|---------|-------|
| `id` | UUID (PK) | NO | auto-generated | |
| `username` | TEXT | NO | | UNIQUE constraint |
| `password_hash` | TEXT | NO | | |
| `full_name` | TEXT | YES | | |
| `email` | TEXT | YES | | UNIQUE constraint |
| `phone` | TEXT | YES | | |
| `role_level` | SMALLINT | YES | `1` | |
| `permissions` | JSONB | YES | | |
| `is_active` | BOOLEAN | NO | `true` | |
| `created_at` | TIMESTAMPTZ | NO | now() | immutable after insert |
| `updated_at` | TIMESTAMPTZ | NO | now() | auto-updated |
| `created_by` | UUID | YES | | ref to another super_admin |
| `meta` | JSONB | YES | | |

**Indexes:** Unique constraints on `username` and `email`.

---

## Relationship Summary

| Relationship | Type | FK Column | Enforced? |
|--------------|------|-----------|-----------|
| Restaurant → SuperAdmin (created_by) | N:1 | `restaurants.created_by` | Logical only |
| Menu → Restaurant | N:1 | `menu.restaurant_id` | Logical only |
| MenuItem → Restaurant | N:1 | `menu_item.restaurant_id` | Logical only |
| MenuItem → Menu | N:1 | `menu_item.menu_id` | Logical only |
| RestaurantStaff → Restaurant | N:1 | `restaurant_staff.restaurant_id` | Logical only |
| RestaurantStaff → RestaurantStaff (reporting_to) | N:1 | `restaurant_staff.reporting_to` | Logical only |
| Customer → Restaurant | N:1 | `customer.restaurant_id` | Logical only |

> **Note:** All foreign keys are **logical** (UUID columns referencing other tables) but are **not enforced** at the database level via `@JoinColumn` / `@ManyToOne`. There are no JPA relationship annotations — all references are plain `UUID` columns.

---

## JSONB Fields (Untyped / Schema-less)

These columns store arbitrary JSON and have no enforced structure:

| Table | Column | Typical Contents |
|-------|--------|------------------|
| `menu` | `meta` | tags, styling, filters |
| `menu_item` | `variants` | `[{"size": "Large", "price": 250}, ...]` |
| `menu_item` | `addons` | `[{"name": "Extra Cheese", "price": 40}, ...]` |
| `menu_item` | `gallery` | `["url1", "url2", ...]` |
| `menu_item` | `nutrition` | `{"calories": 350, "protein": "12g", ...}` |
| `menu_item` | `meta` | arbitrary extra fields |
| `restaurant_staff` | `shift_meta` | recurring shifts, timezone, weekly hours |
| `restaurant_staff` | `permissions` | permission overrides |
| `restaurant_staff` | `meta` | extra metadata |
| `customer` | `cuisine_preferences` | preferred cuisines |
| `customer` | `health_goals` | dietary health goals |
| `customer` | `meta` | extra metadata |
| `customer` | `qr_code` | QR code configuration |
| `super_admin` | `permissions` | admin permissions |
| `super_admin` | `meta` | extra metadata |

---

## PostgreSQL Array Fields

| Table | Column | Element Type |
|-------|--------|--------------|
| `restaurants` | `cuisine_types` | TEXT |
| `restaurants` | `gallery_images` | TEXT |
| `menu_item` | `tags` | TEXT |
| `customer` | `allergies` | TEXT |

---

## Known Limitations

1. **No database-level FK constraints** — All relationships are logical UUID references without `@ManyToOne` / `@JoinColumn`. Orphaned records are possible.
2. **No indexes on `menu_item`** — At scale (millions of rows), queries on `restaurant_id`, `menu_id`, `category`, `is_available`, and `price` will degrade.
3. **Category is a plain string** — No dedicated entity, so no description, image, ordering, or nesting for categories.
4. **Variants and addons are untyped JSONB** — Cannot be indexed, queried efficiently, or reused across items.
5. **No combo/bundle support** — No entity or structure for bundled meal deals.
6. **No migration tooling** — Using `ddl-auto=update` with no Flyway/Liquibase versioning.
7. **Soft delete inconsistency** — `menu_item`, `restaurant_staff`, and `customer` have `deleted_at`; `menu` uses `is_active` only; `restaurants` uses `status`; `super_admin` uses `is_active`.
