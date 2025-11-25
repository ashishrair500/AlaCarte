package com.alaCarte.service;

import com.alaCarte.entity.MenuItem;
import com.alaCarte.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        if (menuItemRepository.existsByMenuIdAndName(menuItem.getMenuId(), menuItem.getName())) {
            throw new RuntimeException("Menu item with name '" + menuItem.getName() + "' already exists in this menu");
        }
        
        // Auto-assign display order if not provided
        if (menuItem.getDisplayOrder() == null || menuItem.getDisplayOrder() == 0) {
            Integer maxOrder = menuItemRepository.getMaxDisplayOrder(menuItem.getMenuId());
            menuItem.setDisplayOrder(maxOrder + 1);
        }
        
        return menuItemRepository.save(menuItem);
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
    public List<MenuItem> getMenuItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    /**
     * Get items by menu
     */
    public List<MenuItem> getMenuItemsByMenu(Long menuId) {
        return menuItemRepository.findByMenuId(menuId);
    }

    /**
     * Get items by restaurant and menu
     */
    public List<MenuItem> getMenuItemsByRestaurantAndMenu(Long restaurantId, Long menuId) {
        return menuItemRepository.findByRestaurantIdAndMenuId(restaurantId, menuId);
    }

    /**
     * Get available items by menu
     */
    public List<MenuItem> getAvailableItemsByMenu(Long menuId) {
        return menuItemRepository.findByMenuIdAndIsAvailableTrue(menuId);
    }

    /**
     * Get available items by restaurant
     */
    public List<MenuItem> getAvailableItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);
    }

    /**
     * Get items by category
     */
    public List<MenuItem> getMenuItemsByCategory(Long menuId, String category) {
        return menuItemRepository.findByMenuIdAndCategory(menuId, category);
    }

    /**
     * Get vegetarian items
     */
    public List<MenuItem> getVegetarianItems(Long menuId) {
        return menuItemRepository.findByMenuIdAndIsVegTrue(menuId);
    }

    /**
     * Get non-vegetarian items
     */
    public List<MenuItem> getNonVegetarianItems(Long menuId) {
        return menuItemRepository.findByMenuIdAndIsVegFalse(menuId);
    }

    /**
     * Get spicy items
     */
    public List<MenuItem> getSpicyItems(Long menuId) {
        return menuItemRepository.findByMenuIdAndIsSpicyTrue(menuId);
    }

    /**
     * Get egg-free items
     */
    public List<MenuItem> getEggFreeItems(Long menuId) {
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
    public List<MenuItem> searchMenuItemsByNameInMenu(Long menuId, String searchTerm) {
        return menuItemRepository.findByMenuIdAndNameContainingIgnoreCase(menuId, searchTerm);
    }

    /**
     * Get items within price range
     */
    public List<MenuItem> getMenuItemsByPriceRange(Long menuId, BigDecimal minPrice, BigDecimal maxPrice) {
        return menuItemRepository.findByMenuIdAndPriceBetween(menuId, minPrice, maxPrice);
    }

    /**
     * Get discounted items
     */
    public List<MenuItem> getDiscountedItems(Long menuId) {
        return menuItemRepository.findDiscountedItems(menuId);
    }

    /**
     * Get items by badge
     */
    public List<MenuItem> getItemsByBadge(Long menuId, String badge) {
        return menuItemRepository.findByMenuIdAndBadge(menuId, badge);
    }

    /**
     * Get items ordered by display order
     */
    public List<MenuItem> getItemsOrderedByDisplayOrder(Long menuId) {
        return menuItemRepository.findByMenuIdOrderByDisplayOrderAsc(menuId);
    }

    /**
     * Get available items ordered by display order
     */
    public List<MenuItem> getAvailableItemsOrdered(Long menuId) {
        return menuItemRepository.findAvailableItemsOrdered(menuId);
    }

    /**
     * Get items by category ordered by display order
     */
    public List<MenuItem> getItemsByCategoryOrdered(Long menuId, String category) {
        return menuItemRepository.findByCategoryOrdered(menuId, category);
    }

    /**
     * Get soft-deleted items
     */
    public List<MenuItem> getSoftDeletedItems(Long menuId) {
        return menuItemRepository.findSoftDeleted(menuId);
    }

    /**
     * Get not soft-deleted items
     */
    public List<MenuItem> getNotSoftDeletedItems(Long menuId) {
        return menuItemRepository.findNotSoftDeleted(menuId);
    }

    /**
     * Get items with specific tag
     */
    public List<MenuItem> getItemsByTag(Long menuId, String tag) {
        return menuItemRepository.findByMenuIdAndTag(menuId, tag);
    }

    /**
     * Advanced search with multiple criteria
     */
    public List<MenuItem> searchMenuItems(Long menuId, Boolean isAvailable, Boolean isVeg, 
                                          String category, BigDecimal minPrice, BigDecimal maxPrice, 
                                          String searchTerm) {
        return menuItemRepository.searchMenuItems(menuId, isAvailable, isVeg, category, 
                                                   minPrice, maxPrice, searchTerm);
    }

    /**
     * Get items with nutrition info
     */
    public List<MenuItem> getItemsWithNutrition(Long menuId) {
        return menuItemRepository.findItemsWithNutrition(menuId);
    }

    /**
     * Get items with variants
     */
    public List<MenuItem> getItemsWithVariants(Long menuId) {
        return menuItemRepository.findItemsWithVariants(menuId);
    }

    /**
     * Get items with addons
     */
    public List<MenuItem> getItemsWithAddons(Long menuId) {
        return menuItemRepository.findItemsWithAddons(menuId);
    }

    /**
     * Get popular items
     */
    public List<MenuItem> getPopularItems(Long menuId) {
        return menuItemRepository.findPopularItems(menuId);
    }

    /**
     * Get new items
     */
    public List<MenuItem> getNewItems(Long menuId) {
        return menuItemRepository.findNewItems(menuId);
    }

    /**
     * Get all distinct categories in a menu
     */
    public List<String> getDistinctCategories(Long menuId) {
        return menuItemRepository.findDistinctCategoriesByMenuId(menuId);
    }

    /**
     * Count items by menu
     */
    public long countItemsByMenu(Long menuId) {
        return menuItemRepository.countByMenuId(menuId);
    }

    /**
     * Count available items by menu
     */
    public long countAvailableItemsByMenu(Long menuId) {
        return menuItemRepository.countByMenuIdAndIsAvailableTrue(menuId);
    }

    /**
     * Count by category
     */
    public long countByCategory(Long menuId, String category) {
        return menuItemRepository.countByMenuIdAndCategory(menuId, category);
    }

    /**
     * Check if item name exists in menu
     */
    public boolean itemNameExists(Long menuId, String name) {
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
                                existingItem.getMenuId(), updatedItem.getName())) {
                            throw new RuntimeException("Menu item with name '" + updatedItem.getName() + 
                                    "' already exists in this menu");
                        }
                    }
                    
                    existingItem.setName(updatedItem.getName());
                    existingItem.setDescription(updatedItem.getDescription());
                    existingItem.setCategory(updatedItem.getCategory());
                    existingItem.setPrice(updatedItem.getPrice());
                    existingItem.setDiscountPrice(updatedItem.getDiscountPrice());
                    existingItem.setIsAvailable(updatedItem.getIsAvailable());
                    existingItem.setIsVeg(updatedItem.getIsVeg());
                    existingItem.setIsSpicy(updatedItem.getIsSpicy());
                    existingItem.setContainsEgg(updatedItem.getContainsEgg());
                    existingItem.setVariants(updatedItem.getVariants());
                    existingItem.setAddons(updatedItem.getAddons());
                    existingItem.setTags(updatedItem.getTags());
                    existingItem.setImageUrl(updatedItem.getImageUrl());
                    existingItem.setGallery(updatedItem.getGallery());
                    existingItem.setNutrition(updatedItem.getNutrition());
                    existingItem.setDisplayOrder(updatedItem.getDisplayOrder());
                    existingItem.setBadge(updatedItem.getBadge());
                    existingItem.setMeta(updatedItem.getMeta());
                    
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
                                    existing.getMenuId(), updates.getName())) {
                                throw new RuntimeException("Menu item with name '" + updates.getName() + 
                                        "' already exists in this menu");
                            }
                        }
                        existing.setName(updates.getName());
                    }
                    if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
                    if (updates.getCategory() != null) existing.setCategory(updates.getCategory());
                    if (updates.getPrice() != null) existing.setPrice(updates.getPrice());
                    if (updates.getDiscountPrice() != null) existing.setDiscountPrice(updates.getDiscountPrice());
                    if (updates.getIsAvailable() != null) existing.setIsAvailable(updates.getIsAvailable());
                    if (updates.getIsVeg() != null) existing.setIsVeg(updates.getIsVeg());
                    if (updates.getIsSpicy() != null) existing.setIsSpicy(updates.getIsSpicy());
                    if (updates.getContainsEgg() != null) existing.setContainsEgg(updates.getContainsEgg());
                    if (updates.getVariants() != null) existing.setVariants(updates.getVariants());
                    if (updates.getAddons() != null) existing.setAddons(updates.getAddons());
                    if (updates.getTags() != null) existing.setTags(updates.getTags());
                    if (updates.getImageUrl() != null) existing.setImageUrl(updates.getImageUrl());
                    if (updates.getGallery() != null) existing.setGallery(updates.getGallery());
                    if (updates.getNutrition() != null) existing.setNutrition(updates.getNutrition());
                    if (updates.getDisplayOrder() != null) existing.setDisplayOrder(updates.getDisplayOrder());
                    if (updates.getBadge() != null) existing.setBadge(updates.getBadge());
                    if (updates.getMeta() != null) existing.setMeta(updates.getMeta());
                    
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
     * Soft delete - mark as deleted with timestamp
     */
    @Transactional
    public MenuItem softDeleteMenuItem(UUID id) {
        return menuItemRepository.findById(id)
                .map(menuItem -> {
                    menuItem.setDeletedAt(OffsetDateTime.now());
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
                    menuItem.setDeletedAt(null);
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
                    menuItem.setDisplayOrder(newOrder);
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