package com.alaCarte.menu.repository;

import com.alaCarte.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {

    List<MenuCategory> findByMenuIdAndDeletedOrderByDisplayOrderAsc(UUID menuId, Short deleted);

    Optional<MenuCategory> findByIdAndRestaurantIdAndDeleted(UUID id, UUID restaurantId, Short deleted);

    List<MenuCategory> findByMenuIdAndParentCategoryIsNullAndDeleted(UUID menuId, Short deleted);

    List<MenuCategory> findByParentCategoryIdAndDeleted(UUID parentId, Short deleted);

    long countByMenuIdAndDeleted(UUID menuId, Short deleted);

    @Query("SELECT COUNT(mi) FROM MenuItem mi WHERE mi.category.id = :categoryId AND mi.deleted = 0")
    Integer countActiveItemsByCategoryId(@Param("categoryId") UUID categoryId);
}
