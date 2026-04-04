-- =============================================================================
-- AlaCarte — Menu Domain DDL
-- Database  : PostgreSQL 16.11
-- Generated : 2026-04-03
-- -----------------------------------------------------------------------------
-- EXECUTION ORDER (respect FK dependencies):
--   1.  ALTER menu_item          (modify existing table)
--   2.  CREATE menu_category     (new — referenced by menu_item.category_id)
--   3.  CREATE menu_item_variant (new)
--   4.  CREATE menu_item_addon_group     (new)
--   5.  CREATE menu_item_addon           (new)
--   6.  CREATE menu_item_addon_group_map (new)
--   7.  CREATE menu_item_tag             (new)
--   8.  CREATE menu_item_tag_map         (new)
--   9.  CREATE combo                     (new)
--   10. CREATE combo_item                (new)
-- =============================================================================


-- =============================================================================
-- SECTION 1 — ALTER TABLE menu_item
-- Strategy: table has 0 rows — safe to drop/rename columns and add constraints.
-- All changes are grouped into logical steps with comments.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Step 1A: Rename columns whose semantics changed
-- `price` → `base_price` to reflect that variants may override it
-- `category` (plain text) is kept temporarily until Step 1D handles it
-- -----------------------------------------------------------------------------
ALTER TABLE menu_item
    RENAME COLUMN price TO base_price;

-- -----------------------------------------------------------------------------
-- Step 1B: Drop columns replaced by normalized tables
-- `variants`  → replaced by menu_item_variant table
-- `addons`    → replaced by menu_item_addon + menu_item_addon_group tables
-- `tags`      → replaced by menu_item_tag + menu_item_tag_map tables
-- `gallery`   → moved to TEXT[] (simpler, consistent with schema convention)
-- `category`  → replaced by category_id FK (Step 1D), drop the text column now
-- -----------------------------------------------------------------------------
ALTER TABLE menu_item
    DROP COLUMN variants,
    DROP COLUMN addons,
    DROP COLUMN tags,
    DROP COLUMN gallery,
    DROP COLUMN category;

-- -----------------------------------------------------------------------------
-- Step 1C: Add all new columns
-- -----------------------------------------------------------------------------
ALTER TABLE menu_item
    -- Foreign key to new menu_category table (nullable — NULL = uncategorized)
    ADD COLUMN category_id          UUID,

    -- Additional dietary flags missing from original schema
    ADD COLUMN is_vegan             BOOLEAN,
    ADD COLUMN contains_gluten      BOOLEAN,
    ADD COLUMN contains_nuts        BOOLEAN,
    ADD COLUMN contains_dairy       BOOLEAN,

    -- Granular spice scale (1–5); supplements the existing boolean is_spicy
    ADD COLUMN spice_level          SMALLINT
                                        CONSTRAINT chk_spice_level CHECK (spice_level BETWEEN 1 AND 5),

    -- Estimated preparation time in minutes
    ADD COLUMN prep_time_minutes    SMALLINT
                                        CONSTRAINT chk_prep_time CHECK (prep_time_minutes > 0),

    -- Gallery replaces JSONB gallery with a clean PostgreSQL TEXT array
    ADD COLUMN gallery              TEXT[]      DEFAULT '{}',

    -- Structured ingredient list for allergen matching and LLM context
    ADD COLUMN ingredients          TEXT[]      DEFAULT '{}',

    -- Cuisine and course classification for filtering and AI context
    ADD COLUMN cuisine_type         VARCHAR(100),
    ADD COLUMN course_type          VARCHAR(50)
                                        CONSTRAINT chk_course_type CHECK (
                                            course_type IN ('STARTER','MAIN','DESSERT','BEVERAGE','SNACK','BREAD','SOUP')
                                        ),

    -- LLM-ready description — richer than customer-facing description.
    -- Pre-generated on item create/update; read by AI pipeline without joins.
    ADD COLUMN ai_description       TEXT,

    -- Denormalized flags so frontend knows to show "Customize" button
    -- without joining variant or addon tables. Managed by service layer.
    ADD COLUMN has_variants         BOOLEAN     NOT NULL DEFAULT false,
    ADD COLUMN has_addons           BOOLEAN     NOT NULL DEFAULT false,

    -- Audit columns
    ADD COLUMN created_by           UUID,
    ADD COLUMN updated_by           UUID;

