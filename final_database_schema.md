# AlaCarte — Redesigned Menu Domain Schema

**Database:** PostgreSQL  
**Schema Management:** Flyway (recommended migration to replace `ddl-auto=update`)  
**Primary Key Strategy:** UUID (`gen_random_uuid()`)  
**Soft Delete Convention:** All tables use `deleted_at TIMESTAMPTZ` — active records always have `deleted_at IS NULL`  
**Timestamp Convention:** All timestamps are `TIMESTAMPTZ` (timezone-aware)  
**JPA Stack:** Spring Boot 3.2 · Hibernate 6.3 · `jakarta.persistence` · Java 21

---

## Table of Contents

1. [Entity Relationship Overview](#entity-relationship-overview)
2. [JPA Mapping Strategy](#jpa-mapping-strategy)
3. [menu_category](#1-menu_category)
4. [menu_item](#2-menu_item)
5. [menu_item_variant](#3-menu_item_variant)
6. [menu_item_addon_group](#4-menu_item_addon_group)
7. [menu_item_addon](#5-menu_item_addon)
8. [menu_item_addon_group_map](#6-menu_item_addon_group_map)
9. [menu_item_tag](#7-menu_item_tag)
10. [menu_item_tag_map](#8-menu_item_tag_map)
11. [combo](#9-combo)
12. [combo_item](#10-combo_item)
13. [Relationship Summary](#relationship-summary)
14. [Index Strategy](#index-strategy)
15. [Design Decisions & Notes](#design-decisions--notes)

---

## Entity Relationship Overview

```
restaurant
    │
    └──→ menu
              │
              ├──→ menu_category (supports self-referencing nesting)
              │         │
              │         └──→ menu_item
              │                   │
              │                   ├──→ menu_item_variant        (sizes, portions, flavors)
              │                   │
              │                   ├──→ menu_item_addon_group_map ──→ menu_item_addon_group
              │                   │                                          │
              │                   │                                          └──→ menu_item_addon
              │                   │
              │                   └──→ menu_item_tag_map ──→ menu_item_tag
              │
              └──→ combo
                        │
                        └──→ combo_item ──→ menu_item
                                      └──→ menu_item_variant (optional)
```

---

## JPA Mapping Strategy

### Why only `@ManyToOne` and no `@OneToMany` on parents?

`@OneToMany` on parent entities (e.g., `Menu.items`) loads potentially thousands of child records into memory whenever the parent is fetched — even with `LAZY`, it introduces N+1 risks and makes accidental eager loads expensive at scale. Instead:

- **Child → Parent navigation** is mapped with `@ManyToOne(fetch = LAZY)` + `@JoinColumn`.
- **Parent → Children queries** are done explicitly via `Repository` methods with `JOIN FETCH` or `@EntityGraph` — giving full control over what gets loaded per use case.
- This keeps every entity lightweight and fetch behaviour predictable.

### `@OneToOne` usage

`@OneToOne` applies only where a strict one-to-one structural relationship exists in the domain. In this schema, **no current table has a true `@OneToOne`** — every child table (`menu_item_variant`, `menu_item_addon`, etc.) can have multiple rows per parent. The annotation column is retained here for future use (e.g., if `menu_item_nutrition` is split into its own table, it would be `@OneToOne` back to `MenuItem`).

### Convention applied across all entities

```java
// Standard pattern used on every FK field in this schema
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "fk_column_name", nullable = false)   // nullable = true when the FK is optional
private ParentEntity parent;
```

- `fetch = FetchType.LAZY` is the default on all associations — never override to EAGER.
- `@JoinColumn(name = ...)` always explicitly names the DB column to match DDL exactly.
- `nullable` on `@JoinColumn` mirrors the DB column's nullable constraint.
- The raw UUID field (e.g., `private UUID menuId`) is **removed** wherever `@ManyToOne` is added — JPA manages the FK column through the object reference.

---

## 1. `menu_category`

**Description:**  
Replaces the plain `category` string column on `menu_item`. A category belongs to a specific menu and restaurant. Supports nested categories via a self-referencing `parent_category_id` — e.g., *Beverages → Hot Drinks → Coffee*. Nesting is limited by application logic (recommended max depth: 3 levels) to keep query complexity manageable.

This table is critical for LLM context generation — each category carries a description that can be fed into prompts to give the AI semantic understanding of what the category represents.

### Columns

| Column | DB Type | Nullable | Default | Description |
|--------|---------|----------|---------|-------------|
| `id` | `UUID` (PK) | NO | `gen_random_uuid()` | Primary key, auto-generated UUID |
| `restaurant_id` | `UUID` | NO | — | FK → `restaurants.id`. Denormalized for fast per-restaurant queries without joining through `menu` |
| `menu_id` | `UUID` | NO | — | FK → `menu.id`. A category belongs to exactly one menu |
| `parent_category_id` | `UUID` | YES | `NULL` | Self-referencing FK → `menu_category.id`. NULL = top-level category |
| `name` | `VARCHAR(150)` | NO | — | Display name of the category (e.g., "Starters", "Pizza", "Hot Drinks") |
| `description` | `TEXT` | YES | `NULL` | Human-readable description. Used as LLM context for AI prompts |
| `image_url` | `TEXT` | YES | `NULL` | Banner/thumbnail image URL for this category |
| `display_order` | `SMALLINT` | NO | `0` | Controls sort order within the same menu. Lower value = shown first |
| `is_active` | `BOOLEAN` | NO | `true` | Soft toggle to hide a category without deleting it |
| `meta` | `JSONB` | YES | `NULL` | Extensible field for future attributes (e.g., color codes, icons, UI hints) |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | Record creation time. Immutable after insert |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | Auto-updated on every row modification |
| `deleted_at` | `TIMESTAMPTZ` | YES | `NULL` | Soft delete. NULL = active; populated = deleted |
| `created_by` | `UUID` | YES | `NULL` | Audit: who created this record (staff or super_admin id) |
| `updated_by` | `UUID` | YES | `NULL` | Audit: who last updated this record |

### JPA Mappings — `MenuCategory.java`

```java
@Entity
@Table(name = "menu_category")
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // ---------------------------------------------------------------
    // @ManyToOne — many categories belong to one Menu
    // DB column: menu_id (NOT NULL)
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    // ---------------------------------------------------------------
    // @ManyToOne — many categories belong to one Restaurant
    // DB column: restaurant_id (NOT NULL)
    // Denormalized: avoids joining through menu on every query
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // ---------------------------------------------------------------
    // @ManyToOne (self-referencing) — supports nested category tree
    // DB column: parent_category_id (NULLABLE — NULL = top-level)
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id", nullable = true)
    private MenuCategory parentCategory;

    // Plain columns — no mapping annotation needed
    private String name;
    private String description;
    private String imageUrl;
    private Short displayOrder;
    private Boolean isActive;
    // created_by and updated_by are plain UUIDs — they can reference
    // either RestaurantStaff or SuperAdmin, so a typed @ManyToOne
    // cannot safely represent both. Resolved at the service layer.
    private UUID createdBy;
    private UUID updatedBy;
    // ... timestamps, meta (JSONB)
}
```

**Notes:**
- `parentCategory` is a self-referencing `@ManyToOne` on the same entity class — standard JPA, no special handling needed beyond ensuring the `nullable = true` is set.
- `createdBy` / `updatedBy` remain plain `UUID` fields throughout the entire schema for the same polymorphic reason noted above.

### Indexes

```sql
CREATE INDEX idx_category_menu         ON menu_category (menu_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_category_restaurant   ON menu_category (restaurant_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_category_parent       ON menu_category (parent_category_id) WHERE parent_category_id IS NOT NULL;
CREATE INDEX idx_category_display      ON menu_category (menu_id, display_order);
```

---

## 2. `menu_item`

**Description:**  
The core food/drink item entity. Variants and addons are fully normalized into their own tables. The plain `category` string column is replaced by a `@ManyToOne` to `MenuCategory`.

Two description fields exist intentionally: `description` is customer-facing (short, marketing-style), while `ai_description` is a richer structured text block pre-generated for LLM consumption — includes ingredients, preparation style, flavor profile, and dietary details.

### Columns

| Column | DB Type | Nullable | Default | Description |
|--------|---------|----------|---------|-------------|
| `id` | `UUID` (PK) | NO | `gen_random_uuid()` | Primary key |
| `restaurant_id` | `UUID` | NO | — | FK → `restaurants.id`. Denormalized for query performance |
| `menu_id` | `UUID` | NO | — | FK → `menu.id` |
| `category_id` | `UUID` | YES | `NULL` | FK → `menu_category.id`. NULL = uncategorized item |
| `name` | `VARCHAR(255)` | NO | — | Item display name (e.g., "Paneer Butter Masala") |
| `description` | `TEXT` | YES | `NULL` | Short customer-facing description shown on menu UI |
| `ai_description` | `TEXT` | YES | `NULL` | Rich LLM-ready description: ingredients, flavor, dietary notes, preparation. Pre-generated and stored — not computed at query time |
| `base_price` | `DECIMAL(10,2)` | NO | — | Default price when no variant is selected. Acts as "starting from" price when variants exist |
| `discount_price` | `DECIMAL(10,2)` | YES | `NULL` | Optional promotional price overriding `base_price`. Applied only when not NULL |
| `is_available` | `BOOLEAN` | NO | `true` | Daily availability toggle managed by staff. Does not affect soft-delete |
| `is_veg` | `BOOLEAN` | YES | `NULL` | True = vegetarian, False = non-veg, NULL = not classified |
| `is_vegan` | `BOOLEAN` | YES | `NULL` | Stricter than `is_veg` — excludes dairy and eggs |
| `is_spicy` | `BOOLEAN` | YES | `NULL` | Spice flag for quick filtering |
| `contains_egg` | `BOOLEAN` | YES | `NULL` | Separate from `is_veg` for egg-vegetarians |
| `contains_gluten` | `BOOLEAN` | YES | `NULL` | Allergen flag |
| `contains_nuts` | `BOOLEAN` | YES | `NULL` | Allergen flag |
| `contains_dairy` | `BOOLEAN` | YES | `NULL` | Allergen flag |
| `spice_level` | `SMALLINT` | YES | `NULL` | 1–5 scale. 1 = mild, 5 = very spicy. More granular than `is_spicy` |
| `prep_time_minutes` | `SMALLINT` | YES | `NULL` | Estimated preparation time in minutes. Used in AI context and customer-facing ETAs |
| `image_url` | `TEXT` | YES | `NULL` | Primary display image |
| `gallery` | `TEXT[]` | YES | `NULL` | PostgreSQL array of additional image URLs |
| `nutrition` | `JSONB` | YES | `NULL` | `{"calories": 350, "protein": "12g", "carbs": "45g", "fat": "8g", "fiber": "3g"}` |
| `ingredients` | `TEXT[]` | YES | `NULL` | Array of ingredient names. Used for allergen matching and LLM context |
| `cuisine_type` | `VARCHAR(100)` | YES | `NULL` | Cuisine classification (e.g., "North Indian", "Italian", "Chinese") |
| `course_type` | `VARCHAR(50)` | YES | `NULL` | Meal course: STARTER / MAIN / DESSERT / BEVERAGE / SNACK / BREAD / SOUP |
| `display_order` | `SMALLINT` | NO | `0` | Sort position within its category. Lower = first |
| `badge` | `VARCHAR(100)` | YES | `NULL` | Highlight badge: "Chef's Special", "Bestseller", "New", "Must Try" |
| `has_variants` | `BOOLEAN` | NO | `false` | Denormalized flag — true if active variants exist. Avoids a join to check |
| `has_addons` | `BOOLEAN` | NO | `false` | Denormalized flag — true if addon groups are linked. Avoids a join to check |
| `meta` | `JSONB` | YES | `NULL` | Extensible field for restaurant-specific or future attributes |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | Immutable creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | Auto-updated on modification |
| `deleted_at` | `TIMESTAMPTZ` | YES | `NULL` | Soft delete. NULL = active |
| `created_by` | `UUID` | YES | `NULL` | Audit: creator reference |
| `updated_by` | `UUID` | YES | `NULL` | Audit: last updater reference |

### JPA Mappings — `MenuItem.java`

```java
@Entity
@Table(name = "menu_item")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // ---------------------------------------------------------------
    // @ManyToOne — many items belong to one Restaurant
    // DB column: restaurant_id (NOT NULL)
    // Denormalized: avoids joins through menu → restaurant on hot paths
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // ---------------------------------------------------------------
    // @ManyToOne — many items belong to one Menu
    // DB column: menu_id (NOT NULL)
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    // ---------------------------------------------------------------
    // @ManyToOne — many items belong to one MenuCategory
    // DB column: category_id (NULLABLE — NULL = uncategorized)
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private MenuCategory category;

    // Plain columns
    private String name;
    private String description;
    private String aiDescription;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private Boolean isAvailable;
    private Boolean isVeg;
    private Boolean isVegan;
    private Boolean isSpicy;
    private Boolean containsEgg;
    private Boolean containsGluten;
    private Boolean containsNuts;
    private Boolean containsDairy;
    private Short spiceLevel;
    private Short prepTimeMinutes;
    private String imageUrl;
    private String cuisineType;
    private String courseType;
    private Short displayOrder;
    private String badge;
    private Boolean hasVariants;   // service-managed denormalized flag
    private Boolean hasAddons;     // service-managed denormalized flag
    private UUID createdBy;
    private UUID updatedBy;
    // ... timestamps, meta (JSONB), gallery (TEXT[]),
    //     ingredients (TEXT[]), nutrition (JSONB)
}
```

**Notes:**
- `category` is nullable (`nullable = true`) — items can exist without a category assignment.
- `restaurant` is denormalized intentionally and maps to a real `@ManyToOne` — it matches the DB column and avoids deep join chains on the most frequent query path.
- `hasVariants` and `hasAddons` are plain boolean columns managed by the service layer — not derived via JPA.

### Indexes

```sql
-- Most critical: rendering a full menu page
CREATE INDEX idx_item_menu_category    ON menu_item (menu_id, category_id, display_order) WHERE deleted_at IS NULL;

-- Staff availability dashboard
CREATE INDEX idx_item_availability     ON menu_item (restaurant_id, is_available) WHERE deleted_at IS NULL;

-- Price filtering (AI recommendations, customer filters)
CREATE INDEX idx_item_price            ON menu_item (restaurant_id, base_price) WHERE deleted_at IS NULL;

-- Dietary filtering
CREATE INDEX idx_item_dietary          ON menu_item (restaurant_id, is_veg, is_vegan) WHERE deleted_at IS NULL;

-- GIN index for full-text search on name + description
CREATE INDEX idx_item_fts              ON menu_item USING GIN (to_tsvector('english', name || ' ' || coalesce(description, '')));

-- GIN index for ingredients array
CREATE INDEX idx_item_ingredients      ON menu_item USING GIN (ingredients);
```

---

## 3. `menu_item_variant`

**Description:**  
Replaces the untyped `variants` JSONB column on `menu_item`. Each variant is a specific purchasable version of an item — e.g., Small / Medium / Full plate, Half / Full portion, Regular / Large drink.

A variant has its own price and availability, independently manageable by staff. When variants exist, the frontend prompts the customer to select one before adding to the order.

### Columns

| Column | DB Type | Nullable | Default | Description |
|--------|---------|----------|---------|-------------|
| `id` | `UUID` (PK) | NO | `gen_random_uuid()` | Primary key |
| `menu_item_id` | `UUID` | NO | — | FK → `menu_item.id`. The parent item this variant belongs to |
| `restaurant_id` | `UUID` | NO | — | FK → `restaurants.id`. Denormalized for fast restaurant-scoped queries |
| `variant_type` | `VARCHAR(50)` | NO | — | `SIZE` / `PORTION` / `FLAVOR` / `CRUST` / `TEMPERATURE` / `CUSTOM` |
| `name` | `VARCHAR(150)` | NO | — | Display label (e.g., "Small", "Medium", "Full", "Half", "Thin Crust") |
| `description` | `TEXT` | YES | `NULL` | Optional extra detail (e.g., "Serves 1–2 people") |
| `price` | `DECIMAL(10,2)` | NO | — | Price for this variant. Overrides `menu_item.base_price` when selected |
| `discount_price` | `DECIMAL(10,2)` | YES | `NULL` | Optional promotional price for this variant |
| `is_available` | `BOOLEAN` | NO | `true` | Toggled independently per variant |
| `is_default` | `BOOLEAN` | NO | `false` | If true, pre-selected when the item is shown. Only one per item should be true |
| `display_order` | `SMALLINT` | NO | `0` | Sort order within the item's variant list |
| `meta` | `JSONB` | YES | `NULL` | Extensible. E.g., weight in grams, serving size ml |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | Immutable creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | Auto-updated |
| `deleted_at` | `TIMESTAMPTZ` | YES | `NULL` | Soft delete |

### JPA Mappings — `MenuItemVariant.java`

```java
@Entity
@Table(name = "menu_item_variant")
public class MenuItemVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // ---------------------------------------------------------------
    // @ManyToOne — many variants belong to one MenuItem
    // DB column: menu_item_id (NOT NULL)
    // Primary parent relationship for a variant
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    // ---------------------------------------------------------------
    // @ManyToOne — many variants belong to one Restaurant
    // DB column: restaurant_id (NOT NULL)
    // Denormalized: enables restaurant-scoped availability queries
    // without joining through menu_item
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // Plain columns
    private String variantType;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Boolean isAvailable;
    private Boolean isDefault;
    private Short displayOrder;
    // ... timestamps, meta (JSONB)
}
```

**Notes:**
- No `@OneToOne` here — one `MenuItem` can have many variants (Small, Medium, Large), making this definitively `@ManyToOne`.
- `is_default` must be enforced at the service layer: when setting a variant as default, unset the flag on all sibling variants first within the same transaction.

### Indexes

```sql
CREATE INDEX idx_variant_item          ON menu_item_variant (menu_item_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_variant_restaurant    ON menu_item_variant (restaurant_id, is_available) WHERE deleted_at IS NULL;
```

---

## 4. `menu_item_addon_group`

**Description:**  
A named set of optional (or required) choices attachable to one or more menu items — e.g., "Choose Toppings", "Select Sauce", "Add Extras".

Groups are defined at the **restaurant level** and reused across items — avoiding duplication of "Extra Cheese" for every pizza variant. `selection_type` controls whether a customer can pick one or multiple options.

### Columns

| Column | DB Type | Nullable | Default | Description |
|--------|---------|----------|---------|-------------|
| `id` | `UUID` (PK) | NO | `gen_random_uuid()` | Primary key |
| `restaurant_id` | `UUID` | NO | — | FK → `restaurants.id`. Groups are scoped to a restaurant |
| `name` | `VARCHAR(150)` | NO | — | Display name (e.g., "Choose Toppings", "Add Extras", "Select Base") |
| `description` | `TEXT` | YES | `NULL` | Optional clarification shown to customer (e.g., "Pick up to 3") |
| `selection_type` | `VARCHAR(20)` | NO | `'MULTIPLE'` | `SINGLE` = radio (pick one); `MULTIPLE` = checkbox (pick many) |
| `min_selections` | `SMALLINT` | NO | `0` | Minimum addons customer must select. 0 = optional group |
| `max_selections` | `SMALLINT` | YES | `NULL` | Maximum allowed. NULL = unlimited |
| `is_required` | `BOOLEAN` | NO | `false` | Shorthand: true when `min_selections > 0`. Stored for fast frontend validation |
| `display_order` | `SMALLINT` | NO | `0` | Sort order when multiple groups shown for one item |
| `is_active` | `BOOLEAN` | NO | `true` | Soft toggle — deactivating hides group from all linked items |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | Immutable creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | Auto-updated |
| `deleted_at` | `TIMESTAMPTZ` | YES | `NULL` | Soft delete |
| `created_by` | `UUID` | YES | `NULL` | Audit: creator reference |

### JPA Mappings — `MenuItemAddonGroup.java`

```java
@Entity
@Table(name = "menu_item_addon_group")
public class MenuItemAddonGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // ---------------------------------------------------------------
    // @ManyToOne — many addon groups belong to one Restaurant
    // DB column: restaurant_id (NOT NULL)
    // Groups are restaurant-scoped and reused across many items —
    // the item link is managed via MenuItemAddonGroupMap, not here
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // Plain columns
    private String name;
    private String description;
    private String selectionType;
    private Short minSelections;
    private Short maxSelections;
    private Boolean isRequired;
    private Short displayOrder;
    private Boolean isActive;
    private UUID createdBy;
    // ... timestamps
}
```

**Notes:**
- This entity has no `@ManyToOne` to `MenuItem` — the link is managed via the join table `MenuItemAddonGroupMap`. The group is intentionally item-agnostic so it can be reused across many items.

### Indexes

```sql
CREATE INDEX idx_addon_group_restaurant ON menu_item_addon_group (restaurant_id) WHERE deleted_at IS NULL;
```

---

## 5. `menu_item_addon`

**Description:**  
Individual options within an addon group — e.g., "Extra Cheese +₹40", "Jalapeños +₹20", "No Onion (free)".

Zero-price addons (removal requests or free extras) are valid — `price` defaults to `0.00`.

### Columns

| Column | DB Type | Nullable | Default | Description |
|--------|---------|----------|---------|-------------|
| `id` | `UUID` (PK) | NO | `gen_random_uuid()` | Primary key |
| `addon_group_id` | `UUID` | NO | — | FK → `menu_item_addon_group.id`. The group this option belongs to |
| `restaurant_id` | `UUID` | NO | — | FK → `restaurants.id`. Denormalized for restaurant-scoped queries |
| `name` | `VARCHAR(150)` | NO | — | Display label (e.g., "Extra Cheese", "Jalapeños", "No Onion") |
| `description` | `TEXT` | YES | `NULL` | Optional detail (e.g., "Aged cheddar blend") |
| `price` | `DECIMAL(10,2)` | NO | `0.00` | Additional charge. 0 = free addon |
| `is_available` | `BOOLEAN` | NO | `true` | Toggled independently |
| `is_default` | `BOOLEAN` | NO | `false` | If true, pre-selected in the group |
| `display_order` | `SMALLINT` | NO | `0` | Sort order within the group |
| `meta` | `JSONB` | YES | `NULL` | Extensible. E.g., calorie addition, per-addon allergen flags |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | Immutable |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | Auto-updated |
| `deleted_at` | `TIMESTAMPTZ` | YES | `NULL` | Soft delete |

### JPA Mappings — `MenuItemAddon.java`

```java
@Entity
@Table(name = "menu_item_addon")
public class MenuItemAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // ---------------------------------------------------------------
    // @ManyToOne — many addons belong to one AddonGroup
    // DB column: addon_group_id (NOT NULL)
    // Primary structural parent of an addon option
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addon_group_id", nullable = false)
    private MenuItemAddonGroup addonGroup;

    // ---------------------------------------------------------------
    // @ManyToOne — many addons belong to one Restaurant
    // DB column: restaurant_id (NOT NULL)
    // Denormalized: supports fast restaurant-level availability toggling
    // without joining through addon_group
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // Plain columns
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean isAvailable;
    private Boolean isDefault;
    private Short displayOrder;
    // ... timestamps, meta (JSONB)
}
```

### Indexes

```sql
CREATE INDEX idx_addon_group_fk        ON menu_item_addon (addon_group_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_addon_restaurant      ON menu_item_addon (restaurant_id, is_available) WHERE deleted_at IS NULL;
```

---

## 6. `menu_item_addon_group_map`

**Description:**  
Join table linking menu items to addon groups. Enables addon reusability — the same group (e.g., "Extra Toppings") attaches to hundreds of items without duplication.

Carries its own `display_order` so the same group can appear at a different position depending on which item it's attached to. Because of this extra column, it is modeled as a full entity — **not** as a transparent `@ManyToMany`.

### Columns

| Column | DB Type | Nullable | Default | Description |
|--------|---------|----------|---------|-------------|
| `id` | `UUID` (PK) | NO | `gen_random_uuid()` | Primary key |
| `menu_item_id` | `UUID` | NO | — | FK → `menu_item.id` |
| `addon_group_id` | `UUID` | NO | — | FK → `menu_item_addon_group.id` |
| `display_order` | `SMALLINT` | NO | `0` | Per-item override of group display position |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | When this mapping was created |

### JPA Mappings — `MenuItemAddonGroupMap.java`

```java
@Entity
@Table(name = "menu_item_addon_group_map")
public class MenuItemAddonGroupMap {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // ---------------------------------------------------------------
    // @ManyToOne — many map rows reference one MenuItem
    // DB column: menu_item_id (NOT NULL)
    // Side A of the join entity
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    // ---------------------------------------------------------------
    // @ManyToOne — many map rows reference one AddonGroup
    // DB column: addon_group_id (NOT NULL)
    // Side B of the join entity
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addon_group_id", nullable = false)
    private MenuItemAddonGroup addonGroup;

    // Payload column — the reason this is a full entity, not @ManyToMany
    private Short displayOrder;
    // ... created_at
}
```

**Notes:**
- Two `@ManyToOne` fields on a join entity is the standard JPA pattern for many-to-many with extra payload columns.
- Do not collapse this into `@ManyToMany` on `MenuItem` — you would lose `display_order`.
- The unique constraint `(menu_item_id, addon_group_id)` prevents duplicate mappings and must be enforced at the DB level.

### Constraints

```sql
ALTER TABLE menu_item_addon_group_map
    ADD CONSTRAINT uq_item_addon_group UNIQUE (menu_item_id, addon_group_id);
```

### Indexes

```sql
CREATE INDEX idx_addon_map_item        ON menu_item_addon_group_map (menu_item_id);
CREATE INDEX idx_addon_map_group       ON menu_item_addon_group_map (addon_group_id);
```

---

## 7. `menu_item_tag`

**Description:**  
Normalized tag taxonomy replacing the `TEXT[]` tags array on `menu_item`. Tags are scoped to a restaurant (custom tags) or platform-wide (`restaurant_id IS NULL`).

Examples: "Gluten-Free", "Vegan", "Contains Alcohol", "Jain", "Low Calorie", "Seasonal Special".

Having tags as a proper table enforces naming consistency, enables filtered queries, and makes it easy to feed a structured tag vocabulary to an LLM.

### Columns

| Column | DB Type | Nullable | Default | Description |
|--------|---------|----------|---------|-------------|
| `id` | `UUID` (PK) | NO | `gen_random_uuid()` | Primary key |
| `restaurant_id` | `UUID` | YES | `NULL` | NULL = platform-wide tag; populated = restaurant-specific custom tag |
| `name` | `VARCHAR(100)` | NO | — | Tag label as displayed (e.g., "Gluten-Free", "Chef's Pick") |
| `slug` | `VARCHAR(100)` | NO | — | URL/filter-safe identifier (e.g., `gluten-free`). Lowercase, hyphenated |
| `tag_type` | `VARCHAR(50)` | NO | — | `DIETARY` / `ALLERGEN` / `CUISINE` / `BADGE` / `OCCASION` / `CUSTOM` |
| `description` | `TEXT` | YES | `NULL` | What this tag means — fed into LLM context to explain the tag semantically |
| `color_hex` | `VARCHAR(7)` | YES | `NULL` | UI color code (e.g., `#4CAF50` for green on veg tags) |
| `icon_url` | `TEXT` | YES | `NULL` | Optional icon image for tag chips |
| `is_active` | `BOOLEAN` | NO | `true` | Deactivating removes the tag from UI across all linked items |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | Immutable |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | Auto-updated |

### JPA Mappings — `MenuItemTag.java`

```java
@Entity
@Table(name = "menu_item_tag")
public class MenuItemTag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // ---------------------------------------------------------------
    // @ManyToOne — many tags can belong to one Restaurant (optional)
    // DB column: restaurant_id (NULLABLE)
    // NULL = platform-wide tag shared across all restaurants
    // When populated, this tag is custom to that specific restaurant
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = true)
    private Restaurant restaurant;

    // Plain columns
    private String name;
    private String slug;
    private String tagType;
    private String description;
    private String colorHex;
    private String iconUrl;
    private Boolean isActive;
    // ... timestamps
}
```

**Notes:**
- `restaurant` is nullable here — a `NULL` value is intentional and means the tag is platform-level. `nullable = true` on `@JoinColumn` correctly reflects this.
- When `restaurant` is null, JPA stores NULL in the `restaurant_id` column with no issues.

### Constraints

```sql
ALTER TABLE menu_item_tag
    ADD CONSTRAINT uq_tag_slug_restaurant UNIQUE (slug, restaurant_id);
```

### Indexes

```sql
CREATE INDEX idx_tag_restaurant        ON menu_item_tag (restaurant_id) WHERE is_active = true;
CREATE INDEX idx_tag_type              ON menu_item_tag (tag_type) WHERE is_active = true;
```

---

## 8. `menu_item_tag_map`

**Description:**  
Join table linking menu items to tags. Modeled as a full entity (rather than transparent `@ManyToMany`) because it carries `created_at` — useful for auditing when a tag was applied and for ordering tags by recency.

### Columns

| Column | DB Type | Nullable | Default | Description |
|--------|---------|----------|---------|-------------|
| `id` | `UUID` (PK) | NO | `gen_random_uuid()` | Primary key |
| `menu_item_id` | `UUID` | NO | — | FK → `menu_item.id` |
| `tag_id` | `UUID` | NO | — | FK → `menu_item_tag.id` |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | When the tag was applied to this item |

### JPA Mappings — `MenuItemTagMap.java`

```java
@Entity
@Table(name = "menu_item_tag_map")
public class MenuItemTagMap {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // ---------------------------------------------------------------
    // @ManyToOne — many tag-map rows reference one MenuItem
    // DB column: menu_item_id (NOT NULL)
    // Side A of the join entity
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    // ---------------------------------------------------------------
    // @ManyToOne — many tag-map rows reference one MenuItemTag
    // DB column: tag_id (NOT NULL)
    // Side B of the join entity
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private MenuItemTag tag;

    // Payload — the reason this is a full entity, not @ManyToMany
    private OffsetDateTime createdAt;
}
```

### Constraints

```sql
ALTER TABLE menu_item_tag_map
    ADD CONSTRAINT uq_item_tag UNIQUE (menu_item_id, tag_id);
```

### Indexes

```sql
CREATE INDEX idx_tag_map_item          ON menu_item_tag_map (menu_item_id);
CREATE INDEX idx_tag_map_tag           ON menu_item_tag_map (tag_id);
```

---

## 9. `combo`

**Description:**  
A combo (or bundle/deal) is a named collection of menu items sold together at a single price — e.g., "Family Feast", "Lunch Deal", "Weekend BBQ Platter". Combos belong to a menu and restaurant, and support validity windows for time-limited offers.

Combos carry their own `ai_description` so the LLM can reason about bundles as first-class entities, not just a sum of their parts.

### Columns

| Column | DB Type | Nullable | Default | Description |
|--------|---------|----------|---------|-------------|
| `id` | `UUID` (PK) | NO | `gen_random_uuid()` | Primary key |
| `restaurant_id` | `UUID` | NO | — | FK → `restaurants.id` |
| `menu_id` | `UUID` | NO | — | FK → `menu.id`. Combos appear within a specific menu |
| `name` | `VARCHAR(255)` | NO | — | Display name (e.g., "Family Feast", "Lunch Combo #3") |
| `description` | `TEXT` | YES | `NULL` | Customer-facing description of what's included |
| `ai_description` | `TEXT` | YES | `NULL` | LLM-ready description: contents, value proposition, occasion suitability |
| `image_url` | `TEXT` | YES | `NULL` | Hero image for the combo |
| `combo_price` | `DECIMAL(10,2)` | NO | — | Final price of the entire combo |
| `original_price` | `DECIMAL(10,2)` | YES | `NULL` | Sum of individual item prices before bundling — used to display savings |
| `discount_price` | `DECIMAL(10,2)` | YES | `NULL` | Further promotional discount on top of `combo_price` |
| `is_available` | `BOOLEAN` | NO | `true` | Staff availability toggle |
| `valid_from` | `TIMESTAMPTZ` | YES | `NULL` | Start of validity window for time-limited combos |
| `valid_to` | `TIMESTAMPTZ` | YES | `NULL` | End of validity window |
| `available_days` | `TEXT[]` | YES | `NULL` | Days active: `{MON, TUE, WED, THU, FRI, SAT, SUN}`. NULL = always active |
| `max_quantity_per_order` | `SMALLINT` | YES | `NULL` | Per-order limit. NULL = unlimited |
| `badge` | `VARCHAR(100)` | YES | `NULL` | Highlight label: "Best Value", "Limited Time", "Weekend Special" |
| `display_order` | `SMALLINT` | NO | `0` | Sort order within the menu |
| `meta` | `JSONB` | YES | `NULL` | Extensible |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | Immutable |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | Auto-updated |
| `deleted_at` | `TIMESTAMPTZ` | YES | `NULL` | Soft delete |
| `created_by` | `UUID` | YES | `NULL` | Audit: creator reference |
| `updated_by` | `UUID` | YES | `NULL` | Audit: last updater reference |

### JPA Mappings — `Combo.java`

```java
@Entity
@Table(name = "combo")
public class Combo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // ---------------------------------------------------------------
    // @ManyToOne — many combos belong to one Restaurant
    // DB column: restaurant_id (NOT NULL)
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // ---------------------------------------------------------------
    // @ManyToOne — many combos belong to one Menu
    // DB column: menu_id (NOT NULL)
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    // Plain columns
    private String name;
    private String description;
    private String aiDescription;
    private String imageUrl;
    private BigDecimal comboPrice;
    private BigDecimal originalPrice;
    private BigDecimal discountPrice;
    private Boolean isAvailable;
    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;
    private Short maxQuantityPerOrder;
    private String badge;
    private Short displayOrder;
    private UUID createdBy;
    private UUID updatedBy;
    // ... timestamps, meta (JSONB), available_days (TEXT[])
}
```

### Indexes

```sql
CREATE INDEX idx_combo_menu            ON combo (menu_id, is_available) WHERE deleted_at IS NULL;
CREATE INDEX idx_combo_restaurant      ON combo (restaurant_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_combo_validity        ON combo (valid_from, valid_to) WHERE deleted_at IS NULL;
```

---

## 10. `combo_item`

**Description:**  
Each row is one slot in a combo — a specific menu item (and optionally a specific variant) that is part of the bundle. `quantity` allows multiples of the same item (e.g., 2 garlic breads in one combo).

`is_substitutable` enables "build your own combo" — if true, the customer can swap this slot for another item from the same category. Application layer enforces substitution rules.

### Columns

| Column | DB Type | Nullable | Default | Description |
|--------|---------|----------|---------|-------------|
| `id` | `UUID` (PK) | NO | `gen_random_uuid()` | Primary key |
| `combo_id` | `UUID` | NO | — | FK → `combo.id` |
| `menu_item_id` | `UUID` | NO | — | FK → `menu_item.id`. The item in this slot |
| `variant_id` | `UUID` | YES | `NULL` | FK → `menu_item_variant.id`. Specific size/portion. NULL = base item |
| `quantity` | `SMALLINT` | NO | `1` | Units of this item included in the combo |
| `is_substitutable` | `BOOLEAN` | NO | `false` | If true, customer may swap this slot with an item from `substitution_category_id` |
| `substitution_category_id` | `UUID` | YES | `NULL` | FK → `menu_category.id`. Replacement must come from this category when substitutable |
| `display_order` | `SMALLINT` | NO | `0` | Order in which combo items are shown in UI |
| `note` | `TEXT` | YES | `NULL` | Internal note about this slot (e.g., "Always served chilled") |
| `created_at` | `TIMESTAMPTZ` | NO | `now()` | Immutable |
| `updated_at` | `TIMESTAMPTZ` | NO | `now()` | Auto-updated |

### JPA Mappings — `ComboItem.java`

```java
@Entity
@Table(name = "combo_item")
public class ComboItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // ---------------------------------------------------------------
    // @ManyToOne — many combo slots belong to one Combo
    // DB column: combo_id (NOT NULL)
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id", nullable = false)
    private Combo combo;

    // ---------------------------------------------------------------
    // @ManyToOne — many combo slots reference one MenuItem
    // DB column: menu_item_id (NOT NULL)
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    // ---------------------------------------------------------------
    // @ManyToOne — slot may lock to a specific variant
    // DB column: variant_id (NULLABLE — NULL = no variant, use base item)
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = true)
    private MenuItemVariant variant;

    // ---------------------------------------------------------------
    // @ManyToOne — substitution pool category when is_substitutable = true
    // DB column: substitution_category_id (NULLABLE)
    // Only meaningful when is_substitutable = true; service layer must
    // check is_substitutable before using this reference
    // ---------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitution_category_id", nullable = true)
    private MenuCategory substitutionCategory;

    // Plain columns
    private Short quantity;
    private Boolean isSubstitutable;
    private Short displayOrder;
    private String note;
    // ... timestamps
}
```

**Notes:**
- `ComboItem` has the most `@ManyToOne` fields in the entire schema (4 total) — this is expected. It is a rich join entity that references four separate parent tables.
- `variant` and `substitutionCategory` are both nullable. The service layer must guard access: check `isSubstitutable` before reading `substitutionCategory`, and check `variant != null` before using variant pricing.

### Indexes

```sql
CREATE INDEX idx_combo_item_combo      ON combo_item (combo_id);
CREATE INDEX idx_combo_item_menu_item  ON combo_item (menu_item_id);
```

---

## Relationship Summary

| Child Entity | Field | Annotation | Parent Entity | FK Column | Nullable | Reason |
|---|---|---|---|---|---|---|
| `MenuCategory` | `menu` | `@ManyToOne` | `Menu` | `menu_id` | NO | Category belongs to one menu |
| `MenuCategory` | `restaurant` | `@ManyToOne` | `Restaurant` | `restaurant_id` | NO | Denormalized for performance |
| `MenuCategory` | `parentCategory` | `@ManyToOne` (self) | `MenuCategory` | `parent_category_id` | YES | Nested category tree |
| `MenuItem` | `restaurant` | `@ManyToOne` | `Restaurant` | `restaurant_id` | NO | Denormalized for performance |
| `MenuItem` | `menu` | `@ManyToOne` | `Menu` | `menu_id` | NO | Item belongs to one menu |
| `MenuItem` | `category` | `@ManyToOne` | `MenuCategory` | `category_id` | YES | NULL = uncategorized |
| `MenuItemVariant` | `menuItem` | `@ManyToOne` | `MenuItem` | `menu_item_id` | NO | Variant's primary parent |
| `MenuItemVariant` | `restaurant` | `@ManyToOne` | `Restaurant` | `restaurant_id` | NO | Denormalized for performance |
| `MenuItemAddonGroup` | `restaurant` | `@ManyToOne` | `Restaurant` | `restaurant_id` | NO | Group is restaurant-scoped |
| `MenuItemAddon` | `addonGroup` | `@ManyToOne` | `MenuItemAddonGroup` | `addon_group_id` | NO | Addon's primary parent |
| `MenuItemAddon` | `restaurant` | `@ManyToOne` | `Restaurant` | `restaurant_id` | NO | Denormalized for performance |
| `MenuItemAddonGroupMap` | `menuItem` | `@ManyToOne` | `MenuItem` | `menu_item_id` | NO | Join entity side A |
| `MenuItemAddonGroupMap` | `addonGroup` | `@ManyToOne` | `MenuItemAddonGroup` | `addon_group_id` | NO | Join entity side B |
| `MenuItemTag` | `restaurant` | `@ManyToOne` | `Restaurant` | `restaurant_id` | YES | NULL = platform-wide tag |
| `MenuItemTagMap` | `menuItem` | `@ManyToOne` | `MenuItem` | `menu_item_id` | NO | Join entity side A |
| `MenuItemTagMap` | `tag` | `@ManyToOne` | `MenuItemTag` | `tag_id` | NO | Join entity side B |
| `Combo` | `restaurant` | `@ManyToOne` | `Restaurant` | `restaurant_id` | NO | Denormalized for performance |
| `Combo` | `menu` | `@ManyToOne` | `Menu` | `menu_id` | NO | Combo belongs to one menu |
| `ComboItem` | `combo` | `@ManyToOne` | `Combo` | `combo_id` | NO | Slot's primary parent |
| `ComboItem` | `menuItem` | `@ManyToOne` | `MenuItem` | `menu_item_id` | NO | The item in this slot |
| `ComboItem` | `variant` | `@ManyToOne` | `MenuItemVariant` | `variant_id` | YES | NULL = use base item |
| `ComboItem` | `substitutionCategory` | `@ManyToOne` | `MenuCategory` | `substitution_category_id` | YES | Only when substitutable |

> **`@OneToOne` in this schema:** No current entity has a true 1:1 relationship in the menu domain. All child tables support multiple rows per parent. `@OneToOne` is reserved for a future split — e.g., if nutrition data moves to a dedicated `menu_item_nutrition` table, that table would have a `@OneToOne` back to `MenuItem` with `@JoinColumn(name = "menu_item_id")` and a unique constraint on the column.

> **No `@OneToMany` on any entity** — all parent→child navigation is handled via explicit Repository queries with `JOIN FETCH` or `@EntityGraph`. This keeps entities lean and fetch behaviour fully intentional.

---

## Index Strategy

### Why partial indexes everywhere?
All performance-critical indexes use `WHERE deleted_at IS NULL` or `WHERE is_active = true`. This keeps index size small — soft-deleted rows (which accumulate over time) are excluded, so index scans stay fast even at millions of rows.

### Composite index ordering rule
Columns are ordered: **equality filters first, then range/sort columns last.** For example, `(menu_id, category_id, display_order)` — `menu_id` and `category_id` are equality conditions, `display_order` is for ORDER BY.

### GIN indexes for arrays and full-text
- `ingredients TEXT[]` — GIN enables `@>` (contains) operator: "find all items containing peanuts"
- Full-text search on `name + description` — enables keyword search for both the frontend and LLM retrieval pipelines

### Future: Table Partitioning
At 5M+ `menu_item` rows across thousands of restaurants, consider hash partitioning by `restaurant_id`:

```sql
CREATE TABLE menu_item PARTITION BY HASH (restaurant_id);
CREATE TABLE menu_item_p0 PARTITION OF menu_item FOR VALUES WITH (MODULUS 4, REMAINDER 0);
CREATE TABLE menu_item_p1 PARTITION OF menu_item FOR VALUES WITH (MODULUS 4, REMAINDER 1);
-- ... etc
```

---

## Design Decisions & Notes

### 1. `@ManyToOne` only — no `@OneToMany` on parent entities
Parent entities (`Menu`, `Restaurant`, `MenuCategory`, etc.) carry no `@OneToMany` collections. Loading a parent never triggers child collection fetches. All parent→children queries are written explicitly in repositories using `JOIN FETCH` or `@EntityGraph`, giving full control over fetch depth per use case.

### 2. Denormalized `restaurant_id` mapped as `@ManyToOne`
`MenuItem`, `MenuItemVariant`, `MenuItemAddon`, and `Combo` all carry a `@ManyToOne` to `Restaurant` even though it's technically derivable by joining up through `menu → restaurant`. This maps to the denormalized `restaurant_id` column directly and avoids multi-hop joins on the most frequent query path.

### 3. Join entities over `@ManyToMany`
`MenuItemAddonGroupMap` and `MenuItemTagMap` are full `@Entity` classes with two `@ManyToOne` fields each — not transparent `@ManyToMany`. Both join tables carry payload columns (`display_order`, `created_at`) that `@ManyToMany` cannot represent.

### 4. `has_variants` and `has_addons` flags on `MenuItem`
These denormalized booleans let the frontend render a "Customize" button without joining variant or addon tables. Must be kept in sync by the service layer whenever variants/addons are created, deleted, or soft-deleted.

### 5. `ai_description` as a stored column
Rather than computing a rich description at LLM query time, `ai_description` is pre-generated and stored on write. The AI pipeline reads a single column — no joins, no computation, minimal latency.

### 6. `variant_type` and `selection_type` as VARCHAR, not PostgreSQL ENUM
PostgreSQL ENUMs require `ALTER TYPE` to add new values, which acquires an `ACCESS EXCLUSIVE` lock and blocks reads/writes. `VARCHAR` with application-layer validation is safer at scale when enum values evolve.

### 7. `createdBy` / `updatedBy` remain plain `UUID` throughout
These audit fields can reference either `RestaurantStaff.id` or `SuperAdmin.id` depending on who performed the action. A typed `@ManyToOne` to a single entity class cannot model this polymorphism safely — they remain plain `UUID` fields resolved by the service layer when audit context is needed.

### 8. Soft delete via Hibernate `@SQLRestriction`
All 10 tables use `deleted_at TIMESTAMPTZ` uniformly. Apply `@SQLRestriction("deleted_at IS NULL")` on each entity class (Hibernate 6.3+ replacement for deprecated `@Where`) so every repository query automatically filters out soft-deleted rows without explicit `WHERE` clauses in every JPQL/native query.

### 9. Migration tooling
Manage this schema via **Flyway** versioned migrations. The existing `ddl-auto=update` must be disabled before applying these changes in production to prevent Hibernate from making uncontrolled schema modifications.
