package com.alaCarte.menu.repository;

import com.alaCarte.entity.MenuItemAddon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemAddonRepository extends JpaRepository<MenuItemAddon, UUID> {

    List<MenuItemAddon> findByAddonGroupIdAndDeletedOrderByDisplayOrderAsc(UUID groupId, Short deleted);

    Optional<MenuItemAddon> findByIdAndAddonGroupIdAndRestaurantIdAndDeleted(
            UUID id,
            UUID groupId,
            UUID restaurantId,
            Short deleted
    );

    Integer countByAddonGroupIdAndDeleted(UUID groupId, Short deleted);
}
