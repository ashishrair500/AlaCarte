package com.alaCarte.menu.repository;

import com.alaCarte.entity.MenuItemAddonGroupMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemAddonGroupMapRepository extends JpaRepository<MenuItemAddonGroupMap, UUID> {

    @Query("""
        SELECT m FROM MenuItemAddonGroupMap m
        JOIN FETCH m.addonGroup g
        WHERE m.menuItem.id = :menuItemId
          AND m.deleted = 0
          AND g.deleted = 0
        ORDER BY m.displayOrder ASC
        """)
    List<MenuItemAddonGroupMap> findByMenuItemIdWithGroup(@Param("menuItemId") UUID menuItemId);

    boolean existsByMenuItemIdAndAddonGroupIdAndDeleted(UUID menuItemId, UUID addonGroupId, Short deleted);

    Integer countByMenuItemIdAndDeleted(UUID menuItemId, Short deleted);

    Optional<MenuItemAddonGroupMap> findByMenuItemIdAndAddonGroupIdAndDeleted(
            UUID menuItemId,
            UUID addonGroupId,
            Short deleted
    );
}
