package com.alaCarte.service;

import com.alaCarte.entity.MenuItem;
import com.alaCarte.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service class for MenuItem operations
 * Provides business logic for managing menu items
 */
@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    // Constructor injection (recommended over @Autowired field injection)
    public MenuItemService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    /**
     * Create a new menu item
     */
    @Transactional
    public MenuItem createMenuItem(MenuItem menuItem) {
        // Check if item with same name already exists in this menu
        UUID menuId = requireMenuId(menuItem);
        if (menuItemRepository.existsByMenuIdAndName(menuId, menuItem.getName())) {
            throw new RuntimeException("Menu item with name '" + menuItem.getName() + "' already exists in this menu");
        }
        
        // Auto-assign display order if not provided
        if (menuItem.getDisplayOrder() == null || menuItem.getDisplayOrder() == 0) {
            Short maxOrder = menuItemRepository.getMaxDisplayOrder(menuId);
            menuItem.setDisplayOrder((short) (maxOrder + 1));
        }
        
        return menuItemRepository.save(menuItem);
    }

    /**
     * Create multiple menu items in a single transaction.
     */
    @Transactional
    public List<MenuItem> createMenuItemsBulk(List<MenuItem> menuItems) {
        if (menuItems == null || menuItems.isEmpty()) {
            throw new RuntimeException("At least one menu item is required for bulk upload");
        }

        UUID menuId = requireMenuId(menuItems.get(0));
        UUID restaurantId = requireRestaurantId(menuItems.get(0));
        Set<String> itemNamesInRequest = new HashSet<>();

        for (MenuItem menuItem : menuItems) {
            if (!menuId.equals(requireMenuId(menuItem))) {
                throw new RuntimeException("All menu items in a bulk upload must belong to the same menu");
            }

            if (!restaurantId.equals(requireRestaurantId(menuItem))) {
                throw new RuntimeException("All menu items in a bulk upload must belong to the same restaurant");
            }

            String normalizedName = menuItem.getName().trim().toLowerCase();
            if (!itemNamesInRequest.add(normalizedName)) {
                throw new RuntimeException("Duplicate menu item name '" + menuItem.getName() + "' found in bulk upload request");
            }

            if (menuItemRepository.existsByMenuIdAndName(menuId, menuItem.getName())) {
                throw new RuntimeException("Menu item with name '" + menuItem.getName() + "' already exists in this menu");
            }
        }

        short nextDisplayOrder = menuItemRepository.getMaxDisplayOrder(menuId);
        for (MenuItem menuItem : menuItems) {
            if (menuItem.getDisplayOrder() == null || menuItem.getDisplayOrder() == 0) {
                nextDisplayOrder++;
                menuItem.setDisplayOrder(nextDisplayOrder);
            }
        }

        return menuItemRepository.saveAll(menuItems);
    }

    /**
     * Get all menu items
     */
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    /**
     * Get menu item by ID
     */
    public Optional<MenuItem> getMenuItemById(UUID id) {
        return menuItemRepository.findById(id);
    }

    /**
     * Get items by restaurant
     */
    public List<MenuItem> getMenuItemsByRestaurant(UUID restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    /**
     * Get items by menu
     */
    public List<MenuItem> getMenuItemsByMenu(UUID menuId) {
        return menuItemRepository.findByMenuId(menuId);
    }

    /**
     * Get items by restaurant and menu
     */
    public List<MenuItem> getMenuItemsByRestaurantAndMenu(UUID restaurantId, UUID menuId) {
        return menuItemRepository.findByRestaurantIdAndMenuId(restaurantId, menuId);
    }

    /**
     * Get available items by menu
     */
    public List<MenuItem> getAvailableItemsByMenu(UUID menuId) {
        return menuItemRepository.findByMenuIdAndIsAvailableTrue(menuId);
    }

    /**
     * Get available items by restaurant
     */
    public List<MenuItem> getAvailableItemsByRestaurant(UUID restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);
    }

    /**
     * Get items by category
     */
    public List<MenuItem> getMenuItemsByCategory(UUID menuId, String category) {
        return menuItemRepository.findByMenuIdAndCategory(menuId, category);
    }

    /**
     * Get vegetarian items
     */
    public List<MenuItem> getVegetarianItems(UUID menuId) {
        return menuItemRepository.findByMenuIdAndIsVegTrue(menuId);
    }

    /**
     * Get non-vegetarian items
     */
    public List<MenuItem> getNonVegetarianItems(UUID menuId) {
        return menuItemRepository.findByMenuIdAndIsVegFalse(menuId);
    }

    /**
     * Get spicy items
     */
    public List<MenuItem> getSpicyItems(UUID menuId) {
        return menuItemRepository.findByMenuIdAndIsSpicyTrue(menuId);
    }

    /**
     * Get egg-free items
     */
    public List<MenuItem> getEggFreeItems(UUID menuId) {
        return menuItemRepository.findByMenuIdAndContainsEggFalse(menuId);
    }

    /**
     * Search menu items by name (case-insensitive)
     */
    public List<MenuItem> searchMenuItemsByName(String searchTerm) {
        return menuItemRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    /**
     * Search menu items by name in specific menu
     */
    public List<MenuItem> searchMenuItemsByNameInMenu(UUID menuId, String searchTerm) {
        return menuItemRepository.findByMenuIdAndNameContainingIgnoreCase(menuId, searchTerm);
    }

    /**
     * Get items within price range
     */
    public List<MenuItem> getMenuItemsByPriceRange(UUID menuId, BigDecimal minPrice, BigDecimal maxPrice) {
        return menuItemRepository.findByMenuIdAndPriceBetween(menuId, minPrice, maxPrice);
    }

    /**
     * Get discounted items
     */
    public List<MenuItem> getDiscountedItems(UUID menuId) {
        return menuItemRepository.findDiscountedItems(menuId);
    }

    /**
     * Get items by badge
     */
    public List<MenuItem> getItemsByBadge(UUID menuId, String badge) {
        return menuItemRepository.findByMenuIdAndBadge(menuId, badge);
    }

    /**
     * Get items ordered by display order
     */
    public List<MenuItem> getItemsOrderedByDisplayOrder(UUID menuId) {
        return menuItemRepository.findByMenuIdOrderByDisplayOrderAsc(menuId);
    }

    /**
     * Get available items ordered by display order
     */
    public List<MenuItem> getAvailableItemsOrdered(UUID menuId) {
        return menuItemRepository.findAvailableItemsOrdered(menuId);
    }

    /**
     * Get items by category ordered by display order
     */
    public List<MenuItem> getItemsByCategoryOrdered(UUID menuId, String category) {
        return menuItemRepository.findByCategoryOrdered(menuId, category);
    }

    /**
     * Get soft-deleted items
     */
    public List<MenuItem> getSoftDeletedItems(UUID menuId) {
        return menuItemRepository.findSoftDeleted(menuId);
    }

    /**
     * Get not soft-deleted items
     */
    public List<MenuItem> getNotSoftDeletedItems(UUID menuId) {
        return menuItemRepository.findNotSoftDeleted(menuId);
    }

    /**
     * Get items with specific tag
     */
    public List<MenuItem> getItemsByTag(UUID menuId, String tag) {
        return menuItemRepository.findByMenuIdAndTag(menuId, tag);
    }

    /**
     * Advanced search with multiple criteria
     */
    public List<MenuItem> searchMenuItems(UUID menuId, Boolean isAvailable, Boolean isVeg, 
                                          String category, BigDecimal minPrice, BigDecimal maxPrice, 
                                          String searchTerm) {
        return menuItemRepository.searchMenuItems(menuId, isAvailable, isVeg, category, 
                                                   minPrice, maxPrice, searchTerm);
    }

    /**
     * Get items with nutrition info
     */
    public List<MenuItem> getItemsWithNutrition(UUID menuId) {
        return menuItemRepository.findItemsWithNutrition(menuId);
    }

    /**
     * Get items with variants
     */
    public List<MenuItem> getItemsWithVariants(UUID menuId) {
        return menuItemRepository.findItemsWithVariants(menuId);
    }

    /**
     * Get items with addons
     */
    public List<MenuItem> getItemsWithAddons(UUID menuId) {
        return menuItemRepository.findItemsWithAddons(menuId);
    }

    /**
     * Get popular items
     */
    public List<MenuItem> getPopularItems(UUID menuId) {
        return menuItemRepository.findPopularItems(menuId);
    }

    /**
     * Get new items
     */
    public List<MenuItem> getNewItems(UUID menuId) {
        return menuItemRepository.findNewItems(menuId);
    }

    /**
     * Get all distinct categories in a menu
     */
    public List<String> getDistinctCategories(UUID menuId) {
        return menuItemRepository.findDistinctCategoriesByMenuId(menuId);
    }

    /**
     * Count items by menu
     */
    public long countItemsByMenu(UUID menuId) {
        return menuItemRepository.countByMenuId(menuId);
    }

    /**
     * Count available items by menu
     */
    public long countAvailableItemsByMenu(UUID menuId) {
        return menuItemRepository.countByMenuIdAndIsAvailableTrue(menuId);
    }

    /**
     * Count by category
     */
    public long countByCategory(UUID menuId, String category) {
        return menuItemRepository.countByMenuIdAndCategory(menuId, category);
    }

    /**
     * Check if item name exists in menu
     */
    public boolean itemNameExists(UUID menuId, String name) {
        return menuItemRepository.existsByMenuIdAndName(menuId, name);
    }

    /**
     * Update an existing menu item
     */
    @Transactional
    public MenuItem updateMenuItem(UUID id, MenuItem updatedItem) {
        return menuItemRepository.findById(id)
                .map(existingItem -> {
                    // Check if name is being changed and if it conflicts
                    if (!existingItem.getName().equals(updatedItem.getName())) {
                        if (menuItemRepository.existsByMenuIdAndName(
                                requireMenuId(existingItem), updatedItem.getName())) {
                            throw new RuntimeException("Menu item with name '" + updatedItem.getName() + 
                                    "' already exists in this menu");
                        }
                    }
                    
                    existingItem.setRestaurant(updatedItem.getRestaurant());
                    existingItem.setMenu(updatedItem.getMenu());
                    existingItem.setCategory(updatedItem.getCategory());
                    existingItem.setName(updatedItem.getName());
                    existingItem.setDescription(updatedItem.getDescription());
                    existingItem.setAiDescription(updatedItem.getAiDescription());
                    existingItem.setBasePrice(updatedItem.getBasePrice());
                    existingItem.setDiscountPrice(updatedItem.getDiscountPrice());
                    existingItem.setIsAvailable(updatedItem.getIsAvailable());
                    existingItem.setIsVeg(updatedItem.getIsVeg());
                    existingItem.setIsVegan(updatedItem.getIsVegan());
                    existingItem.setIsSpicy(updatedItem.getIsSpicy());
                    existingItem.setContainsEgg(updatedItem.getContainsEgg());
                    existingItem.setContainsGluten(updatedItem.getContainsGluten());
                    existingItem.setContainsNuts(updatedItem.getContainsNuts());
                    existingItem.setContainsDairy(updatedItem.getContainsDairy());
                    existingItem.setSpiceLevel(updatedItem.getSpiceLevel());
                    existingItem.setPrepTimeMinutes(updatedItem.getPrepTimeMinutes());
                    existingItem.setImageUrl(updatedItem.getImageUrl());
                    existingItem.setGallery(updatedItem.getGallery());
                    existingItem.setNutrition(updatedItem.getNutrition());
                    existingItem.setIngredients(updatedItem.getIngredients());
                    existingItem.setCuisineType(updatedItem.getCuisineType());
                    existingItem.setCourseType(updatedItem.getCourseType());
                    existingItem.setDisplayOrder(updatedItem.getDisplayOrder());
                    existingItem.setBadge(updatedItem.getBadge());
                    existingItem.setHasVariants(updatedItem.getHasVariants());
                    existingItem.setHasAddons(updatedItem.getHasAddons());
                    existingItem.setMeta(updatedItem.getMeta());
                    existingItem.setCreatedBy(updatedItem.getCreatedBy());
                    existingItem.setUpdatedBy(updatedItem.getUpdatedBy());
                    existingItem.setDeleted(updatedItem.getDeleted());
                    
                    return menuItemRepository.save(existingItem);
                })
                .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
    }

    /**
     * Partially update menu item (only non-null fields)
     */
    @Transactional
    public MenuItem patchMenuItem(UUID id, MenuItem updates) {
        return menuItemRepository.findById(id)
                .map(existing -> {
                    if (updates.getName() != null) {
                        if (!existing.getName().equals(updates.getName())) {
                            if (menuItemRepository.existsByMenuIdAndName(
                                    requireMenuId(existing), updates.getName())) {
                                throw new RuntimeException("Menu item with name '" + updates.getName() + 
                                        "' already exists in this menu");
                            }
                        }
                        existing.setName(updates.getName());
                    }
                    if (updates.getRestaurant() != null) existing.setRestaurant(updates.getRestaurant());
                    if (updates.getMenu() != null) existing.setMenu(updates.getMenu());
                    if (updates.getCategory() != null) existing.setCategory(updates.getCategory());
                    if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
                    if (updates.getAiDescription() != null) existing.setAiDescription(updates.getAiDescription());
                    if (updates.getBasePrice() != null) existing.setBasePrice(updates.getBasePrice());
                    if (updates.getDiscountPrice() != null) existing.setDiscountPrice(updates.getDiscountPrice());
                    if (updates.getIsAvailable() != null) existing.setIsAvailable(updates.getIsAvailable());
                    if (updates.getIsVeg() != null) existing.setIsVeg(updates.getIsVeg());
                    if (updates.getIsVegan() != null) existing.setIsVegan(updates.getIsVegan());
                    if (updates.getIsSpicy() != null) existing.setIsSpicy(updates.getIsSpicy());
                    if (updates.getContainsEgg() != null) existing.setContainsEgg(updates.getContainsEgg());
                    if (updates.getContainsGluten() != null) existing.setContainsGluten(updates.getContainsGluten());
                    if (updates.getContainsNuts() != null) existing.setContainsNuts(updates.getContainsNuts());
                    if (updates.getContainsDairy() != null) existing.setContainsDairy(updates.getContainsDairy());
                    if (updates.getSpiceLevel() != null) existing.setSpiceLevel(updates.getSpiceLevel());
                    if (updates.getPrepTimeMinutes() != null) existing.setPrepTimeMinutes(updates.getPrepTimeMinutes());
                    if (updates.getImageUrl() != null) existing.setImageUrl(updates.getImageUrl());
                    if (updates.getGallery() != null) existing.setGallery(updates.getGallery());
                    if (updates.getNutrition() != null) existing.setNutrition(updates.getNutrition());
                    if (updates.getIngredients() != null) existing.setIngredients(updates.getIngredients());
                    if (updates.getCuisineType() != null) existing.setCuisineType(updates.getCuisineType());
                    if (updates.getCourseType() != null) existing.setCourseType(updates.getCourseType());
                    if (updates.getDisplayOrder() != null) existing.setDisplayOrder(updates.getDisplayOrder());
                    if (updates.getBadge() != null) existing.setBadge(updates.getBadge());
                    if (updates.getHasVariants() != null) existing.setHasVariants(updates.getHasVariants());
                    if (updates.getHasAddons() != null) existing.setHasAddons(updates.getHasAddons());
                    if (updates.getMeta() != null) existing.setMeta(updates.getMeta());
                    if (updates.getCreatedBy() != null) existing.setCreatedBy(updates.getCreatedBy());
                    if (updates.getUpdatedBy() != null) existing.setUpdatedBy(updates.getUpdatedBy());
                    if (updates.getDeleted() != null) existing.setDeleted(updates.getDeleted());
                    
                    return menuItemRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
    }

    /**
     * Delete a menu item by ID (hard delete)
     */
    @Transactional
    public void deleteMenuItem(UUID id) {
        menuItemRepository.deleteById(id);
    }

    /**
     * Soft delete - mark as deleted using the deleted flag
     */
    @Transactional
    public MenuItem softDeleteMenuItem(UUID id) {
        return menuItemRepository.findById(id)
                .map(menuItem -> {
                    menuItem.setDeleted((short) 1);
                    menuItem.setIsAvailable(false);
                    return menuItemRepository.save(menuItem);
                })
                .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
    }

    /**
     * Restore soft-deleted menu item
     */
    @Transactional
    public MenuItem restoreMenuItem(UUID id) {
        return menuItemRepository.findById(id)
                .map(menuItem -> {
                    menuItem.setDeleted((short) 0);
                    return menuItemRepository.save(menuItem);
                })
                .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
    }

    /**
     * Toggle availability
     */
    @Transactional
    public MenuItem toggleAvailability(UUID id) {
        return menuItemRepository.findById(id)
                .map(menuItem -> {
                    menuItem.setIsAvailable(!menuItem.getIsAvailable());
                    return menuItemRepository.save(menuItem);
                })
                .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
    }

    /**
     * Update availability status
     */
    @Transactional
    public MenuItem updateAvailability(UUID id, Boolean isAvailable) {
        return menuItemRepository.findById(id)
                .map(menuItem -> {
                    menuItem.setIsAvailable(isAvailable);
                    return menuItemRepository.save(menuItem);
                })
                .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
    }

    /**
     * Update display order
     */
    @Transactional
    public MenuItem updateDisplayOrder(UUID id, Integer newOrder) {
        return menuItemRepository.findById(id)
                .map(menuItem -> {
                    menuItem.setDisplayOrder(newOrder.shortValue());
                    return menuItemRepository.save(menuItem);
                })
                .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
    }

    /**
     * Update nutrition info
     */
    @Transactional
    public MenuItem updateNutrition(UUID id, Map<String, Object> nutrition) {
        return menuItemRepository.findById(id)
                .map(menuItem -> {
                    menuItem.setNutrition(nutrition);
                    return menuItemRepository.save(menuItem);
                })
                .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
    }

    /**
     * Check if a menu item exists
     */
    public boolean menuItemExists(UUID id) {
        return menuItemRepository.existsById(id);
    }

    /**
     * Count total menu items
     */
    public long countMenuItems() {
        return menuItemRepository.count();
    }

    private UUID requireMenuId(MenuItem menuItem) {
        if (menuItem.getMenu() == null || menuItem.getMenu().getId() == null) {
            throw new RuntimeException("Menu reference is required on MenuItem");
        }
        return menuItem.getMenu().getId();
    }

    private UUID requireRestaurantId(MenuItem menuItem) {
        if (menuItem.getRestaurant() == null || menuItem.getRestaurant().getId() == null) {
            throw new RuntimeException("Restaurant reference is required on MenuItem");
        }
        return menuItem.getRestaurant().getId();
    }
}


// // Create a menu item with variants
// MenuItem pizza = new MenuItem();
// pizza.setRestaurantId(1L);
// pizza.setMenuId(100L);
// pizza.setName("Margherita Pizza");
// pizza.setPrice(new BigDecimal("299.00"));
// pizza.setDiscountPrice(new BigDecimal("249.00"));
// pizza.setIsVeg(true);
// pizza.setIsAvailable(true);
// pizza.setBadge("Bestseller");

// // Add variants (sizes)
// List<Map<String, Object>> variants = List.of(
//     Map.of("size", "Medium", "price", 299),
//     Map.of("size", "Large", "price", 449)
// );
// pizza.setVariants(variants);

// // Add tags
// pizza.setTags(new String[]{"gluten-free", "chef-special"});

// menuItemService.createMenuItem(pizza);

// // Get all vegetarian items in a menu
// List<MenuItem> vegItems = menuItemService.getVegetarianItems(menuId);

// // Toggle availability (staff operation)
// menuItemService.toggleAvailability(itemId);

// // Search with multiple criteria
// List<MenuItem> results = menuItemService.searchMenuItems(
//     menuId, true, true, "Pizza", 
//     new BigDecimal("100"), new BigDecimal("500"), "special"
// );
