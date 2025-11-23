package com.foodbymood.service;

import com.foodbymood.entity.Restaurant;
import com.foodbymood.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for Restaurant operations
 * Provides business logic for managing restaurants
 */
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    // Constructor injection
    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Create a new restaurant
     */
    @Transactional
    public Restaurant createRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    /**
     * Get all restaurants
     */
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    /**
     * Get restaurant by ID
     */
    public Optional<Restaurant> getRestaurantById(UUID id) {
        return restaurantRepository.findById(id);
    }

    /**
     * Search restaurants by name
     */
    public List<Restaurant> searchRestaurantsByName(String name) {
        return restaurantRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Get restaurants by city
     */
    public List<Restaurant> getRestaurantsByCity(String city) {
        return restaurantRepository.findByCity(city);
    }

    /**
     * Get restaurants by city and state
     */
    public List<Restaurant> getRestaurantsByCityAndState(String city, String state) {
        return restaurantRepository.findByCityAndState(city, state);
    }

    /**
     * Get restaurants by price range (1-3)
     */
    public List<Restaurant> getRestaurantsByPriceRange(Integer priceRange) {
        return restaurantRepository.findByPriceRange(priceRange);
    }

    /**
     * Get all vegetarian-only restaurants
     */
    public List<Restaurant> getVegOnlyRestaurants() {
        return restaurantRepository.findByIsVegOnlyTrue();
    }

    /**
     * Get restaurants with parking
     */
    public List<Restaurant> getRestaurantsWithParking() {
        return restaurantRepository.findByHasParkingTrue();
    }

    /**
     * Get popular restaurants
     */
    public List<Restaurant> getPopularRestaurants() {
        return restaurantRepository.findByIsPopularTrue();
    }

    /**
     * Get restaurants by type (cafe, fine_dine, hotel, etc.)
     */
    public List<Restaurant> getRestaurantsByType(String restaurantType) {
        return restaurantRepository.findByRestaurantType(restaurantType);
    }

    /**
     * Get open restaurants
     */
    public List<Restaurant> getOpenRestaurants() {
        return restaurantRepository.findOpenRestaurants();
    }

    /**
     * Get restaurants by status
     */
    public List<Restaurant> getRestaurantsByStatus(String status) {
        return restaurantRepository.findByStatus(status);
    }

    /**
     * Get restaurants with minimum rating
     */
    public List<Restaurant> getRestaurantsByMinRating(BigDecimal minRating) {
        return restaurantRepository.findByRatingGreaterThanEqual(minRating);
    }

    /**
     * Get restaurants by price range and minimum rating
     */
    public List<Restaurant> getRestaurantsByPriceRangeAndRating(Integer priceRange, BigDecimal minRating) {
        return restaurantRepository.findByPriceRangeAndMinRating(priceRange, minRating);
    }

    /**
     * Get restaurants by cuisine type
     */
    public List<Restaurant> getRestaurantsByCuisine(String cuisine) {
        return restaurantRepository.findByCuisineType(cuisine);
    }

    /**
     * Find restaurants within a geographic area
     */
    public List<Restaurant> findRestaurantsInArea(BigDecimal minLat, BigDecimal maxLat, 
                                                   BigDecimal minLon, BigDecimal maxLon) {
        return restaurantRepository.findRestaurantsInArea(minLat, maxLat, minLon, maxLon);
    }

    /**
     * Get restaurants in a city with minimum rating
     */
    public List<Restaurant> getRestaurantsByCityAndMinRating(String city, BigDecimal minRating) {
        return restaurantRepository.findByCityAndMinRating(city, minRating);
    }

    /**
     * Get veg-only restaurants in a city
     */
    public List<Restaurant> getVegOnlyRestaurantsByCity(String city) {
        return restaurantRepository.findVegOnlyRestaurantsByCity(city);
    }

    /**
     * Get hotels by star category
     */
    public List<Restaurant> getHotelsByStarCategory(Integer starCategory) {
        return restaurantRepository.findByHotelStarCategory(starCategory);
    }

    /**
     * Get restaurants requiring reservation
     */
    public List<Restaurant> getRestaurantsRequiringReservation() {
        return restaurantRepository.findByReservationRequiredTrue();
    }

    /**
     * Get popular restaurants by city and cuisine
     */
    public List<Restaurant> getPopularRestaurantsByCityAndCuisine(String city, String cuisine) {
        return restaurantRepository.findPopularRestaurantsByCityAndCuisine(city, cuisine);
    }

    /**
     * Get restaurants by average cost for two within range
     */
    public List<Restaurant> getRestaurantsByAvgCostRange(BigDecimal minCost, BigDecimal maxCost) {
        return restaurantRepository.findByAvgCostForTwoBetween(minCost, maxCost);
    }

    /**
     * Advanced search with multiple criteria
     */
    public List<Restaurant> searchRestaurants(String city, Integer priceRange, 
                                               BigDecimal minRating, Boolean isVegOnly) {
        return restaurantRepository.searchRestaurants(city, priceRange, minRating, isVegOnly);
    }

    /**
     * Update an existing restaurant
     */
    @Transactional
    public Restaurant updateRestaurant(UUID id, Restaurant updatedRestaurant) {
        return restaurantRepository.findById(id)
                .map(existingRestaurant -> {
                    existingRestaurant.setName(updatedRestaurant.getName());
                    existingRestaurant.setDescription(updatedRestaurant.getDescription());
                    existingRestaurant.setAddressLine1(updatedRestaurant.getAddressLine1());
                    existingRestaurant.setAddressLine2(updatedRestaurant.getAddressLine2());
                    existingRestaurant.setCity(updatedRestaurant.getCity());
                    existingRestaurant.setState(updatedRestaurant.getState());
                    existingRestaurant.setPincode(updatedRestaurant.getPincode());
                    existingRestaurant.setLatitude(updatedRestaurant.getLatitude());
                    existingRestaurant.setLongitude(updatedRestaurant.getLongitude());
                    existingRestaurant.setPhone(updatedRestaurant.getPhone());
                    existingRestaurant.setEmail(updatedRestaurant.getEmail());
                    existingRestaurant.setCuisineTypes(updatedRestaurant.getCuisineTypes());
                    existingRestaurant.setPriceRange(updatedRestaurant.getPriceRange());
                    existingRestaurant.setAvgCostForTwo(updatedRestaurant.getAvgCostForTwo());
                    existingRestaurant.setOpeningTime(updatedRestaurant.getOpeningTime());
                    existingRestaurant.setClosingTime(updatedRestaurant.getClosingTime());
                    existingRestaurant.setRating(updatedRestaurant.getRating());
                    existingRestaurant.setTotalRatings(updatedRestaurant.getTotalRatings());
                    existingRestaurant.setIsVegOnly(updatedRestaurant.getIsVegOnly());
                    existingRestaurant.setHasParking(updatedRestaurant.getHasParking());
                    existingRestaurant.setIsPopular(updatedRestaurant.getIsPopular());
                    existingRestaurant.setRestaurantType(updatedRestaurant.getRestaurantType());
                    existingRestaurant.setHotelStarCategory(updatedRestaurant.getHotelStarCategory());
                    existingRestaurant.setServiceFee(updatedRestaurant.getServiceFee());
                    existingRestaurant.setReservationRequired(updatedRestaurant.getReservationRequired());
                    existingRestaurant.setDressCode(updatedRestaurant.getDressCode());
                    existingRestaurant.setMenuQrUrl(updatedRestaurant.getMenuQrUrl());
                    existingRestaurant.setFeaturedImageUrl(updatedRestaurant.getFeaturedImageUrl());
                    existingRestaurant.setGalleryImages(updatedRestaurant.getGalleryImages());
                    existingRestaurant.setStatus(updatedRestaurant.getStatus());
                    return restaurantRepository.save(existingRestaurant);
                })
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
    }

    /**
     * Partially update restaurant (only non-null fields)
     */
    @Transactional
    public Restaurant patchRestaurant(UUID id, Restaurant updates) {
        return restaurantRepository.findById(id)
                .map(existing -> {
                    if (updates.getName() != null) existing.setName(updates.getName());
                    if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
                    if (updates.getPhone() != null) existing.setPhone(updates.getPhone());
                    if (updates.getEmail() != null) existing.setEmail(updates.getEmail());
                    if (updates.getRating() != null) existing.setRating(updates.getRating());
                    if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
                    if (updates.getIsPopular() != null) existing.setIsPopular(updates.getIsPopular());
                    // Add other fields as needed
                    return restaurantRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
    }

    /**
     * Delete a restaurant by ID
     */
    @Transactional
    public void deleteRestaurant(UUID id) {
        restaurantRepository.deleteById(id);
    }

    /**
     * Check if a restaurant exists
     */
    public boolean restaurantExists(UUID id) {
        return restaurantRepository.existsById(id);
    }

    /**
     * Count total restaurants
     */
    public long countRestaurants() {
        return restaurantRepository.count();
    }

    /**
     * Update restaurant status
     */
    @Transactional
    public Restaurant updateRestaurantStatus(UUID id, String status) {
        return restaurantRepository.findById(id)
                .map(restaurant -> {
                    restaurant.setStatus(status);
                    return restaurantRepository.save(restaurant);
                })
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
    }

    /**
     * Update restaurant rating
     */
    @Transactional
    public Restaurant updateRestaurantRating(UUID id, BigDecimal newRating) {
        return restaurantRepository.findById(id)
                .map(restaurant -> {
                    restaurant.setRating(newRating);
                    restaurant.setTotalRatings(restaurant.getTotalRatings() + 1);
                    return restaurantRepository.save(restaurant);
                })
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
    }
}

