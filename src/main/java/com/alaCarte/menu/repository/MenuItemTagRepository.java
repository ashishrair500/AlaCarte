package com.alaCarte.menu.repository;

import com.alaCarte.entity.MenuItemTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemTagRepository extends JpaRepository<MenuItemTag, UUID> {

    @Query("""
        SELECT t FROM MenuItemTag t
        WHERE (t.restaurant IS NULL OR t.restaurant.id = :restaurantId)
          AND t.deleted = 0
          AND t.isActive = true
        ORDER BY t.tagType, t.name
        """)
    List<MenuItemTag> findAllVisibleToRestaurant(@Param("restaurantId") UUID restaurantId);

    @Query("""
        SELECT t FROM MenuItemTag t
        WHERE (t.restaurant IS NULL OR t.restaurant.id = :restaurantId)
          AND t.tagType = :tagType
          AND t.deleted = 0
          AND t.isActive = true
        ORDER BY t.name
        """)
    List<MenuItemTag> findByTagTypeVisibleToRestaurant(
            @Param("restaurantId") UUID restaurantId,
            @Param("tagType") String tagType
    );

    Optional<MenuItemTag> findByIdAndRestaurantIdAndDeleted(UUID id, UUID restaurantId, Short deleted);

    @Query("""
        SELECT t FROM MenuItemTag t
        WHERE t.id = :tagId
          AND (t.restaurant IS NULL OR t.restaurant.id = :restaurantId)
          AND t.deleted = 0
          AND t.isActive = true
        """)
    Optional<MenuItemTag> findVisibleById(@Param("tagId") UUID tagId, @Param("restaurantId") UUID restaurantId);

    boolean existsBySlugAndRestaurantIdAndDeleted(String slug, UUID restaurantId, Short deleted);
}
