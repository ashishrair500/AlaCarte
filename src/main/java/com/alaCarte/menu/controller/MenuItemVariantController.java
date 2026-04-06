package com.alaCarte.menu.controller;

import com.alaCarte.dto.common.ApiResponse;
import com.alaCarte.menu.dto.request.AvailabilityUpdateRequestDTO;
import com.alaCarte.menu.dto.request.MenuItemVariantRequestDTO;
import com.alaCarte.menu.dto.response.MenuItemVariantResponseDTO;
import com.alaCarte.menu.service.MenuItemVariantService;
import com.alaCarte.menu.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/items/{itemId}/variants")
public class MenuItemVariantController {

    private final MenuItemVariantService menuItemVariantService;
    private final RequestContextUtil requestContextUtil;

    public MenuItemVariantController(MenuItemVariantService menuItemVariantService, RequestContextUtil requestContextUtil) {
        this.menuItemVariantService = menuItemVariantService;
        this.requestContextUtil = requestContextUtil;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemVariantResponseDTO>> addVariant(
            @PathVariable UUID itemId,
            @Valid @RequestBody MenuItemVariantRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        MenuItemVariantResponseDTO response = menuItemVariantService.addVariant(restaurantId, itemId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItemVariantResponseDTO>>> getVariants(
            @PathVariable UUID itemId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(menuItemVariantService.getVariants(restaurantId, itemId)));
    }

    @PutMapping("/{variantId}")
    public ResponseEntity<ApiResponse<MenuItemVariantResponseDTO>> updateVariant(
            @PathVariable UUID itemId,
            @PathVariable UUID variantId,
            @Valid @RequestBody MenuItemVariantRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                menuItemVariantService.updateVariant(restaurantId, itemId, variantId, request)
        ));
    }

    @PatchMapping("/{variantId}/availability")
    public ResponseEntity<ApiResponse<MenuItemVariantResponseDTO>> toggleAvailability(
            @PathVariable UUID itemId,
            @PathVariable UUID variantId,
            @Valid @RequestBody AvailabilityUpdateRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                menuItemVariantService.toggleAvailability(restaurantId, itemId, variantId, request.getIsAvailable())
        ));
    }

    @PatchMapping("/{variantId}/default")
    public ResponseEntity<ApiResponse<MenuItemVariantResponseDTO>> setDefault(
            @PathVariable UUID itemId,
            @PathVariable UUID variantId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(menuItemVariantService.setDefault(restaurantId, itemId, variantId)));
    }

    @DeleteMapping("/{variantId}")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @PathVariable UUID itemId,
            @PathVariable UUID variantId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        menuItemVariantService.deleteVariant(restaurantId, itemId, variantId);
        return ResponseEntity.ok(ApiResponse.success("deleted", null));
    }
}
