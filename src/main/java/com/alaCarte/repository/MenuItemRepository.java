package com.alaCarte.repository;

import com.alaCarte.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for MenuItem entity
 * Spring Data JPA automatically implements this interface
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    // Find by restaurant
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurant.id = :restaurantId")
    List<MenuItem> findByRestaurantId(@Param("restaurantId") UUID restaurantId);

    // Find by menu
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId")
    List<MenuItem> findByMenuId(@Param("menuId") UUID menuId);

    // Find by restaurant and menu
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurant.id = :restaurantId AND mi.menu.id = :menuId")
    List<MenuItem> findByRestaurantIdAndMenuId(@Param("restaurantId") UUID restaurantId, @Param("menuId") UUID menuId);

    // Find available items by menu
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.isAvailable = true")
    List<MenuItem> findByMenuIdAndIsAvailableTrue(@Param("menuId") UUID menuId);

    // Find available items by restaurant
    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurant.id = :restaurantId AND mi.isAvailable = true")
    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(@Param("restaurantId") UUID restaurantId);

    // Find by category
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.category.name = :category")
    List<MenuItem> findByMenuIdAndCategory(@Param("menuId") UUID menuId, @Param("category") String category);

    // Find vegetarian items
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.isVeg = true")
    List<MenuItem> findByMenuIdAndIsVegTrue(@Param("menuId") UUID menuId);

    // Find non-vegetarian items
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.isVeg = false")
    List<MenuItem> findByMenuIdAndIsVegFalse(@Param("menuId") UUID menuId);

    // Find spicy items
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.isSpicy = true")
    List<MenuItem> findByMenuIdAndIsSpicyTrue(@Param("menuId") UUID menuId);

    // Find egg-free items
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.containsEgg = false")
    List<MenuItem> findByMenuIdAndContainsEggFalse(@Param("menuId") UUID menuId);

    // Find by name containing (case-insensitive)
    List<MenuItem> findByNameContainingIgnoreCase(String name);

    // Find by name in a specific menu
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND LOWER(mi.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<MenuItem> findByMenuIdAndNameContainingIgnoreCase(@Param("menuId") UUID menuId, @Param("name") String name);

    // Find items within price range
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.basePrice BETWEEN :minPrice AND :maxPrice")
    List<MenuItem> findByMenuIdAndPriceBetween(@Param("menuId") UUID menuId, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    // Find items with discount
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.discountPrice IS NOT NULL")
    List<MenuItem> findDiscountedItems(@Param("menuId") UUID menuId);

    // Find items by badge
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.badge = :badge")
    List<MenuItem> findByMenuIdAndBadge(@Param("menuId") UUID menuId, @Param("badge") String badge);

    // Find items ordered by display order
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId ORDER BY mi.displayOrder ASC")
    List<MenuItem> findByMenuIdOrderByDisplayOrderAsc(@Param("menuId") UUID menuId);

    // Find available items ordered by display order
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.isAvailable = true ORDER BY mi.displayOrder ASC")
    List<MenuItem> findAvailableItemsOrdered(@Param("menuId") UUID menuId);

    // Find items by category ordered by display order
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.category.name = :category ORDER BY mi.displayOrder ASC")
    List<MenuItem> findByCategoryOrdered(@Param("menuId") UUID menuId, @Param("category") String category);

    // Find soft-deleted items
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.deleted = 1")
    List<MenuItem> findSoftDeleted(@Param("menuId") UUID menuId);

    // Find not soft-deleted items
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.deleted = 0")
    List<MenuItem> findNotSoftDeleted(@Param("menuId") UUID menuId);

    // Count items by menu
    @Query("SELECT COUNT(mi) FROM MenuItem mi WHERE mi.menu.id = :menuId")
    long countByMenuId(@Param("menuId") UUID menuId);

    // Count available items by menu
    @Query("SELECT COUNT(mi) FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.isAvailable = true")
    long countByMenuIdAndIsAvailableTrue(@Param("menuId") UUID menuId);

    // Count by category
    @Query("SELECT COUNT(mi) FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.category.name = :category")
    long countByMenuIdAndCategory(@Param("menuId") UUID menuId, @Param("category") String category);

    // Check if item exists by name in menu
    @Query("SELECT CASE WHEN COUNT(mi) > 0 THEN true ELSE false END FROM MenuItem mi WHERE mi.menu.id = :menuId AND LOWER(mi.name) = LOWER(:name)")
    boolean existsByMenuIdAndName(@Param("menuId") UUID menuId, @Param("name") String name);

    // Get maximum display order for a menu
    @Query("SELECT COALESCE(MAX(mi.displayOrder), 0) FROM MenuItem mi WHERE mi.menu.id = :menuId")
    Short getMaxDisplayOrder(@Param("menuId") UUID menuId);

    // Find items with specific tag by tag name or slug
    @Query("""
           SELECT DISTINCT mi
           FROM MenuItem mi
           WHERE mi.menu.id = :menuId
             AND EXISTS (
                 SELECT 1
                 FROM MenuItemTagMap tm
                 JOIN tm.tag tag
                 WHERE tm.menuItem = mi
                   AND tm.deleted = 0
                   AND tag.deleted = 0
                   AND (LOWER(tag.name) = LOWER(:tag) OR LOWER(tag.slug) = LOWER(:tag))
             )
           """)
    List<MenuItem> findByMenuIdAndTag(@Param("menuId") UUID menuId, @Param("tag") String tag);

    // Advanced search with multiple criteria
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND " +
           "(:isAvailable IS NULL OR mi.isAvailable = :isAvailable) AND " +
           "(:isVeg IS NULL OR mi.isVeg = :isVeg) AND " +
           "(:category IS NULL OR mi.category.name = :category) AND " +
           "(:minPrice IS NULL OR mi.basePrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR mi.basePrice <= :maxPrice) AND " +
           "(CAST(:searchTerm AS string) IS NULL OR LOWER(mi.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "mi.deleted = 0")
    List<MenuItem> searchMenuItems(
            @Param("menuId") UUID menuId,
            @Param("isAvailable") Boolean isAvailable,
            @Param("isVeg") Boolean isVeg,
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("searchTerm") String searchTerm
    );

    // Find items with nutrition info
    @Query(value = "SELECT * FROM menu_item WHERE menu_id = :menuId AND nutrition IS NOT NULL AND nutrition != '{}'::jsonb AND deleted = 0", nativeQuery = true)
    List<MenuItem> findItemsWithNutrition(@Param("menuId") UUID menuId);

    // Find items with variants using denormalized flag
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.hasVariants = true")
    List<MenuItem> findItemsWithVariants(@Param("menuId") UUID menuId);

    // Find items with addons using denormalized flag
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.hasAddons = true")
    List<MenuItem> findItemsWithAddons(@Param("menuId") UUID menuId);

    // Find popular items (could be based on badge or custom logic)
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.badge IN ('Bestseller', 'Chef''s Special', 'Popular') AND mi.isAvailable = true")
    List<MenuItem> findPopularItems(@Param("menuId") UUID menuId);

    // Find new items
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.badge = 'New' AND mi.isAvailable = true")
    List<MenuItem> findNewItems(@Param("menuId") UUID menuId);

    // Get all categories for a menu
    @Query("SELECT DISTINCT mi.category.name FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.category IS NOT NULL AND mi.deleted = 0 ORDER BY mi.category.name")
    List<String> findDistinctCategoriesByMenuId(@Param("menuId") UUID menuId);
}
