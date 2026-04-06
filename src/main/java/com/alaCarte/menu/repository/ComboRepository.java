package com.alaCarte.menu.repository;

import com.alaCarte.entity.Combo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ComboRepository extends JpaRepository<Combo, UUID> {

    Page<Combo> findByMenuIdAndRestaurantIdAndDeleted(
            UUID menuId,
            UUID restaurantId,
            Short deleted,
            Pageable pageable
    );

    Optional<Combo> findByIdAndMenuIdAndRestaurantIdAndDeleted(
            UUID id,
            UUID menuId,
            UUID restaurantId,
            Short deleted
    );
}
