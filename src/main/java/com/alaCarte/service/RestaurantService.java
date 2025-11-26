package com.alaCarte.service;

import com.alaCarte.entity.Restaurant;
import com.alaCarte.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for Restaurant management
 * Handles business logic for restaurant onboarding and management
 */
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Create a new restaurant
     */
    @Transactional
    public Restaurant createRestaurant(Restaurant restaurant, UUID createdBy) {
        // Check if restaurant with same name exists
        if (restaurantRepository.existsByName(restaurant.getName())) {
            throw new RuntimeException("Restaurant with name '" + restaurant.getName() + "' already exists");
        }

        // Check if email already exists
        if (restaurant.getEmail() != null && restaurantRepository.existsByEmail(restaurant.getEmail())) {
            throw new RuntimeException("Restaurant with email '" + restaurant.getEmail() + "' already exists");
        }

        restaurant.setCreatedBy(createdBy);
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
    public List<Restaurant> searchByName(String name) {
        return restaurantRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Get restaurants by city
     */
    public List<Restaurant> getRestaurantsByCity(String city) {
        return restaurantRepository.findByCity(city);
    }

    /**
     * Get restaurants by status
     */
    public List<Restaurant> getRestaurantsByStatus(String status) {
        return restaurantRepository.findByStatus(status);
    }

    /**
     * Get restaurants by type
     */
    public List<Restaurant> getRestaurantsByType(String restaurantType) {
        return restaurantRepository.findByRestaurantType(restaurantType);
    }

    /**
     * Get vegetarian-only restaurants
     */
    public List<Restaurant> getVegOnlyRestaurants() {
        return restaurantRepository.findByIsVegOnlyTrue();
    }

    /**
     * Get popular restaurants
     */
    public List<Restaurant> getPopularRestaurants() {
        return restaurantRepository.findByIsPopularTrue();
    }

    /**
     * Get restaurants created by specific SuperAdmin
     */
    public List<Restaurant> getRestaurantsByCreator(UUID createdBy) {
        return restaurantRepository.findByCreatedBy(createdBy);
    }

    /**
     * Advanced search
     */
    public List<Restaurant> searchRestaurants(String city, String status, String restaurantType, 
                                               BigDecimal minRating, String searchTerm) {
        return restaurantRepository.searchRestaurants(city, status, restaurantType, minRating, searchTerm);
    }

    /**
     * Update restaurant
     */
    @Transactional
    public Restaurant updateRestaurant(UUID id, Restaurant updatedRestaurant) {
        return restaurantRepository.findById(id)
                .map(existing -> {
                    // Check name conflict
                    if (!existing.getName().equals(updatedRestaurant.getName()) &&
                        restaurantRepository.existsByName(updatedRestaurant.getName())) {
                        throw new RuntimeException("Restaurant name already exists");
                    }

                    // Check email conflict
                    if (updatedRestaurant.getEmail() != null &&
                        !updatedRestaurant.getEmail().equals(existing.getEmail()) &&
                        restaurantRepository.existsByEmail(updatedRestaurant.getEmail())) {
                        throw new RuntimeException("Email already exists");
                    }

                    existing.setName(updatedRestaurant.getName());
                    existing.setDescription(updatedRestaurant.getDescription());
                    existing.setAddressLine1(updatedRestaurant.getAddressLine1());
                    existing.setAddressLine2(updatedRestaurant.getAddressLine2());
                    existing.setCity(updatedRestaurant.getCity());
                    existing.setState(updatedRestaurant.getState());
                    existing.setPincode(updatedRestaurant.getPincode());
                    existing.setLatitude(updatedRestaurant.getLatitude());
                    existing.setLongitude(updatedRestaurant.getLongitude());
                    existing.setPhone(updatedRestaurant.getPhone());
                    existing.setEmail(updatedRestaurant.getEmail());
                    existing.setCuisineTypes(updatedRestaurant.getCuisineTypes());
                    existing.setPriceRange(updatedRestaurant.getPriceRange());
                    existing.setAvgCostForTwo(updatedRestaurant.getAvgCostForTwo());
                    existing.setOpeningTime(updatedRestaurant.getOpeningTime());
                    existing.setClosingTime(updatedRestaurant.getClosingTime());
                    existing.setIsVegOnly(updatedRestaurant.getIsVegOnly());
                    existing.setHasParking(updatedRestaurant.getHasParking());
                    existing.setRestaurantType(updatedRestaurant.getRestaurantType());
                    existing.setHotelStarCategory(updatedRestaurant.getHotelStarCategory());
                    existing.setServiceFee(updatedRestaurant.getServiceFee());
                    existing.setReservationRequired(updatedRestaurant.getReservationRequired());
                    existing.setDressCode(updatedRestaurant.getDressCode());
                    existing.setMenuQrUrl(updatedRestaurant.getMenuQrUrl());
                    existing.setFeaturedImageUrl(updatedRestaurant.getFeaturedImageUrl());
                    existing.setGalleryImages(updatedRestaurant.getGalleryImages());
                    existing.setStatus(updatedRestaurant.getStatus());

                    return restaurantRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
    }

    /**
     * Partial update (patch)
     */
    @Transactional
    public Restaurant patchRestaurant(UUID id, Restaurant updates) {
        return restaurantRepository.findById(id)
                .map(existing -> {
                    if (updates.getName() != null) existing.setName(updates.getName());
                    if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
                    if (updates.getPhone() != null) existing.setPhone(updates.getPhone());
                    if (updates.getEmail() != null) existing.setEmail(updates.getEmail());
                    if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
                    if (updates.getIsPopular() != null) existing.setIsPopular(updates.getIsPopular());
                    if (updates.getRating() != null) existing.setRating(updates.getRating());
                    if (updates.getFeaturedImageUrl() != null) existing.setFeaturedImageUrl(updates.getFeaturedImageUrl());

                    return restaurantRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
    }

    /**
     * Delete restaurant
     */
    @Transactional
    public void deleteRestaurant(UUID id) {
        if (!restaurantRepository.existsById(id)) {
            throw new RuntimeException("Restaurant not found with id: " + id);
        }
        restaurantRepository.deleteById(id);
    }

    /**
     * Update restaurant status
     */
    @Transactional
    public Restaurant updateStatus(UUID id, String status) {
        return restaurantRepository.findById(id)
                .map(restaurant -> {
                    restaurant.setStatus(status);
                    return restaurantRepository.save(restaurant);
                })
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
    }

    /**
     * Mark as popular
     */
    @Transactional
    public Restaurant togglePopular(UUID id) {
        return restaurantRepository.findById(id)
                .map(restaurant -> {
                    restaurant.setIsPopular(!restaurant.getIsPopular());
                    return restaurantRepository.save(restaurant);
                })
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
    }

    /**
     * Check if restaurant exists
     */
    public boolean restaurantExists(UUID id) {
        return restaurantRepository.existsById(id);
    }

    /**
     * Count restaurants
     */
    public long countRestaurants() {
        return restaurantRepository.count();
    }

    /**
     * Count by status
     */
    public long countByStatus(String status) {
        return restaurantRepository.countByStatus(status);
    }
}
