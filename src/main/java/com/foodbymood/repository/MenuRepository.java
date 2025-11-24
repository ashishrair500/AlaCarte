package com.foodbymood.repository;

import com.foodbymood.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Menu entity
 * Spring Data JPA automatically implements this interface
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> {

    // Find all menus by restaurant ID
    List<Menu> findByRestaurantId(Long restaurantId);

    // Find active menus by restaurant ID
    List<Menu> findByRestaurantIdAndIsActiveTrue(Long restaurantId);

    // Find inactive menus by restaurant ID
    List<Menu> findByRestaurantIdAndIsActiveFalse(Long restaurantId);

    // Find by menu type
    List<Menu> findByMenuType(String menuType);

    // Find by restaurant and menu type
    List<Menu> findByRestaurantIdAndMenuType(Long restaurantId, String menuType);

    // Find active menus by restaurant and menu type
    @Query("SELECT m FROM Menu m WHERE m.restaurantId = :restaurantId AND m.menuType = :menuType AND m.isActive = true")
    List<Menu> findActiveByRestaurantIdAndMenuType(@Param("restaurantId") Long restaurantId, @Param("menuType") String menuType);

    // Find by name (case-insensitive)
    List<Menu> findByNameContainingIgnoreCase(String name);

    // Find menus by restaurant ordered by display order
    List<Menu> findByRestaurantIdOrderByDisplayOrderAsc(Long restaurantId);

    // Find active menus by restaurant ordered by display order
    @Query("SELECT m FROM Menu m WHERE m.restaurantId = :restaurantId AND m.isActive = true ORDER BY m.displayOrder ASC")
    List<Menu> findActiveByRestaurantIdOrderedByDisplayOrder(@Param("restaurantId") Long restaurantId);

    // Find currently valid menus (based on date range)
    @Query("SELECT m FROM Menu m WHERE m.restaurantId = :restaurantId AND m.isActive = true AND " +
           "(m.validFrom IS NULL OR m.validFrom <= :now) AND (m.validTo IS NULL OR m.validTo >= :now)")
    List<Menu> findCurrentlyValidMenus(@Param("restaurantId") Long restaurantId, @Param("now") OffsetDateTime now);

    // Find seasonal/time-limited menus
    @Query("SELECT m FROM Menu m WHERE m.restaurantId = :restaurantId AND (m.validFrom IS NOT NULL OR m.validTo IS NOT NULL)")
    List<Menu> findSeasonalMenus(@Param("restaurantId") Long restaurantId);

    // Find expired menus
    @Query("SELECT m FROM Menu m WHERE m.restaurantId = :restaurantId AND m.validTo < :now")
    List<Menu> findExpiredMenus(@Param("restaurantId") Long restaurantId, @Param("now") OffsetDateTime now);

    // Find upcoming menus (not yet valid)
    @Query("SELECT m FROM Menu m WHERE m.restaurantId = :restaurantId AND m.validFrom > :now")
    List<Menu> findUpcomingMenus(@Param("restaurantId") Long restaurantId, @Param("now") OffsetDateTime now);

    // Check if a menu exists by name and restaurant
    boolean existsByRestaurantIdAndName(Long restaurantId, String name);

    // Count menus by restaurant
    long countByRestaurantId(Long restaurantId);

    // Count active menus by restaurant
    long countByRestaurantIdAndIsActiveTrue(Long restaurantId);

    // Find menus created by a specific user
    List<Menu> findByCreatedBy(UUID createdBy);

    // Find menus updated by a specific user
    List<Menu> findByUpdatedBy(UUID updatedBy);

    // Get maximum display order for a restaurant (for adding new menus)
    @Query("SELECT COALESCE(MAX(m.displayOrder), 0) FROM Menu m WHERE m.restaurantId = :restaurantId")
    Integer getMaxDisplayOrder(@Param("restaurantId") Long restaurantId);

    // Advanced search with multiple criteria
    @Query("SELECT m FROM Menu m WHERE m.restaurantId = :restaurantId AND " +
           "(:isActive IS NULL OR m.isActive = :isActive) AND " +
           "(:menuType IS NULL OR m.menuType = :menuType) AND " +
           "(CAST(:searchTerm AS string) IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Menu> searchMenus(
            @Param("restaurantId") Long restaurantId,
            @Param("isActive") Boolean isActive,
            @Param("menuType") String menuType,
            @Param("searchTerm") String searchTerm
    );

    // Find all active menus across all restaurants (for global listing)
    @Query("SELECT m FROM Menu m WHERE m.isActive = true ORDER BY m.restaurantId, m.displayOrder")
    List<Menu> findAllActiveMenus();

    // Custom query to find menus with specific metadata using JSONB
    @Query(value = "SELECT * FROM menu WHERE restaurant_id = :restaurantId AND meta @> :metaJson::jsonb", nativeQuery = true)
    List<Menu> findByRestaurantIdAndMeta(@Param("restaurantId") Long restaurantId, @Param("metaJson") String metaJson);
}