-- -----------------------------------------------------------------------------
-- Step 1D: Tighten column constraints that were loose in the original schema
-- -----------------------------------------------------------------------------

-- display_order: change from INTEGER to SMALLINT (max value 32767 is ample)
-- and ensure it is always NOT NULL with a default
ALTER TABLE menu_item
    ALTER COLUMN display_order SET NOT NULL,
    ALTER COLUMN display_order SET DEFAULT 0;

-- is_available: ensure NOT NULL (was nullable in original)
ALTER TABLE menu_item
    ALTER COLUMN is_available SET NOT NULL,
    ALTER COLUMN is_available SET DEFAULT true;

-- created_at: ensure NOT NULL (was nullable in original)
ALTER TABLE menu_item
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN created_at SET DEFAULT now();

-- updated_at: ensure NOT NULL
ALTER TABLE menu_item
    ALTER COLUMN updated_at SET NOT NULL,
    ALTER COLUMN updated_at SET DEFAULT now();

-- has_variants and has_addons: already set NOT NULL in ADD COLUMN above
-- base_price: already NOT NULL from original (renamed from price)

-- -----------------------------------------------------------------------------
-- Step 1E: Add indexes for menu_item
-- All hot-path indexes are partial on deleted_at IS NULL to stay lean.
-- -----------------------------------------------------------------------------

-- Primary menu rendering query: menu page loads all items for a menu,
-- grouped and ordered by category
CREATE INDEX idx_item_menu_category
    ON menu_item (menu_id, category_id, display_order)
    WHERE deleted_at IS NULL;

-- Staff availability dashboard: toggle item on/off per restaurant
CREATE INDEX idx_item_availability
    ON menu_item (restaurant_id, is_available)
    WHERE deleted_at IS NULL;

-- Price filtering: AI recommendations and customer-facing price range filter
CREATE INDEX idx_item_price
    ON menu_item (restaurant_id, base_price)
    WHERE deleted_at IS NULL;

-- Dietary filtering: veg/vegan filter used on every menu load
CREATE INDEX idx_item_dietary
    ON menu_item (restaurant_id, is_veg, is_vegan)
    WHERE deleted_at IS NULL;

-- Full-text search: keyword search across item name + description.
-- Used by both the frontend search bar and the LLM retrieval pipeline.
CREATE INDEX idx_item_fts
    ON menu_item
    USING GIN (to_tsvector('english', name || ' ' || coalesce(description, '')));

-- Ingredients array: allergen containment queries — "find all items with peanuts"
CREATE INDEX idx_item_ingredients
    ON menu_item
    USING GIN (ingredients);


-- =============================================================================
-- SECTION 2 — CREATE TABLE menu_category
-- Depends on: restaurants (existing), menu (existing)
-- Referenced by: menu_item.category_id, combo_item.substitution_category_id
-- =============================================================================

CREATE TABLE menu_category
(
    id                 UUID          NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,

    -- FK to existing menu table (logical, not enforced at DB level per project convention)
    menu_id            UUID          NOT NULL,

    -- Denormalized FK to existing restaurants table.
    -- Avoids joining through menu on every restaurant-scoped category query.
    restaurant_id      UUID          NOT NULL,

    -- Self-referencing FK for nested category tree.
    -- NULL = top-level category (e.g., "Drinks").
    -- Populated = sub-category (e.g., "Hot Drinks" under "Drinks").
    -- Recommended max nesting depth: 3 levels (enforced by application logic).
    parent_category_id UUID,

    -- Display name shown on menu UI (e.g., "Starters", "Pizza", "Hot Drinks")
    name               VARCHAR(150)  NOT NULL,

    -- Human-readable description. Doubles as LLM context — fed into AI prompts
    -- to give the model semantic understanding of the category.
    description        TEXT,

    -- Banner / thumbnail image URL for this category
    image_url          TEXT,

    -- Controls sort order within the same menu. Lower value = shown first.
    display_order      SMALLINT      NOT NULL DEFAULT 0,

    -- Soft toggle: false hides the category from UI without deleting it.
    -- Different from deleted_at — is_active = false means intentionally hidden;
    -- deleted_at populated means permanently removed.
    is_active          BOOLEAN       NOT NULL DEFAULT true,

    -- Extensible JSONB for future attributes: color codes, icons, UI hints, etc.
    meta               JSONB                  DEFAULT '{}'::jsonb,

    -- Immutable creation timestamp
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),

    -- Auto-updated on every modification (trigger or application layer)
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),

    -- Soft delete timestamp. NULL = active record.
    -- Consistent with deleted_at convention used across all menu domain tables.
    deleted_at         TIMESTAMPTZ,

    -- Audit: UUID of the staff member or super_admin who created this record.
    -- Kept as plain UUID (not a typed FK) because it can reference either
    -- restaurant_staff.id or super_admin.id depending on who acted.
    created_by         UUID,

    -- Audit: UUID of last updater
    updated_by         UUID
);

