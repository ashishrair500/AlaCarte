package com.alaCarte.repository;

import com.alaCarte.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Restaurant entity
 * Used by SuperAdmin to manage restaurant onboarding
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    // Find by name (case-insensitive)
    List<Restaurant> findByNameContainingIgnoreCase(String name);

    // Find by city
    List<Restaurant> findByCity(String city);

    // Find by city and state
    List<Restaurant> findByCityAndState(String city, String state);

    // Find by status
    List<Restaurant> findByStatus(String status);

    // Find by restaurant type
    List<Restaurant> findByRestaurantType(String restaurantType);

    // Find by price range
    List<Restaurant> findByPriceRange(Integer priceRange);

    // Find vegetarian-only restaurants
    List<Restaurant> findByIsVegOnlyTrue();

    // Find popular restaurants
    List<Restaurant> findByIsPopularTrue();

    // Find by minimum rating
    List<Restaurant> findByRatingGreaterThanEqual(BigDecimal rating);

    // Find restaurants created by specific SuperAdmin
    List<Restaurant> findByCreatedBy(UUID createdBy);

    // Find by email
    Optional<Restaurant> findByEmail(String email);

    // Check if restaurant with name exists
    boolean existsByName(String name);

    // Check if restaurant with email exists
    boolean existsByEmail(String email);

    // Advanced search
    @Query("SELECT r FROM Restaurant r WHERE " +
           "(:city IS NULL OR r.city = :city) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:restaurantType IS NULL OR r.restaurantType = :restaurantType) AND " +
           "(:minRating IS NULL OR r.rating >= :minRating) AND " +
           "(CAST(:searchTerm AS string) IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Restaurant> searchRestaurants(
            @Param("city") String city,
            @Param("status") String status,
            @Param("restaurantType") String restaurantType,
            @Param("minRating") BigDecimal minRating,
            @Param("searchTerm") String searchTerm
    );

    // Count by status
    long countByStatus(String status);

    // Count by city
    long countByCity(String city);
}
