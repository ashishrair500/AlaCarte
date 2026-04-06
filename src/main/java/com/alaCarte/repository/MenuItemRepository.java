package com.alaCarte.repository;

import com.alaCarte.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    @EntityGraph(attributePaths = {"category", "menu", "restaurant"})
    Optional<MenuItem> findByIdAndRestaurantIdAndDeleted(UUID id, UUID restaurantId, Short deleted);

    @EntityGraph(attributePaths = {"category", "menu", "restaurant"})
    Optional<MenuItem> findByIdAndMenuIdAndRestaurantIdAndDeleted(UUID id, UUID menuId, UUID restaurantId, Short deleted);

    List<MenuItem> findByMenuIdAndDeletedOrderByDisplayOrderAsc(UUID menuId, Short deleted);

    List<MenuItem> findByRestaurantIdAndDeleted(UUID restaurantId, Short deleted);

    List<MenuItem> findByMenuIdAndRestaurantIdAndDeleted(UUID menuId, UUID restaurantId, Short deleted);

    @Query("""
        SELECT mi FROM MenuItem mi
        WHERE mi.menu.id = :menuId
          AND mi.deleted = 0
          AND (:categoryId IS NULL OR mi.category.id = :categoryId)
          AND (:isAvailable IS NULL OR mi.isAvailable = :isAvailable)
          AND (:isVeg IS NULL OR mi.isVeg = :isVeg)
          AND (:search IS NULL OR LOWER(mi.name) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:minPrice IS NULL OR mi.basePrice >= :minPrice)
          AND (:maxPrice IS NULL OR mi.basePrice <= :maxPrice)
          AND (:badge IS NULL OR mi.badge = :badge)
          AND (:hasVariants IS NULL OR mi.hasVariants = :hasVariants)
          AND (:hasAddons IS NULL OR mi.hasAddons = :hasAddons)
          AND (
                :tag IS NULL OR EXISTS (
                    SELECT 1
                    FROM MenuItemTagMap tm
                    JOIN tm.tag tag
                    WHERE tm.menuItem = mi
                      AND tm.deleted = 0
                      AND tag.deleted = 0
                      AND tag.isActive = true
                      AND (LOWER(tag.name) = LOWER(:tag) OR LOWER(tag.slug) = LOWER(:tag))
                )
          )
        """)
    Page<MenuItem> findAllWithFilters(
            @Param("menuId") UUID menuId,
            @Param("categoryId") UUID categoryId,
            @Param("isAvailable") Boolean isAvailable,
            @Param("isVeg") Boolean isVeg,
            @Param("search") String search,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("badge") String badge,
            @Param("tag") String tag,
            @Param("hasVariants") Boolean hasVariants,
            @Param("hasAddons") Boolean hasAddons,
            Pageable pageable
    );

    @Query("""
        SELECT mi FROM MenuItem mi
        WHERE mi.menu.id = :menuId
          AND mi.deleted = 0
          AND mi.isAvailable = true
        ORDER BY mi.displayOrder ASC
        """)
    List<MenuItem> findAvailableItemsOrdered(@Param("menuId") UUID menuId);

    @Query(value = """
        SELECT * FROM menu_item
        WHERE menu_id = :menuId
          AND deleted = 1
        ORDER BY display_order ASC
        """, nativeQuery = true)
    List<MenuItem> findSoftDeleted(@Param("menuId") UUID menuId);

    @Query(value = """
        SELECT * FROM menu_item
        WHERE id = :id
          AND menu_id = :menuId
          AND restaurant_id = :restaurantId
          AND deleted = 1
        """, nativeQuery = true)
    Optional<MenuItem> findDeletedByIdAndMenuIdAndRestaurantId(
            @Param("id") UUID id,
            @Param("menuId") UUID menuId,
            @Param("restaurantId") UUID restaurantId
    );

    @Query("""
        SELECT COALESCE(MAX(mi.displayOrder), 0) FROM MenuItem mi
        WHERE mi.menu.id = :menuId
          AND mi.deleted = 0
        """)
    Short getMaxDisplayOrder(@Param("menuId") UUID menuId);

    @Query("""
        SELECT COUNT(mi) FROM MenuItem mi
        WHERE mi.menu.id = :menuId
          AND mi.deleted = 0
        """)
    long countByMenuId(@Param("menuId") UUID menuId);

    @Query("""
        SELECT COUNT(mi) FROM MenuItem mi
        WHERE mi.menu.id = :menuId
          AND mi.deleted = 0
          AND mi.isAvailable = true
        """)
    long countByMenuIdAndIsAvailableTrue(@Param("menuId") UUID menuId);

    @Query("""
        SELECT COUNT(mi) FROM MenuItem mi
        WHERE mi.menu.id = :menuId
          AND mi.deleted = 0
          AND mi.category.id = :categoryId
        """)
    long countByMenuIdAndCategoryId(@Param("menuId") UUID menuId, @Param("categoryId") UUID categoryId);

    @Query("""
        SELECT CASE WHEN COUNT(mi) > 0 THEN true ELSE false END
        FROM MenuItem mi
        WHERE mi.menu.id = :menuId
          AND mi.deleted = 0
          AND LOWER(mi.name) = LOWER(:name)
          AND (:excludeId IS NULL OR mi.id <> :excludeId)
        """)
    boolean existsByMenuIdAndName(
            @Param("menuId") UUID menuId,
            @Param("name") String name,
            @Param("excludeId") UUID excludeId
    );

    @Query(value = """
        SELECT * FROM menu_item
        WHERE menu_id = :menuId
          AND deleted = 0
          AND nutrition IS NOT NULL
          AND nutrition <> '{}'::jsonb
        ORDER BY display_order ASC
        """, nativeQuery = true)
    List<MenuItem> findItemsWithNutrition(@Param("menuId") UUID menuId);

    @Query("""
        SELECT mi FROM MenuItem mi
        WHERE mi.menu.id = :menuId
          AND mi.deleted = 0
          AND mi.hasVariants = true
        ORDER BY mi.displayOrder ASC
        """)
    List<MenuItem> findItemsWithVariants(@Param("menuId") UUID menuId);

    @Query("""
        SELECT mi FROM MenuItem mi
        WHERE mi.menu.id = :menuId
          AND mi.deleted = 0
          AND mi.hasAddons = true
        ORDER BY mi.displayOrder ASC
        """)
    List<MenuItem> findItemsWithAddons(@Param("menuId") UUID menuId);

    @Query("""
        SELECT mi FROM MenuItem mi
        WHERE mi.menu.id = :menuId
          AND mi.deleted = 0
          AND mi.isAvailable = true
          AND mi.badge IN ('Bestseller', 'Chef''s Special', 'Popular')
        ORDER BY mi.displayOrder ASC
        """)
    List<MenuItem> findPopularItems(@Param("menuId") UUID menuId);

    @Query("""
        SELECT mi FROM MenuItem mi
        WHERE mi.menu.id = :menuId
          AND mi.deleted = 0
          AND mi.isAvailable = true
          AND mi.badge = 'New'
        ORDER BY mi.displayOrder ASC
        """)
    List<MenuItem> findNewItems(@Param("menuId") UUID menuId);

    @Query("""
        SELECT DISTINCT mi.category.name
        FROM MenuItem mi
        WHERE mi.menu.id = :menuId
          AND mi.deleted = 0
          AND mi.category IS NOT NULL
        ORDER BY mi.category.name
        """)
    List<String> findDistinctCategoriesByMenuId(@Param("menuId") UUID menuId);
}