-- Index: fetch all active categories for a menu (menu rendering hot path)
CREATE INDEX idx_category_menu
    ON menu_category (menu_id)
    WHERE deleted_at IS NULL;

-- Index: fetch all categories for a restaurant (staff management dashboard)
CREATE INDEX idx_category_restaurant
    ON menu_category (restaurant_id)
    WHERE deleted_at IS NULL;

-- Index: fetch child categories for a given parent (tree traversal)
CREATE INDEX idx_category_parent
    ON menu_category (parent_category_id)
    WHERE parent_category_id IS NOT NULL;

-- Index: ordered category listing within a menu
CREATE INDEX idx_category_display
    ON menu_category (menu_id, display_order);


-- =============================================================================
-- SECTION 3 — CREATE TABLE menu_item_variant
-- Depends on: menu_item (existing + altered), restaurants (existing)
-- Replaces: menu_item.variants JSONB column
-- =============================================================================

CREATE TABLE menu_item_variant
(
    id             UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,

    -- FK to the parent menu item. Many variants belong to one item.
    menu_item_id   UUID           NOT NULL,

    -- Denormalized FK to restaurants. Enables restaurant-scoped availability
    -- queries (e.g., "toggle all Medium variants for restaurant X") without
    -- joining through menu_item.
    restaurant_id  UUID           NOT NULL,

    -- Classification of the variation dimension.
    -- Values: SIZE | PORTION | FLAVOR | CRUST | TEMPERATURE | CUSTOM
    -- Stored as VARCHAR (not ENUM) to avoid ALTER TYPE lock on future additions.
    variant_type   VARCHAR(50)    NOT NULL,

    -- Display label shown to customer when selecting a variant.
    -- e.g., "Small", "Medium", "Full", "Half", "Thin Crust", "Iced"
    name           VARCHAR(150)   NOT NULL,

    -- Optional extra detail shown alongside the variant name.
    -- e.g., "Serves 1–2 people", "250ml"
    description    TEXT,

    -- Price for this specific variant. When a customer selects a variant,
    -- this value overrides menu_item.base_price for billing purposes.
    price          NUMERIC(10, 2) NOT NULL,

    -- Optional promotional price. Applied instead of price when not NULL.
    discount_price NUMERIC(10, 2),

    -- Staff can toggle individual variant availability independently.
    -- e.g., "Full portion sold out tonight" without hiding Small/Half.
    is_available   BOOLEAN        NOT NULL DEFAULT true,

    -- If true, this variant is pre-selected in the UI when the item is shown.
    -- Business rule: only one variant per menu_item_id should be true.
    -- Enforced by service layer (clear flag on siblings before setting here).
    is_default     BOOLEAN        NOT NULL DEFAULT false,

    -- Sort order within this item's variant list. Lower = shown first.
    display_order  SMALLINT       NOT NULL DEFAULT 0,

    -- Extensible JSONB for future variant metadata.
    -- e.g., {"weight_grams": 350, "serving_ml": 500}
    meta           JSONB                   DEFAULT '{}'::jsonb,

    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMPTZ                                -- soft delete
);

-- Index: load all variants for a specific item (item detail / ordering page)
CREATE INDEX idx_variant_item
    ON menu_item_variant (menu_item_id)
    WHERE deleted_at IS NULL;

-- Index: restaurant-scoped availability toggle (staff dashboard)
CREATE INDEX idx_variant_restaurant
    ON menu_item_variant (restaurant_id, is_available)
    WHERE deleted_at IS NULL;


