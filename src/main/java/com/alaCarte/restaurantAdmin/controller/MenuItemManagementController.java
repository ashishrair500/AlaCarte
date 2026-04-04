package com.alaCarte.restaurantAdmin.controller;

import com.alaCarte.dto.ApiResponse;
import com.alaCarte.dto.MenuItemRequest;
import com.alaCarte.dto.MenuItemResponse;
import com.alaCarte.entity.MenuItem;
import com.alaCarte.jwtUtils.JwtUtils;
import com.alaCarte.middlewares.RestaurantAdminMiddleware;
import com.alaCarte.service.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for RestaurantAdmin to manage menu items
 * RestaurantAdmin can perform CRUD operations on menu items for their restaurant
 */
@RestController
@RequestMapping("/api/restaurant-admin/menu-items")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MenuItemManagementController {

    private final MenuItemService menuItemService;
    private final JwtUtils jwtUtils;
    private final RestaurantAdminMiddleware restaurantAdminMiddleware;

    public MenuItemManagementController(MenuItemService menuItemService, JwtUtils jwtUtils, RestaurantAdminMiddleware restaurantAdminMiddleware) {
        this.menuItemService = menuItemService;
        this.jwtUtils = jwtUtils;
        this.restaurantAdminMiddleware = restaurantAdminMiddleware;
    }

    /**
     * Create a new menu item
     * POST /api/restaurant-admin/menu-items
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemResponse>> createMenuItem(
            @Valid @RequestBody MenuItemRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            // Convert DTO to Entity
            MenuItem menuItem = mapToEntity(request);

            // Create menu item
            MenuItem savedMenuItem = menuItemService.createMenuItem(menuItem);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Menu item created successfully", mapToResponse(savedMenuItem)));

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create menu item: " + e.getMessage()));
        }
    }

    /**
     * Bulk create menu items
     * POST /api/restaurant-admin/menu-items/bulk
     */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> bulkCreateMenuItems(
            @Valid @RequestBody List<@Valid MenuItemRequest> requests,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            List<MenuItem> menuItems = requests.stream()
                    .map(this::mapToEntity)
                    .collect(Collectors.toList());

            List<MenuItemResponse> responses = menuItemService.createMenuItemsBulk(menuItems).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Menu items uploaded successfully", responses));

        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to bulk upload menu items: " + e.getMessage()));
        }
    }

    /**
     * Get all menu items for a menu
     * GET /api/restaurant-admin/menu-items?menuId=...
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenuItemsByMenu(
            @RequestParam UUID menuId,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            List<MenuItem> menuItems = menuItemService.getMenuItemsByMenu(menuId);
            List<MenuItemResponse> responses = menuItems.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Menu items retrieved successfully", responses));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve menu items: " + e.getMessage()));
        }
    }

    /**
     * Get menu item by ID
     * GET /api/restaurant-admin/menu-items/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getMenuItemById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            MenuItem menuItem = menuItemService.getMenuItemById(id)
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));

            return ResponseEntity.ok(ApiResponse.success("Menu item retrieved successfully", mapToResponse(menuItem)));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve menu item: " + e.getMessage()));
        }
    }

    /**
     * Update menu item
     * PUT /api/restaurant-admin/menu-items/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(
            @PathVariable UUID id,
            @Valid @RequestBody MenuItemRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            MenuItem menuItemUpdates = mapToEntity(request);
            MenuItem updatedMenuItem = menuItemService.updateMenuItem(id, menuItemUpdates);

            return ResponseEntity.ok(ApiResponse.success("Menu item updated successfully", mapToResponse(updatedMenuItem)));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update menu item: " + e.getMessage()));
        }
    }

    /**
     * Partial update (patch) menu item
     * PATCH /api/restaurant-admin/menu-items/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> patchMenuItem(
            @PathVariable UUID id,
            @RequestBody MenuItemRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            MenuItem updates = mapToEntity(request);
            MenuItem updatedMenuItem = menuItemService.patchMenuItem(id, updates);

            return ResponseEntity.ok(ApiResponse.success("Menu item updated successfully", mapToResponse(updatedMenuItem)));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update menu item: " + e.getMessage()));
        }
    }

    /**
     * Delete menu item
     * DELETE /api/restaurant-admin/menu-items/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            menuItemService.deleteMenuItem(id);
            return ResponseEntity.ok(ApiResponse.success("Menu item deleted successfully", null));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete menu item: " + e.getMessage()));
        }
    }

    /**
     * Toggle availability
     * PATCH /api/restaurant-admin/menu-items/{id}/toggle-availability
     */
    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<ApiResponse<MenuItemResponse>> toggleAvailability(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            MenuItem menuItem = menuItemService.toggleAvailability(id);
            return ResponseEntity.ok(ApiResponse.success("Availability toggled successfully", mapToResponse(menuItem)));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to toggle availability: " + e.getMessage()));
        }
    }

    // Helper methods to map between Entity and DTO
    private MenuItem mapToEntity(MenuItemRequest request) {
        MenuItem menuItem = new MenuItem();
        menuItem.setRestaurantId(request.getRestaurantId());
        menuItem.setMenuId(request.getMenuId());
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setCategory(request.getCategory());
        menuItem.setPrice(request.getPrice());
        menuItem.setDiscountPrice(request.getDiscountPrice());
        menuItem.setIsAvailable(request.getIsAvailable());
        menuItem.setIsVeg(request.getIsVeg());
        menuItem.setIsSpicy(request.getIsSpicy());
        menuItem.setContainsEgg(request.getContainsEgg());
        menuItem.setVariants(request.getVariants());
        menuItem.setAddons(request.getAddons());
        menuItem.setTags(request.getTags());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setGallery(request.getGallery());
        menuItem.setNutrition(request.getNutrition());
        menuItem.setDisplayOrder(request.getDisplayOrder());
        menuItem.setBadge(request.getBadge());
        menuItem.setMeta(request.getMeta());
        return menuItem;
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId())
                .restaurantId(menuItem.getRestaurantId())
                .menuId(menuItem.getMenuId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .category(menuItem.getCategory())
                .price(menuItem.getPrice())
                .discountPrice(menuItem.getDiscountPrice())
                .isAvailable(menuItem.getIsAvailable())
                .isVeg(menuItem.getIsVeg())
                .isSpicy(menuItem.getIsSpicy())
                .containsEgg(menuItem.getContainsEgg())
                .variants(menuItem.getVariants())
                .addons(menuItem.getAddons())
                .tags(menuItem.getTags())
                .imageUrl(menuItem.getImageUrl())
                .gallery(menuItem.getGallery())
                .nutrition(menuItem.getNutrition())
                .displayOrder(menuItem.getDisplayOrder())
                .badge(menuItem.getBadge())
                .meta(menuItem.getMeta())
                .createdAt(menuItem.getCreatedAt())
                .updatedAt(menuItem.getUpdatedAt())
                .deletedAt(menuItem.getDeletedAt())
                .build();
    }
}
