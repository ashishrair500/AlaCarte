package com.foodbymood.repository;

import com.foodbymood.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for MenuItem entity
 * Spring Data JPA automatically implements this interface
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // Custom query methods - Spring Data JPA generates the implementation

    // Find by category
    List<MenuItem> findByCategory(String category);

    // Find vegetarian items
    List<MenuItem> findByIsVegetarianTrue();

    // Find vegan items
    List<MenuItem> findByIsVeganTrue();

    // Find items by spice level
    List<MenuItem> findBySpiceLevel(Integer spiceLevel);

    // Find items within price range
    List<MenuItem> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Find by name containing (case-insensitive)
    List<MenuItem> findByNameContainingIgnoreCase(String name);

    // Custom JPQL query
    @Query("SELECT m FROM MenuItem m WHERE m.isVegetarian = true AND m.spiceLevel >= :minSpice")
    List<MenuItem> findVegetarianByMinSpiceLevel(@Param("minSpice") Integer minSpice);

    // Native SQL query example
    @Query(value = "SELECT * FROM menu_items WHERE category = :category AND price < :maxPrice", nativeQuery = true)
    List<MenuItem> findByCategoryAndMaxPrice(@Param("category") String category, @Param("maxPrice") BigDecimal maxPrice);
}

