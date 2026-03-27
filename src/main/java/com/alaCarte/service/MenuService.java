package com.alaCarte.service;

import com.alaCarte.entity.Menu;
import com.alaCarte.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for Menu CRUD operations
 * Provides business logic for managing restaurant menus
 */
@Service
public class MenuService {

    private final MenuRepository menuRepository;

    // Constructor injection
    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    /**
     * Create a new menu
     */
    @Transactional
    public Menu createMenu(Menu menu) {
        // Check if menu with same name already exists for this restaurant
        if (menuRepository.existsByRestaurantIdAndName(menu.getRestaurantId(), menu.getName())) {
            throw new RuntimeException("Menu with name '" + menu.getName() + "' already exists for this restaurant");
        }
        
        // Auto-assign display order if not provided
        if (menu.getDisplayOrder() == null || menu.getDisplayOrder() == 0) {
            Integer maxOrder = menuRepository.getMaxDisplayOrder(menu.getRestaurantId());
            menu.setDisplayOrder(maxOrder + 1);
        }
        
        return menuRepository.save(menu);
    }

    /**
     * Get all menus
     */
    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    /**
     * Get menu by ID
     */
    public Optional<Menu> getMenuById(UUID id) {
        return menuRepository.findById(id);
    }

    /**
     * Get all menus by restaurant ID
     */
    public List<Menu> getMenusByRestaurantId(UUID restaurantId) {
        return menuRepository.findByRestaurantId(restaurantId);
    }

    /**
     * Get active menus by restaurant ID
     */
    public List<Menu> getActiveMenusByRestaurantId(UUID restaurantId) {
        return menuRepository.findByRestaurantIdAndIsActiveTrue(restaurantId);
    }

    /**
     * Get inactive menus by restaurant ID
     */
    public List<Menu> getInactiveMenusByRestaurantId(UUID restaurantId) {
        return menuRepository.findByRestaurantIdAndIsActiveFalse(restaurantId);
    }

    /**
     * Get menus by menu type
     */
    public List<Menu> getMenusByType(String menuType) {
        return menuRepository.findByMenuType(menuType);
    }

    /**
     * Get menus by restaurant and menu type
     */
    public List<Menu> getMenusByRestaurantAndType(UUID restaurantId, String menuType) {
        return menuRepository.findByRestaurantIdAndMenuType(restaurantId, menuType);
    }

    /**
     * Get active menus by restaurant and menu type
     */
    public List<Menu> getActiveMenusByRestaurantAndType(UUID restaurantId, String menuType) {
        return menuRepository.findActiveByRestaurantIdAndMenuType(restaurantId, menuType);
    }

