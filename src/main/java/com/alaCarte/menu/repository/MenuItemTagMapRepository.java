package com.alaCarte.menu.repository;

import com.alaCarte.entity.MenuItemTagMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemTagMapRepository extends JpaRepository<MenuItemTagMap, UUID> {

    @Query("""
        SELECT m FROM MenuItemTagMap m
        JOIN FETCH m.tag t
        WHERE m.menuItem.id = :menuItemId
          AND m.deleted = 0
          AND t.deleted = 0
          AND t.isActive = true
        ORDER BY t.tagType, t.name
        """)
    List<MenuItemTagMap> findByMenuItemIdWithTag(@Param("menuItemId") UUID menuItemId);

    boolean existsByMenuItemIdAndTagIdAndDeleted(UUID menuItemId, UUID tagId, Short deleted);

    Optional<MenuItemTagMap> findByMenuItemIdAndTagIdAndDeleted(UUID menuItemId, UUID tagId, Short deleted);
}