-- =============================================================================
-- SECTION 4 — CREATE TABLE menu_item_addon_group
-- Depends on: restaurants (existing)
-- Replaces: menu_item.addons JSONB (group-level metadata)
-- Note: Groups are restaurant-scoped and REUSED across items.
--       The item ↔ group link is in menu_item_addon_group_map (Section 6).
-- =============================================================================

CREATE TABLE menu_item_addon_group
(
    id              UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,

    -- Groups are defined at the restaurant level so they can be reused
    -- across many items without duplication.
    -- e.g., "Choose Toppings" defined once, linked to 50 pizza variants.
    restaurant_id   UUID         NOT NULL,

    -- Display name shown as a section header on the customization screen.
    -- e.g., "Choose Toppings", "Select Sauce", "Add Extras"
    name            VARCHAR(150) NOT NULL,

    -- Optional subtitle shown to customer.
    -- e.g., "Pick up to 3", "Required — choose one"
    description     TEXT,

    -- Controls how many options the customer can select from this group.
    -- SINGLE  = radio-button style — customer picks exactly one option.
    -- MULTIPLE = checkbox style — customer picks one or more options.
    -- Stored as VARCHAR (not ENUM) — see design note on ENUM avoidance.
    selection_type  VARCHAR(20)  NOT NULL DEFAULT 'MULTIPLE'
                                     CONSTRAINT chk_selection_type
                                         CHECK (selection_type IN ('SINGLE', 'MULTIPLE')),

    -- Minimum number of options the customer must select from this group.
    -- 0 = the entire group is optional.
    min_selections  SMALLINT     NOT NULL DEFAULT 0
                                     CONSTRAINT chk_min_selections CHECK (min_selections >= 0),

    -- Maximum number of options allowed. NULL = no upper limit.
    max_selections  SMALLINT
                                     CONSTRAINT chk_max_selections CHECK (max_selections > 0),

    -- Derived convenience flag: true when min_selections > 0.
    -- Stored explicitly so the frontend can validate required groups
    -- without computing from min_selections on every render.
    is_required     BOOLEAN      NOT NULL DEFAULT false,

    -- Sort order when multiple groups appear on one item's customization screen.
    display_order   SMALLINT     NOT NULL DEFAULT 0,

    -- Soft toggle: deactivating a group hides it from ALL linked items instantly.
    -- More efficient than updating every menu_item_addon_group_map row.
    is_active       BOOLEAN      NOT NULL DEFAULT true,

    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ,                            -- soft delete

    -- Audit
    created_by      UUID
);

-- Index: list all addon groups for a restaurant (staff management)
CREATE INDEX idx_addon_group_restaurant
    ON menu_item_addon_group (restaurant_id)
    WHERE deleted_at IS NULL;


-- =============================================================================
-- SECTION 5 — CREATE TABLE menu_item_addon
-- Depends on: menu_item_addon_group (Section 4), restaurants (existing)
-- Replaces: menu_item.addons JSONB (individual option entries)
-- =============================================================================

CREATE TABLE menu_item_addon
(
    id             UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,

    -- FK to the parent group. Many addons belong to one group.
    addon_group_id UUID           NOT NULL,

    -- Denormalized FK to restaurants. Allows toggling all addons for a
    -- restaurant (e.g., ingredient shortage) without joining through the group.
    restaurant_id  UUID           NOT NULL,

    -- Display label for this option.
    -- e.g., "Extra Cheese", "Jalapeños", "No Onion", "Garlic Butter"
    name           VARCHAR(150)   NOT NULL,

    -- Optional detail text shown below the addon name.
    -- e.g., "Aged cheddar blend", "Freshly sliced"
    description    TEXT,

    -- Additional charge applied when this option is selected.
    -- 0.00 = free addon (e.g., removal requests like "No Onion").
    price          NUMERIC(10, 2) NOT NULL DEFAULT 0.00
                                      CONSTRAINT chk_addon_price CHECK (price >= 0),

    -- Staff can toggle individual addon availability.
    -- e.g., "Jalapeños out of stock" — without hiding other options.
    is_available   BOOLEAN        NOT NULL DEFAULT true,

    -- If true, this option is pre-selected when the group renders.
    -- Business rule enforced by service layer (same as variant is_default).
    is_default     BOOLEAN        NOT NULL DEFAULT false,

    -- Sort order within the group. Lower = shown first.
    display_order  SMALLINT       NOT NULL DEFAULT 0,

    -- Extensible JSONB for per-addon metadata.
    -- e.g., {"calorie_addition": 80, "contains_nuts": true}
    meta           JSONB                   DEFAULT '{}'::jsonb,

    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    deleted_at     TIMESTAMPTZ                               -- soft delete
);

