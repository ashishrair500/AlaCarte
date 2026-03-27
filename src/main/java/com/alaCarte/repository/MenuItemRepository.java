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
    List<MenuItem> findByRestaurantId(UUID restaurantId);

    // Find by menu
    List<MenuItem> findByMenuId(UUID menuId);

    // Find by restaurant and menu
    List<MenuItem> findByRestaurantIdAndMenuId(UUID restaurantId, UUID menuId);

    // Find available items by menu
    List<MenuItem> findByMenuIdAndIsAvailableTrue(UUID menuId);

    // Find available items by restaurant
    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(UUID restaurantId);

    // Find by category
    List<MenuItem> findByMenuIdAndCategory(UUID menuId, String category);

    // Find vegetarian items
    List<MenuItem> findByMenuIdAndIsVegTrue(UUID menuId);

    // Find non-vegetarian items
    List<MenuItem> findByMenuIdAndIsVegFalse(UUID menuId);

    // Find spicy items
    List<MenuItem> findByMenuIdAndIsSpicyTrue(UUID menuId);

    // Find egg-free items
    List<MenuItem> findByMenuIdAndContainsEggFalse(UUID menuId);

    // Find by name containing (case-insensitive)
    List<MenuItem> findByNameContainingIgnoreCase(String name);

    // Find by name in a specific menu
    List<MenuItem> findByMenuIdAndNameContainingIgnoreCase(UUID menuId, String name);

    // Find items within price range
    List<MenuItem> findByMenuIdAndPriceBetween(UUID menuId, BigDecimal minPrice, BigDecimal maxPrice);

    // Find items with discount
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menuId = :menuId AND mi.discountPrice IS NOT NULL")
    List<MenuItem> findDiscountedItems(@Param("menuId") UUID menuId);

    // Find items by badge
    List<MenuItem> findByMenuIdAndBadge(UUID menuId, String badge);

    // Find items ordered by display order
    List<MenuItem> findByMenuIdOrderByDisplayOrderAsc(UUID menuId);

    // Find available items ordered by display order
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menuId = :menuId AND mi.isAvailable = true ORDER BY mi.displayOrder ASC")
    List<MenuItem> findAvailableItemsOrdered(@Param("menuId") UUID menuId);

    // Find items by category ordered by display order
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menuId = :menuId AND mi.category = :category ORDER BY mi.displayOrder ASC")
    List<MenuItem> findByCategoryOrdered(@Param("menuId") UUID menuId, @Param("category") String category);

    // Find soft-deleted items
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menuId = :menuId AND mi.deletedAt IS NOT NULL")
    List<MenuItem> findSoftDeleted(@Param("menuId") UUID menuId);

    // Find not soft-deleted items
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menuId = :menuId AND mi.deletedAt IS NULL")
    List<MenuItem> findNotSoftDeleted(@Param("menuId") UUID menuId);

    // Count items by menu
    long countByMenuId(UUID menuId);

    // Count available items by menu
    long countByMenuIdAndIsAvailableTrue(UUID menuId);

    // Count by category
    long countByMenuIdAndCategory(UUID menuId, String category);

    // Check if item exists by name in menu
    boolean existsByMenuIdAndName(UUID menuId, String name);

    // Get maximum display order for a menu
    @Query("SELECT COALESCE(MAX(mi.displayOrder), 0) FROM MenuItem mi WHERE mi.menuId = :menuId")
    Integer getMaxDisplayOrder(@Param("menuId") UUID menuId);

    // Find items with specific tag using array contains
    @Query(value = "SELECT * FROM menu_item WHERE menu_id = :menuId AND :tag = ANY(tags)", nativeQuery = true)
    List<MenuItem> findByMenuIdAndTag(@Param("menuId") UUID menuId, @Param("tag") String tag);

    // Advanced search with multiple criteria
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menuId = :menuId AND " +
           "(:isAvailable IS NULL OR mi.isAvailable = :isAvailable) AND " +
           "(:isVeg IS NULL OR mi.isVeg = :isVeg) AND " +
           "(:category IS NULL OR mi.category = :category) AND " +
           "(:minPrice IS NULL OR mi.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR mi.price <= :maxPrice) AND " +
           "(CAST(:searchTerm AS string) IS NULL OR LOWER(mi.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "mi.deletedAt IS NULL")
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
    @Query(value = "SELECT * FROM menu_item WHERE menu_id = :menuId AND nutrition IS NOT NULL AND nutrition != '{}'::jsonb", nativeQuery = true)
    List<MenuItem> findItemsWithNutrition(@Param("menuId") UUID menuId);

    // Find items with variants
    @Query(value = "SELECT * FROM menu_item WHERE menu_id = :menuId AND variants IS NOT NULL AND jsonb_array_length(variants) > 0", nativeQuery = true)
    List<MenuItem> findItemsWithVariants(@Param("menuId") UUID menuId);

    // Find items with addons
    @Query(value = "SELECT * FROM menu_item WHERE menu_id = :menuId AND addons IS NOT NULL AND jsonb_array_length(addons) > 0", nativeQuery = true)
    List<MenuItem> findItemsWithAddons(@Param("menuId") UUID menuId);

    // Find popular items (could be based on badge or custom logic)
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menuId = :menuId AND mi.badge IN ('Bestseller', 'Chef''s Special', 'Popular') AND mi.isAvailable = true")
    List<MenuItem> findPopularItems(@Param("menuId") UUID menuId);

    // Find new items
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menuId = :menuId AND mi.badge = 'New' AND mi.isAvailable = true")
    List<MenuItem> findNewItems(@Param("menuId") UUID menuId);

    // Get all categories for a menu
    @Query("SELECT DISTINCT mi.category FROM MenuItem mi WHERE mi.menuId = :menuId AND mi.deletedAt IS NULL ORDER BY mi.category")
    List<String> findDistinctCategoriesByMenuId(@Param("menuId") UUID menuId);
}
