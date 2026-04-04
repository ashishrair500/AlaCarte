# MenuItem Refactor Plan

## Current state

- [`MenuItem.java`](/Users/ashish/IdeaProjects/MenuAgent/src/main/java/com/alaCarte/entity/MenuItem.java) has been updated to the new schema shape.
- [`MenuItemService.java`](/Users/ashish/IdeaProjects/MenuAgent/src/main/java/com/alaCarte/service/MenuItemService.java), [`MenuItemRepository.java`](/Users/ashish/IdeaProjects/MenuAgent/src/main/java/com/alaCarte/repository/MenuItemRepository.java), and [`MenuItemManagementController.java`](/Users/ashish/IdeaProjects/MenuAgent/src/main/java/com/alaCarte/restaurantAdmin/controller/MenuItemManagementController.java) still target the old flat model.
- This means the codebase is intentionally in a temporary transition state.

## Repository updates

1. Replace raw UUID-derived query methods with relationship-aware queries.
2. Rename repository methods and JPQL/native queries from old fields to new fields.
3. Remove repository logic that depends on deleted timestamps, JSON variants, JSON addons, and array tags.
4. Update category-based queries to use `category.id` or `category.name`, depending on the final API contract.
5. Update menu and restaurant filters to navigate through `menu`, `restaurant`, and `category` relations instead of `menuId`, `restaurantId`, and string `category`.
6. Replace `price` references with `basePrice`.
7. Replace `deletedAt` filters with `deleted = 0` semantics, while respecting `@SQLRestriction`.
8. Rework or delete these old methods because their backing columns no longer exist:
   - `findByMenuIdAndCategory`
   - `countByMenuIdAndCategory`
   - `findByMenuIdAndTag`
   - `findItemsWithVariants`
   - `findItemsWithAddons`
   - `findDistinctCategoriesByMenuId`
9. Add new repository methods for likely near-term needs:
   - by `menu.id`
   - by `restaurant.id`
   - by `category.id`
   - by `hasVariants`
   - by `hasAddons`
   - by `basePrice` range

## Service updates

1. Replace all old getter/setter usage:
   - `getMenuId()` -> `getMenu().getId()` or `getMenu()`
   - `getRestaurantId()` -> `getRestaurant().getId()` or `getRestaurant()`
   - `getPrice()` -> `getBasePrice()`
   - `getDeletedAt()` / `setDeletedAt()` -> `getDeleted()` / `setDeleted()`
2. Remove service logic that directly manipulates removed fields:
   - `variants`
   - `addons`
   - `tags`
   - string `category`
3. Update duplicate-name checks to use the new menu relationship.
4. Update display-order logic to use `Short` instead of `Integer`.
5. Replace soft-delete behavior:
   - old: timestamp set on `deletedAt`
   - new: set `deleted = 1` and optionally `isAvailable = false`
6. Replace restore behavior:
   - old: `deletedAt = null`
   - new: `deleted = 0`
7. Review all search methods and either:
   - map them to new relational fields, or
   - temporarily remove them if the API contract is changing.
8. Decide whether category-based service methods should accept category UUIDs instead of strings.

## Controller updates

1. Update request-to-entity mapping because `restaurantId`, `menuId`, `category`, `price`, `variants`, `addons`, and `tags` no longer map directly to `MenuItem`.
2. Decide controller strategy for relations:
   - fetch `Restaurant`, `Menu`, and `MenuCategory` in the service layer from incoming IDs, or
   - redesign request DTOs first and then map richer objects.
3. Update response mapping because current `MenuItemResponse` still expects:
   - `restaurantId`
   - `menuId`
   - string `category`
   - `price`
   - `variants`
   - `addons`
   - `tags`
   - `deletedAt`
4. Replace response fields with the new entity fields:
   - `basePrice`
   - `aiDescription`
   - `categoryId` or category summary
   - `hasVariants`
   - `hasAddons`
   - dietary/allergen fields
   - `deleted`
5. Remove direct controller dependence on fields that will now live in separate tables.

## Important dependency note

- Fully updating the controller will almost certainly require updating [`MenuItemRequest.java`](/Users/ashish/IdeaProjects/MenuAgent/src/main/java/com/alaCarte/dto/MenuItemRequest.java) and [`MenuItemResponse.java`](/Users/ashish/IdeaProjects/MenuAgent/src/main/java/com/alaCarte/dto/MenuItemResponse.java), even though they are outside the three requested files.
- If we do not change the DTOs, the controller cannot accurately represent the new schema.

## Recommended execution order

1. Update [`MenuItemRepository.java`](/Users/ashish/IdeaProjects/MenuAgent/src/main/java/com/alaCarte/repository/MenuItemRepository.java)
2. Update [`MenuItemService.java`](/Users/ashish/IdeaProjects/MenuAgent/src/main/java/com/alaCarte/service/MenuItemService.java)
3. Update DTOs to match the new contract
4. Update [`MenuItemManagementController.java`](/Users/ashish/IdeaProjects/MenuAgent/src/main/java/com/alaCarte/restaurantAdmin/controller/MenuItemManagementController.java)
5. Compile and fix remaining call sites
