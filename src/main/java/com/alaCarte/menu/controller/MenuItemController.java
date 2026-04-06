package com.alaCarte.menu.controller;

import com.alaCarte.dto.common.ApiResponse;
import com.alaCarte.menu.dto.request.AvailabilityUpdateRequestDTO;
import com.alaCarte.menu.dto.request.CategoryReassignRequestDTO;
import com.alaCarte.menu.dto.request.DisplayOrderUpdateRequestDTO;
import com.alaCarte.menu.dto.request.MenuItemFilterParams;
import com.alaCarte.menu.dto.request.MenuItemRequestDTO;
import com.alaCarte.menu.dto.request.NutritionUpdateRequestDTO;
import com.alaCarte.menu.dto.response.MenuItemDetailResponseDTO;
import com.alaCarte.menu.dto.response.MenuItemResponseDTO;
import com.alaCarte.menu.util.RequestContextUtil;
import com.alaCarte.service.MenuItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/menus/{menuId}/items")
public class MenuItemController {

    private final MenuItemService menuItemService;
    private final RequestContextUtil requestContextUtil;

    public MenuItemController(MenuItemService menuItemService, RequestContextUtil requestContextUtil) {
        this.menuItemService = menuItemService;
        this.requestContextUtil = requestContextUtil;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemResponseDTO>> createItem(
            @PathVariable UUID menuId,
            @Valid @RequestBody MenuItemRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        MenuItemResponseDTO response = menuItemService.createItem(restaurantId, menuId, staffId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDTO>>> createItemsBulk(
            @PathVariable UUID menuId,
            @Valid @RequestBody List<@Valid MenuItemRequestDTO> requests,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        List<MenuItemResponseDTO> response = menuItemService.createItemsBulk(restaurantId, menuId, staffId, requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Menu items uploaded successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MenuItemResponseDTO>>> getItems(
            @PathVariable UUID menuId,
            @PageableDefault(size = 20, sort = "displayOrder") Pageable pageable,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false) Boolean isVeg,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String badge,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Boolean hasVariants,
            @RequestParam(required = false) Boolean hasAddons,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        MenuItemFilterParams filters = MenuItemFilterParams.builder()
                .categoryId(categoryId)
                .isAvailable(isAvailable)
                .isVeg(isVeg)
                .search(search)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .badge(badge)
                .tag(tag)
                .hasVariants(hasVariants)
                .hasAddons(hasAddons)
                .build();
        return ResponseEntity.ok(ApiResponse.success(menuItemService.getItems(restaurantId, menuId, filters, pageable)));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<MenuItemDetailResponseDTO>> getItemDetail(
            @PathVariable UUID menuId,
            @PathVariable UUID itemId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(menuItemService.getItemDetail(restaurantId, menuId, itemId)));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<MenuItemResponseDTO>> updateItem(
            @PathVariable UUID menuId,
            @PathVariable UUID itemId,
            @Valid @RequestBody MenuItemRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                menuItemService.updateItem(restaurantId, menuId, itemId, staffId, request)
        ));
    }

    @PatchMapping("/{itemId}/availability")
    public ResponseEntity<ApiResponse<MenuItemResponseDTO>> updateAvailability(
            @PathVariable UUID menuId,
            @PathVariable UUID itemId,
            @Valid @RequestBody AvailabilityUpdateRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                menuItemService.toggleAvailability(restaurantId, menuId, itemId, request.getIsAvailable())
        ));
    }

    @PatchMapping("/{itemId}/category")
    public ResponseEntity<ApiResponse<MenuItemResponseDTO>> reassignCategory(
            @PathVariable UUID menuId,
            @PathVariable UUID itemId,
            @Valid @RequestBody CategoryReassignRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                menuItemService.reassignCategory(restaurantId, menuId, itemId, request.getCategoryId())
        ));
    }

    @PatchMapping("/{itemId}/order")
    public ResponseEntity<ApiResponse<MenuItemResponseDTO>> updateDisplayOrder(
            @PathVariable UUID menuId,
            @PathVariable UUID itemId,
            @Valid @RequestBody DisplayOrderUpdateRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                menuItemService.updateDisplayOrder(restaurantId, menuId, itemId, request.getDisplayOrder())
        ));
    }

    @PatchMapping("/{itemId}/nutrition")
    public ResponseEntity<ApiResponse<MenuItemResponseDTO>> updateNutrition(
            @PathVariable UUID menuId,
            @PathVariable UUID itemId,
            @Valid @RequestBody NutritionUpdateRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                menuItemService.updateNutrition(restaurantId, menuId, itemId, request.getNutrition())
        ));
    }

    @PatchMapping("/{itemId}/restore")
    public ResponseEntity<ApiResponse<MenuItemResponseDTO>> restoreItem(
            @PathVariable UUID menuId,
            @PathVariable UUID itemId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                menuItemService.restoreItem(restaurantId, menuId, itemId, staffId)
        ));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable UUID menuId,
            @PathVariable UUID itemId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        menuItemService.deleteItem(restaurantId, menuId, itemId, staffId);
        return ResponseEntity.ok(ApiResponse.success("deleted", null));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDTO>>> getPopularItems(
            @PathVariable UUID menuId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(menuItemService.getPopularItems(restaurantId, menuId)));
    }

    @GetMapping("/new")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDTO>>> getNewItems(
            @PathVariable UUID menuId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(menuItemService.getNewItems(restaurantId, menuId)));
    }

    @GetMapping("/with-variants")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDTO>>> getItemsWithVariants(
            @PathVariable UUID menuId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(menuItemService.getItemsWithVariants(restaurantId, menuId)));
    }

    @GetMapping("/with-addons")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDTO>>> getItemsWithAddons(
            @PathVariable UUID menuId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(menuItemService.getItemsWithAddons(restaurantId, menuId)));
    }

    @GetMapping("/with-nutrition")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDTO>>> getItemsWithNutrition(
            @PathVariable UUID menuId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(menuItemService.getItemsWithNutrition(restaurantId, menuId)));
    }
}
