package com.alaCarte.menu.repository;

import com.alaCarte.entity.MenuItemAddonGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MenuItemAddonGroupRepository extends JpaRepository<MenuItemAddonGroup, UUID> {

    Page<MenuItemAddonGroup> findByRestaurantIdAndDeleted(UUID restaurantId, Short deleted, Pageable pageable);

    Optional<MenuItemAddonGroup> findByIdAndRestaurantIdAndDeleted(UUID id, UUID restaurantId, Short deleted);
}
