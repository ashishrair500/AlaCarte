package com.alaCarte.restaurantAdmin.controller;

import com.alaCarte.dto.ApiResponse;
import com.alaCarte.dto.MenuRequest;
import com.alaCarte.dto.MenuResponse;
import com.alaCarte.entity.Menu;
import com.alaCarte.jwtUtils.JwtUtils;
import com.alaCarte.middlewares.RestaurantAdminMiddleware;
import com.alaCarte.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for RestaurantAdmin to manage menus
 * RestaurantAdmin can perform CRUD operations on menus for their restaurant
 */
@RestController
@RequestMapping("/api/restaurant-admin/menus")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MenuManagementController {

    private final MenuService menuService;
    private final JwtUtils jwtUtils;
    private final RestaurantAdminMiddleware restaurantAdminMiddleware;

    public MenuManagementController(MenuService menuService, JwtUtils jwtUtils, RestaurantAdminMiddleware restaurantAdminMiddleware) {
        this.menuService = menuService;
        this.jwtUtils = jwtUtils;
        this.restaurantAdminMiddleware = restaurantAdminMiddleware;
    }

    /**
     * Create a new menu
     * POST /api/restaurant-admin/menus
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MenuResponse>> createMenu(
            @Valid @RequestBody MenuRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            // Extract User ID from token
            String jwtToken = token.replace("Bearer ", "");
            UUID userId = jwtUtils.getUserIdFromToken(jwtToken)
                    .orElseThrow(() -> new RuntimeException("Invalid token"));

            // Convert DTO to Entity
            Menu menu = mapToEntity(request);
            menu.setCreatedBy(userId);
            menu.setUpdatedBy(userId);

            // Create menu
            Menu savedMenu = menuService.createMenu(menu);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Menu created successfully", mapToResponse(savedMenu)));

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
                    .body(ApiResponse.error("Failed to create menu: " + e.getMessage()));
        }
    }

    /**
     * Get all menus for a restaurant
     * GET /api/restaurant-admin/menus?restaurantId=...
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMenusByRestaurant(
            @RequestParam Long restaurantId,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            List<Menu> menus = menuService.getMenusByRestaurantId(restaurantId);
            List<MenuResponse> responses = menus.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Menus retrieved successfully", responses));
        } catch (io.jsonwebtoken.JwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve menus: " + e.getMessage()));
        }
    }

    /**
     * Get menu by ID
     * GET /api/restaurant-admin/menus/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuResponse>> getMenuById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            Menu menu = menuService.getMenuById(id)
                    .orElseThrow(() -> new RuntimeException("Menu not found"));

            return ResponseEntity.ok(ApiResponse.success("Menu retrieved successfully", mapToResponse(menu)));
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
                    .body(ApiResponse.error("Failed to retrieve menu: " + e.getMessage()));
        }
    }

    /**
     * Update menu
     * PUT /api/restaurant-admin/menus/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuResponse>> updateMenu(
            @PathVariable UUID id,
            @Valid @RequestBody MenuRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            // Extract User ID from token
            String jwtToken = token.replace("Bearer ", "");
            UUID userId = jwtUtils.getUserIdFromToken(jwtToken)
                    .orElseThrow(() -> new RuntimeException("Invalid token"));

            Menu menuUpdates = mapToEntity(request);
            Menu updatedMenu = menuService.updateMenu(id, menuUpdates, userId);

            return ResponseEntity.ok(ApiResponse.success("Menu updated successfully", mapToResponse(updatedMenu)));
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
                    .body(ApiResponse.error("Failed to update menu: " + e.getMessage()));
        }
    }

    /**
     * Partial update (patch) menu
     * PATCH /api/restaurant-admin/menus/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuResponse>> patchMenu(
            @PathVariable UUID id,
            @RequestBody MenuRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            // Extract User ID from token
            String jwtToken = token.replace("Bearer ", "");
            UUID userId = jwtUtils.getUserIdFromToken(jwtToken)
                    .orElseThrow(() -> new RuntimeException("Invalid token"));

            Menu updates = mapToEntity(request);
            Menu updatedMenu = menuService.patchMenu(id, updates, userId);

            return ResponseEntity.ok(ApiResponse.success("Menu updated successfully", mapToResponse(updatedMenu)));
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
                    .body(ApiResponse.error("Failed to update menu: " + e.getMessage()));
        }
    }

    /**
     * Delete menu
     * DELETE /api/restaurant-admin/menus/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token) {
        try {
            // Validate RestaurantAdmin access
            restaurantAdminMiddleware.validateRestaurantAdmin(token);

            menuService.deleteMenu(id);
            return ResponseEntity.ok(ApiResponse.success("Menu deleted successfully", null));
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
                    .body(ApiResponse.error("Failed to delete menu: " + e.getMessage()));
        }
    }

    // Helper methods to map between Entity and DTO
    private Menu mapToEntity(MenuRequest request) {
        Menu menu = new Menu();
        menu.setRestaurantId(request.getRestaurantId());
        menu.setName(request.getName());
        menu.setDescription(request.getDescription());
        menu.setMenuType(request.getMenuType());
        menu.setIsActive(request.getIsActive());
        menu.setValidFrom(request.getValidFrom());
        menu.setValidTo(request.getValidTo());
        menu.setDisplayOrder(request.getDisplayOrder());
        menu.setCoverImage(request.getCoverImage());
        menu.setMeta(request.getMeta());
        return menu;
    }

    private MenuResponse mapToResponse(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .restaurantId(menu.getRestaurantId())
                .name(menu.getName())
                .description(menu.getDescription())
                .menuType(menu.getMenuType())
                .isActive(menu.getIsActive())
                .validFrom(menu.getValidFrom())
                .validTo(menu.getValidTo())
                .displayOrder(menu.getDisplayOrder())
                .coverImage(menu.getCoverImage())
                .meta(menu.getMeta())
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .createdBy(menu.getCreatedBy())
                .updatedBy(menu.getUpdatedBy())
                .build();
    }
}
