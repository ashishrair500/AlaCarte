package com.alaCarte.menu.repository;

import com.alaCarte.entity.MenuItemVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemVariantRepository extends JpaRepository<MenuItemVariant, UUID> {

    List<MenuItemVariant> findByMenuItemIdAndDeletedOrderByDisplayOrderAsc(UUID menuItemId, Short deleted);

    Optional<MenuItemVariant> findByIdAndMenuItemIdAndRestaurantIdAndDeleted(
            UUID id,
            UUID menuItemId,
            UUID restaurantId,
            Short deleted
    );

    Integer countByMenuItemIdAndDeleted(UUID menuItemId, Short deleted);

    @Modifying
    @Query("UPDATE MenuItemVariant v SET v.isDefault = false WHERE v.menuItem.id = :menuItemId AND v.deleted = 0")
    void clearDefaultByMenuItemId(@Param("menuItemId") UUID menuItemId);
}
