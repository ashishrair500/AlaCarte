package com.alaCarte.repository;

import com.alaCarte.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Restaurant entity
 * Spring Data JPA automatically implements this interface
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    // Find by name (case-insensitive)
    List<Restaurant> findByNameContainingIgnoreCase(String name);

    // Find by city
    List<Restaurant> findByCity(String city);

    // Find by city and state
    List<Restaurant> findByCityAndState(String city, String state);

    // Find by price range
    List<Restaurant> findByPriceRange(Integer priceRange);

    // Find vegetarian-only restaurants
    List<Restaurant> findByIsVegOnlyTrue();

    // Find restaurants with parking
    List<Restaurant> findByHasParkingTrue();

    // Find popular restaurants
    List<Restaurant> findByIsPopularTrue();

    // Find by restaurant type
    List<Restaurant> findByRestaurantType(String restaurantType);

    // Find by status
    List<Restaurant> findByStatus(String status);

    // Find open restaurants
    @Query("SELECT r FROM Restaurant r WHERE r.status = 'open'")
    List<Restaurant> findOpenRestaurants();

    // Find restaurants by rating greater than or equal to specified value
    List<Restaurant> findByRatingGreaterThanEqual(BigDecimal rating);

    // Find restaurants within price range and minimum rating
    @Query("SELECT r FROM Restaurant r WHERE r.priceRange = :priceRange AND r.rating >= :minRating AND r.status = 'open'")
    List<Restaurant> findByPriceRangeAndMinRating(@Param("priceRange") Integer priceRange, @Param("minRating") BigDecimal minRating);

    // Find restaurants by cuisine type (uses array contains)
    @Query(value = "SELECT * FROM restaurants WHERE :cuisine = ANY(cuisine_types)", nativeQuery = true)
    List<Restaurant> findByCuisineType(@Param("cuisine") String cuisine);

    // Find restaurants within a geographic area (bounding box)
    @Query(value = "SELECT * FROM restaurants WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLon AND :maxLon AND status = 'open'", nativeQuery = true)
    List<Restaurant> findRestaurantsInArea(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLon") BigDecimal minLon,
            @Param("maxLon") BigDecimal maxLon
    );

    // Find restaurants by city with minimum rating
    @Query("SELECT r FROM Restaurant r WHERE r.city = :city AND r.rating >= :minRating AND r.status = 'open' ORDER BY r.rating DESC")
    List<Restaurant> findByCityAndMinRating(@Param("city") String city, @Param("minRating") BigDecimal minRating);

    // Find veg-only restaurants in a city
    @Query("SELECT r FROM Restaurant r WHERE r.city = :city AND r.isVegOnly = true AND r.status = 'open'")
    List<Restaurant> findVegOnlyRestaurantsByCity(@Param("city") String city);

    // Find hotels by star category
    List<Restaurant> findByHotelStarCategory(Integer starCategory);

    // Find restaurants requiring reservation
    List<Restaurant> findByReservationRequiredTrue();

    // Find popular restaurants in a city with specific cuisine
    @Query(value = "SELECT * FROM restaurants WHERE city = :city AND is_popular = true AND :cuisine = ANY(cuisine_types) AND status = 'open' ORDER BY rating DESC", nativeQuery = true)
    List<Restaurant> findPopularRestaurantsByCityAndCuisine(@Param("city") String city, @Param("cuisine") String cuisine);

    // Find restaurants by avg cost for two within range
    List<Restaurant> findByAvgCostForTwoBetween(BigDecimal minCost, BigDecimal maxCost);

    // Search restaurants by multiple criteria
    @Query("SELECT r FROM Restaurant r WHERE " +
            "(:city IS NULL OR r.city = :city) AND " +
            "(:priceRange IS NULL OR r.priceRange = :priceRange) AND " +
            "(:minRating IS NULL OR r.rating >= :minRating) AND " +
            "(:isVegOnly IS NULL OR r.isVegOnly = :isVegOnly) AND " +
            "r.status = 'open'")
    List<Restaurant> searchRestaurants(
            @Param("city") String city,
            @Param("priceRange") Integer priceRange,
            @Param("minRating") BigDecimal minRating,
            @Param("isVegOnly") Boolean isVegOnly
    );
}