-- Index: load all addons for a group (customization screen rendering)
CREATE INDEX idx_addon_group_fk
    ON menu_item_addon (addon_group_id)
    WHERE deleted_at IS NULL;

-- Index: restaurant-scoped availability management (staff dashboard)
CREATE INDEX idx_addon_restaurant
    ON menu_item_addon (restaurant_id, is_available)
    WHERE deleted_at IS NULL;


-- =============================================================================
-- SECTION 6 — CREATE TABLE menu_item_addon_group_map
-- Depends on: menu_item (existing + altered), menu_item_addon_group (Section 4)
-- Purpose: Links items to addon groups (many-to-many with payload).
--          Modeled as a full table (not a transparent join) because it carries
--          display_order — the same group can appear at a different position
--          depending on which item it is attached to.
-- =============================================================================

CREATE TABLE menu_item_addon_group_map
(
    id             UUID     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,

    -- Side A: the menu item that has this addon group
    menu_item_id   UUID     NOT NULL,

    -- Side B: the addon group attached to this item
    addon_group_id UUID     NOT NULL,

    -- Per-item override of the group's display position.
    -- e.g., "Extra Toppings" appears at position 1 on pizzas
    --        but at position 3 on pasta items.
    display_order  SMALLINT NOT NULL DEFAULT 0,

    -- Audit: when this item ↔ group link was created
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Prevent the same group from being linked to the same item twice
    CONSTRAINT uq_item_addon_group UNIQUE (menu_item_id, addon_group_id)
);

-- Index: given a menu item, fetch all its linked addon groups (ordering flow)
CREATE INDEX idx_addon_map_item
    ON menu_item_addon_group_map (menu_item_id);

-- Index: given an addon group, find all items it is attached to
--        (used when deactivating a group — show affected items to staff)
CREATE INDEX idx_addon_map_group
    ON menu_item_addon_group_map (addon_group_id);


-- =============================================================================
-- SECTION 7 — CREATE TABLE menu_item_tag
-- Depends on: restaurants (existing)
-- Replaces: menu_item.tags TEXT[] column
-- Note: restaurant_id IS NULL = platform-wide tag (shared across all restaurants)
--       restaurant_id populated  = restaurant-specific custom tag
-- =============================================================================

CREATE TABLE menu_item_tag
(
    id            UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,

    -- NULL means this tag is platform-level (e.g., "Gluten-Free", "Vegan").
    -- Populated means this tag was created by a specific restaurant
    -- (e.g., "House Special", "Chef's Seasonal Pick").
    restaurant_id UUID,

    -- Display label exactly as shown on the menu UI.
    -- e.g., "Gluten-Free", "Vegan", "Contains Alcohol", "Jain", "Low Calorie"
    name          VARCHAR(100) NOT NULL,

    -- URL and filter-safe version of the name.
    -- e.g., "gluten-free", "chefs-pick", "contains-alcohol"
    -- Used as the filter key in API queries and LLM tag references.
    slug          VARCHAR(100) NOT NULL,

    -- Semantic classification of the tag — used to group tags in the UI
    -- and to build structured LLM context (dietary tags vs allergen tags, etc.)
    -- Values: DIETARY | ALLERGEN | CUISINE | BADGE | OCCASION | CUSTOM
    tag_type      VARCHAR(50)  NOT NULL
                                   CONSTRAINT chk_tag_type
                                       CHECK (tag_type IN ('DIETARY','ALLERGEN','CUISINE','BADGE','OCCASION','CUSTOM')),

    -- What this tag means in plain language.
    -- Fed directly into LLM prompts so the model understands tag semantics
    -- without needing a lookup table at inference time.
    description   TEXT,

    -- Optional hex color code for the tag chip in the UI.
    -- e.g., "#4CAF50" for green (vegetarian), "#F44336" for red (spicy)
    color_hex     VARCHAR(7)
                      CONSTRAINT chk_color_hex CHECK (color_hex ~ '^#[0-9A-Fa-f]{6}$'),

    -- Optional icon image URL displayed alongside the tag label
    icon_url      TEXT,

    -- Deactivating a tag hides it from the UI across all linked items instantly.
    -- The tag_map rows are NOT deleted — reactivating restores all links.
    is_active     BOOLEAN      NOT NULL DEFAULT true,

    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

    -- Slug must be unique within a restaurant scope.
    -- NULL restaurant_id (platform tags) also covered — two platform tags
    -- cannot share the same slug.
    CONSTRAINT uq_tag_slug_restaurant UNIQUE (slug, restaurant_id)
);