    /**
     * Search menus by name
     */
    public List<Menu> searchMenusByName(String name) {
        return menuRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Get menus by restaurant ordered by display order
     */
    public List<Menu> getMenusByRestaurantOrdered(UUID restaurantId) {
        return menuRepository.findByRestaurantIdOrderByDisplayOrderAsc(restaurantId);
    }

    /**
     * Get active menus by restaurant ordered by display order
     */
    public List<Menu> getActiveMenusByRestaurantOrdered(UUID restaurantId) {
        return menuRepository.findActiveByRestaurantIdOrderedByDisplayOrder(restaurantId);
    }

    /**
     * Get currently valid menus (based on date range)
     */
    public List<Menu> getCurrentlyValidMenus(UUID restaurantId) {
        return menuRepository.findCurrentlyValidMenus(restaurantId, OffsetDateTime.now());
    }

    /**
     * Get seasonal/time-limited menus
     */
    public List<Menu> getSeasonalMenus(UUID restaurantId) {
        return menuRepository.findSeasonalMenus(restaurantId);
    }

    /**
     * Get expired menus
     */
    public List<Menu> getExpiredMenus(UUID restaurantId) {
        return menuRepository.findExpiredMenus(restaurantId, OffsetDateTime.now());
    }

    /**
     * Get upcoming menus (not yet valid)
     */
    public List<Menu> getUpcomingMenus(UUID restaurantId) {
        return menuRepository.findUpcomingMenus(restaurantId, OffsetDateTime.now());
    }

    /**
     * Get menus created by a specific user
     */
    public List<Menu> getMenusCreatedBy(UUID createdBy) {
        return menuRepository.findByCreatedBy(createdBy);
    }

    /**
     * Get menus updated by a specific user
     */
    public List<Menu> getMenusUpdatedBy(UUID updatedBy) {
        return menuRepository.findByUpdatedBy(updatedBy);
    }

    /**
     * Count menus by restaurant
     */
    public long countMenusByRestaurant(UUID restaurantId) {
        return menuRepository.countByRestaurantId(restaurantId);
    }

    /**
     * Count active menus by restaurant
     */
    public long countActiveMenusByRestaurant(UUID restaurantId) {
        return menuRepository.countByRestaurantIdAndIsActiveTrue(restaurantId);
    }

    /**
     * Advanced search with multiple criteria
     */
    public List<Menu> searchMenus(UUID restaurantId, Boolean isActive, String menuType, String searchTerm) {
        return menuRepository.searchMenus(restaurantId, isActive, menuType, searchTerm);
    }

    /**
     * Get all active menus across all restaurants
     */
    public List<Menu> getAllActiveMenus() {
        return menuRepository.findAllActiveMenus();
    }

    /**
     * Find menus by restaurant and metadata
     */
    public List<Menu> getMenusByRestaurantAndMeta(UUID restaurantId, String metaJson) {
        return menuRepository.findByRestaurantIdAndMeta(restaurantId, metaJson);
    }

    /**
     * Update an existing menu
     */
    @Transactional
    public Menu updateMenu(UUID id, Menu updatedMenu, UUID updatedBy) {
        return menuRepository.findById(id)
                .map(existingMenu -> {
                    // Check if name is being changed and if it conflicts
                    if (!existingMenu.getName().equals(updatedMenu.getName())) {
                        if (menuRepository.existsByRestaurantIdAndName(
                                existingMenu.getRestaurantId(), updatedMenu.getName())) {
                            throw new RuntimeException("Menu with name '" + updatedMenu.getName() + 
                                    "' already exists for this restaurant");
                        }
                    }
                    
                    existingMenu.setName(updatedMenu.getName());
                    existingMenu.setDescription(updatedMenu.getDescription());
                    existingMenu.setMenuType(updatedMenu.getMenuType());
                    existingMenu.setIsActive(updatedMenu.getIsActive());
                    existingMenu.setValidFrom(updatedMenu.getValidFrom());
                    existingMenu.setValidTo(updatedMenu.getValidTo());
                    existingMenu.setDisplayOrder(updatedMenu.getDisplayOrder());
                    existingMenu.setCoverImage(updatedMenu.getCoverImage());
                    existingMenu.setMeta(updatedMenu.getMeta());
                    existingMenu.setUpdatedBy(updatedBy);
                    
                    return menuRepository.save(existingMenu);
                })
                .orElseThrow(() -> new RuntimeException("Menu not found with id: " + id));
    }

    /**
     * Partially update menu (only non-null fields)
     */
    @Transactional
    public Menu patchMenu(UUID id, Menu updates, UUID updatedBy) {
        return menuRepository.findById(id)
                .map(existing -> {
                    if (updates.getName() != null) {
                        if (!existing.getName().equals(updates.getName())) {
                            if (menuRepository.existsByRestaurantIdAndName(
                                    existing.getRestaurantId(), updates.getName())) {
                                throw new RuntimeException("Menu with name '" + updates.getName() + 
                                        "' already exists for this restaurant");
                            }
                        }
                        existing.setName(updates.getName());
                    }
                    if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
                    if (updates.getMenuType() != null) existing.setMenuType(updates.getMenuType());
                    if (updates.getIsActive() != null) existing.setIsActive(updates.getIsActive());
                    if (updates.getValidFrom() != null) existing.setValidFrom(updates.getValidFrom());
                    if (updates.getValidTo() != null) existing.setValidTo(updates.getValidTo());
                    if (updates.getDisplayOrder() != null) existing.setDisplayOrder(updates.getDisplayOrder());
                    if (updates.getCoverImage() != null) existing.setCoverImage(updates.getCoverImage());
                    if (updates.getMeta() != null) existing.setMeta(updates.getMeta());
                    
                    existing.setUpdatedBy(updatedBy);
                    return menuRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Menu not found with id: " + id));
    }

    /**
     * Delete a menu by ID
     */
    @Transactional
    public void deleteMenu(UUID id) {
        menuRepository.deleteById(id);
    }

    /**
     * Soft delete - deactivate menu
     */
    @Transactional
    public Menu deactivateMenu(UUID id, UUID updatedBy) {
        return menuRepository.findById(id)
                .map(menu -> {
                    menu.setIsActive(false);
                    menu.setUpdatedBy(updatedBy);
                    return menuRepository.save(menu);
                })
                .orElseThrow(() -> new RuntimeException("Menu not found with id: " + id));
    }

    /**
     * Reactivate a deactivated menu
     */
    @Transactional
    public Menu activateMenu(UUID id, UUID updatedBy) {
        return menuRepository.findById(id)
                .map(menu -> {
                    menu.setIsActive(true);
                    menu.setUpdatedBy(updatedBy);
                    return menuRepository.save(menu);
                })
                .orElseThrow(() -> new RuntimeException("Menu not found with id: " + id));
    }

    /**
     * Update menu display order
     */
    @Transactional
    public Menu updateDisplayOrder(UUID id, Integer newOrder, UUID updatedBy) {
        return menuRepository.findById(id)
                .map(menu -> {
                    menu.setDisplayOrder(newOrder);
                    menu.setUpdatedBy(updatedBy);
                    return menuRepository.save(menu);
                })
                .orElseThrow(() -> new RuntimeException("Menu not found with id: " + id));
    }

    /**
     * Update menu metadata
     */
    @Transactional
    public Menu updateMeta(UUID id, Map<String, Object> newMeta, UUID updatedBy) {
        return menuRepository.findById(id)
                .map(menu -> {
                    menu.setMeta(newMeta);
                    menu.setUpdatedBy(updatedBy);
                    return menuRepository.save(menu);
                })
                .orElseThrow(() -> new RuntimeException("Menu not found with id: " + id));
    }

    /**
     * Check if a menu exists
     */
    public boolean menuExists(UUID id) {
        return menuRepository.existsById(id);
    }

    /**
     * Check if menu name exists for restaurant
     */
    public boolean menuNameExists(UUID restaurantId, String name) {
        return menuRepository.existsByRestaurantIdAndName(restaurantId, name);
    }

    /**
     * Count total menus
     */
    public long countMenus() {
        return menuRepository.count();
    }
}

