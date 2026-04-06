package com.alaCarte.repository;

import com.alaCarte.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> {

    Page<Menu> findByRestaurantIdAndDeleted(UUID restaurantId, Short deleted, Pageable pageable);

    List<Menu> findByRestaurantIdAndDeletedOrderByDisplayOrderAsc(UUID restaurantId, Short deleted);

    Optional<Menu> findByIdAndRestaurantIdAndDeleted(UUID id, UUID restaurantId, Short deleted);

    boolean existsByRestaurantIdAndNameAndDeleted(UUID restaurantId, String name, Short deleted);

    boolean existsByRestaurantIdAndNameAndDeletedAndIdNot(UUID restaurantId, String name, Short deleted, UUID id);

    long countByRestaurantIdAndDeleted(UUID restaurantId, Short deleted);

    long countByRestaurantIdAndIsActiveTrueAndDeleted(UUID restaurantId, Short deleted);

    List<Menu> findByRestaurantIdAndIsActiveTrueAndDeletedOrderByDisplayOrderAsc(UUID restaurantId, Short deleted);

    List<Menu> findByRestaurantIdAndIsActiveFalseAndDeletedOrderByDisplayOrderAsc(UUID restaurantId, Short deleted);

    List<Menu> findByRestaurantIdAndMenuTypeAndDeleted(UUID restaurantId, String menuType, Short deleted);

    @Query("""
        SELECT m FROM Menu m
        WHERE m.restaurantId = :restaurantId
          AND m.deleted = 0
          AND (:isActive IS NULL OR m.isActive = :isActive)
          AND (:menuType IS NULL OR m.menuType = :menuType)
          AND (:searchTerm IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        """)
    Page<Menu> searchMenus(
            @Param("restaurantId") UUID restaurantId,
            @Param("isActive") Boolean isActive,
            @Param("menuType") String menuType,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Query("""
        SELECT COALESCE(MAX(m.displayOrder), 0) FROM Menu m
        WHERE m.restaurantId = :restaurantId
          AND m.deleted = 0
        """)
    Integer getMaxDisplayOrder(@Param("restaurantId") UUID restaurantId);

    @Query("""
        SELECT m FROM Menu m
        WHERE m.restaurantId = :restaurantId
          AND m.deleted = 0
          AND m.isActive = true
          AND (m.validFrom IS NULL OR m.validFrom <= :now)
          AND (m.validTo IS NULL OR m.validTo >= :now)
        ORDER BY m.displayOrder ASC
        """)
    List<Menu> findCurrentlyValidMenus(@Param("restaurantId") UUID restaurantId, @Param("now") OffsetDateTime now);

    @Query("""
        SELECT m FROM Menu m
        WHERE m.restaurantId = :restaurantId
          AND m.deleted = 0
          AND (m.validFrom IS NOT NULL OR m.validTo IS NOT NULL)
        ORDER BY m.displayOrder ASC
        """)
    List<Menu> findSeasonalMenus(@Param("restaurantId") UUID restaurantId);

    @Query("""
        SELECT m FROM Menu m
        WHERE m.restaurantId = :restaurantId
          AND m.deleted = 0
          AND m.validTo < :now
        ORDER BY m.displayOrder ASC
        """)
    List<Menu> findExpiredMenus(@Param("restaurantId") UUID restaurantId, @Param("now") OffsetDateTime now);

    @Query("""
        SELECT m FROM Menu m
        WHERE m.restaurantId = :restaurantId
          AND m.deleted = 0
          AND m.validFrom > :now
        ORDER BY m.displayOrder ASC
        """)
    List<Menu> findUpcomingMenus(@Param("restaurantId") UUID restaurantId, @Param("now") OffsetDateTime now);

    @Query("""
        SELECT m FROM Menu m
        WHERE m.deleted = 0
          AND m.isActive = true
        ORDER BY m.restaurantId ASC, m.displayOrder ASC
        """)
    List<Menu> findAllActiveMenus();

    @Query(value = """
        SELECT * FROM menu
        WHERE restaurant_id = :restaurantId
          AND deleted = 0
          AND meta @> CAST(:metaJson AS jsonb)
        """, nativeQuery = true)
    List<Menu> findByRestaurantIdAndMeta(@Param("restaurantId") UUID restaurantId, @Param("metaJson") String metaJson);
}