-- Index: list all active tags for a restaurant (tag management screen)
CREATE INDEX idx_tag_restaurant
    ON menu_item_tag (restaurant_id)
    WHERE is_active = true;

-- Index: filter tags by type (e.g., show only ALLERGEN tags in allergen filter)
CREATE INDEX idx_tag_type
    ON menu_item_tag (tag_type)
    WHERE is_active = true;


-- =============================================================================
-- SECTION 8 — CREATE TABLE menu_item_tag_map
-- Depends on: menu_item (existing + altered), menu_item_tag (Section 7)
-- Purpose: Links items to tags (many-to-many with payload).
--          Modeled as a full table because it carries created_at —
--          useful for auditing when a tag was applied to an item.
-- =============================================================================

CREATE TABLE menu_item_tag_map
(
    id           UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,

    -- Side A: the menu item being tagged
    menu_item_id UUID        NOT NULL,

    -- Side B: the tag being applied
    tag_id       UUID        NOT NULL,

    -- Timestamp when this tag was applied to this item.
    -- Useful for "recently tagged" ordering and audit logs.
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- Prevent the same tag from being applied to the same item twice
    CONSTRAINT uq_item_tag UNIQUE (menu_item_id, tag_id)
);

-- Index: given an item, load all its tags (item detail rendering)
CREATE INDEX idx_tag_map_item
    ON menu_item_tag_map (menu_item_id);

-- Index: given a tag, find all items it is applied to
--        (used for tag-based filtering queries)
CREATE INDEX idx_tag_map_tag
    ON menu_item_tag_map (tag_id);


-- =============================================================================
-- SECTION 9 — CREATE TABLE combo
-- Depends on: restaurants (existing), menu (existing)
-- Purpose: Bundle / deal entity. A combo is a named collection of menu items
--          sold together at a single price. Supports time-limited validity.
-- =============================================================================

CREATE TABLE combo
(
    id                    UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,

    -- FK to the restaurant that owns this combo
    restaurant_id         UUID           NOT NULL,

    -- FK to the menu this combo appears in.
    -- Combos are menu-specific — a "Lunch Deal" lives on the Lunch menu.
    menu_id               UUID           NOT NULL,

    -- Display name shown on the menu UI.
    -- e.g., "Family Feast", "Lunch Combo #3", "Weekend BBQ Platter"
    name                  VARCHAR(255)   NOT NULL,

    -- Customer-facing description of what's included in the combo.
    description           TEXT,

    -- LLM-ready description: contents, value proposition, occasion suitability.
    -- Pre-generated on combo create/update; read by AI pipeline without joins.
    ai_description        TEXT,

    -- Hero image URL shown on the combo card
    image_url             TEXT,

    -- The final price charged for the entire combo bundle
    combo_price           NUMERIC(10, 2) NOT NULL
                                             CONSTRAINT chk_combo_price CHECK (combo_price >= 0),

    -- Sum of individual item prices before bundling.
    -- Stored (not computed) so "You save ₹120" can be displayed without
    -- fetching all combo_item rows at render time.
    original_price        NUMERIC(10, 2)
                                             CONSTRAINT chk_original_price CHECK (original_price >= 0),

    -- Further promotional discount applied on top of combo_price.
    -- Effective price = discount_price when NOT NULL, else combo_price.
    discount_price        NUMERIC(10, 2)
                                             CONSTRAINT chk_combo_discount CHECK (discount_price >= 0),

    -- Staff availability toggle. Separate from soft-delete.
    is_available          BOOLEAN        NOT NULL DEFAULT true,

    -- Optional time window for limited-time offers.
    -- e.g., Lunch Deal active only between 12:00–15:00.
    -- Application layer enforces the window at order time.
    valid_from            TIMESTAMPTZ,
    valid_to              TIMESTAMPTZ,

    -- Days of week when this combo is active.
    -- e.g., '{SAT,SUN}' for weekend-only deals.
    -- NULL = active every day.
    -- Values: MON | TUE | WED | THU | FRI | SAT | SUN
    available_days        TEXT[],

    -- Maximum number of this combo a single order can contain.
    -- NULL = no limit.
    max_quantity_per_order SMALLINT
                               CONSTRAINT chk_max_qty CHECK (max_quantity_per_order > 0),

    -- Highlight label displayed on the combo card badge.
    -- e.g., "Best Value", "Limited Time", "Weekend Special"
    badge                 VARCHAR(100),

    -- Sort order within the menu. Lower = shown first.
    display_order         SMALLINT       NOT NULL DEFAULT 0,

    -- Extensible JSONB for future or restaurant-specific combo attributes
    meta                  JSONB                   DEFAULT '{}'::jsonb,

    created_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    deleted_at            TIMESTAMPTZ,                        -- soft delete

    -- Audit
    created_by            UUID,
    updated_by            UUID
);

