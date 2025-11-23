package com.foodbymood.service;

import com.foodbymood.entity.MenuItem;
import com.foodbymood.repository.MenuItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class demonstrating how to use MenuItemRepository
 * This shows common database operations with PostgreSQL
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
    public Optional<MenuItem> getMenuItemById(Long id) {
        return menuItemRepository.findById(id);
    }

    /**
     * Get menu items by category
     */
    public List<MenuItem> getMenuItemsByCategory(String category) {
        return menuItemRepository.findByCategory(category);
    }

    /**
     * Get all vegetarian menu items
     */
    public List<MenuItem> getVegetarianItems() {
        return menuItemRepository.findByIsVegetarianTrue();
    }

    /**
     * Get all vegan menu items
     */
    public List<MenuItem> getVeganItems() {
        return menuItemRepository.findByIsVeganTrue();
    }

    /**
     * Get menu items by spice level
     */
    public List<MenuItem> getMenuItemsBySpiceLevel(Integer spiceLevel) {
        return menuItemRepository.findBySpiceLevel(spiceLevel);
    }

    /**
     * Get menu items within a price range
     */
    public List<MenuItem> getMenuItemsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return menuItemRepository.findByPriceBetween(minPrice, maxPrice);
    }

    /**
     * Search menu items by name (case-insensitive)
     */
    public List<MenuItem> searchMenuItemsByName(String searchTerm) {
        return menuItemRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    /**
     * Update an existing menu item
     */
    @Transactional
    public MenuItem updateMenuItem(Long id, MenuItem updatedItem) {
        return menuItemRepository.findById(id)
                .map(existingItem -> {
                    existingItem.setName(updatedItem.getName());
                    existingItem.setDescription(updatedItem.getDescription());
                    existingItem.setPrice(updatedItem.getPrice());
                    existingItem.setCategory(updatedItem.getCategory());
                    existingItem.setIsVegetarian(updatedItem.getIsVegetarian());
                    existingItem.setIsVegan(updatedItem.getIsVegan());
                    existingItem.setIsGlutenFree(updatedItem.getIsGlutenFree());
                    existingItem.setSpiceLevel(updatedItem.getSpiceLevel());
                    return menuItemRepository.save(existingItem);
                })
                .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
    }

    /**
     * Delete a menu item by ID
     */
    @Transactional
    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }

    /**
     * Check if a menu item exists
     */
    public boolean menuItemExists(Long id) {
        return menuItemRepository.existsById(id);
    }

    /**
     * Count total menu items
     */
    public long countMenuItems() {
        return menuItemRepository.count();
    }

    /**
     * Get vegetarian items with minimum spice level
     */
    public List<MenuItem> getVegetarianItemsByMinSpiceLevel(Integer minSpiceLevel) {
        return menuItemRepository.findVegetarianByMinSpiceLevel(minSpiceLevel);
    }

    /**
     * Get items by category under a maximum price
     */
    public List<MenuItem> getItemsByCategoryAndMaxPrice(String category, BigDecimal maxPrice) {
        return menuItemRepository.findByCategoryAndMaxPrice(category, maxPrice);
    }
}