-- Index: load all available combos for a menu (menu rendering)
CREATE INDEX idx_combo_menu
    ON combo (menu_id, is_available)
    WHERE deleted_at IS NULL;

-- Index: restaurant-scoped combo management (staff dashboard)
CREATE INDEX idx_combo_restaurant
    ON combo (restaurant_id)
    WHERE deleted_at IS NULL;

-- Index: validity window queries — find all combos active right now
CREATE INDEX idx_combo_validity
    ON combo (valid_from, valid_to)
    WHERE deleted_at IS NULL;


-- =============================================================================
-- SECTION 10 — CREATE TABLE combo_item
-- Depends on: combo (Section 9), menu_item (existing + altered),
--             menu_item_variant (Section 3), menu_category (Section 2)
-- Purpose: Each row is one "slot" in a combo.
--          Supports optional variant locking and item substitution.
-- =============================================================================

CREATE TABLE combo_item
(
    id                       UUID     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,

    -- FK to the parent combo. Many slots belong to one combo.
    combo_id                 UUID     NOT NULL,

    -- The menu item filling this slot.
    -- e.g., "Pepsi" in a "Burger + Pepsi" combo
    menu_item_id             UUID     NOT NULL,

    -- Optional: lock this slot to a specific variant of the item.
    -- e.g., the combo always includes "Medium Pepsi", not any size.
    -- NULL = use the item's base price / default variant.
    variant_id               UUID,

    -- How many units of this item are included in the combo.
    -- e.g., quantity = 2 means "2 Garlic Breads included"
    quantity                 SMALLINT NOT NULL DEFAULT 1
                                          CONSTRAINT chk_combo_item_qty CHECK (quantity > 0),

    -- If true, the customer may swap this item for another item
    -- from the category defined in substitution_category_id.
    -- e.g., "Swap the Pepsi for any other beverage"
    is_substitutable         BOOLEAN  NOT NULL DEFAULT false,

    -- When is_substitutable = true, the replacement item must come
    -- from this category. NULL when is_substitutable = false.
    substitution_category_id UUID,

    -- Order in which this slot is displayed on the combo detail screen.
    display_order            SMALLINT NOT NULL DEFAULT 0,

    -- Internal note visible only to staff about this slot.
    -- e.g., "Always served chilled", "Ask kitchen to plate separately"
    note                     TEXT,

    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Index: given a combo, load all its item slots (combo detail / ordering)
CREATE INDEX idx_combo_item_combo
    ON combo_item (combo_id);

-- Index: given a menu item, find all combos it appears in
--        (used when soft-deleting an item — warn staff about affected combos)
CREATE INDEX idx_combo_item_menu_item
    ON combo_item (menu_item_id);


-- =============================================================================
-- END OF FILE
-- =============================================================================
