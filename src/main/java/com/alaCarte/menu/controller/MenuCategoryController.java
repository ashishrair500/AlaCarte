package com.alaCarte.menu.controller;

import com.alaCarte.dto.common.ApiResponse;
import com.alaCarte.menu.dto.request.DisplayOrderUpdateRequestDTO;
import com.alaCarte.menu.dto.request.MenuCategoryRequestDTO;
import com.alaCarte.menu.dto.response.MenuCategoryResponseDTO;
import com.alaCarte.menu.service.MenuCategoryService;
import com.alaCarte.menu.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/menus/{menuId}/categories")
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;
    private final RequestContextUtil requestContextUtil;

    public MenuCategoryController(MenuCategoryService menuCategoryService, RequestContextUtil requestContextUtil) {
        this.menuCategoryService = menuCategoryService;
        this.requestContextUtil = requestContextUtil;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuCategoryResponseDTO>> createCategory(
            @PathVariable UUID menuId,
            @Valid @RequestBody MenuCategoryRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        MenuCategoryResponseDTO response = menuCategoryService.createCategory(restaurantId, menuId, staffId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuCategoryResponseDTO>>> getCategories(
            @PathVariable UUID menuId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(menuCategoryService.getCategories(restaurantId, menuId)));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<MenuCategoryResponseDTO>> getCategoryById(
            @PathVariable UUID menuId,
            @PathVariable UUID categoryId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(menuCategoryService.getCategoryById(restaurantId, menuId, categoryId)));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<MenuCategoryResponseDTO>> updateCategory(
            @PathVariable UUID menuId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody MenuCategoryRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(menuCategoryService.updateCategory(restaurantId, menuId, categoryId, request)));
    }

    @PatchMapping("/{categoryId}/order")
    public ResponseEntity<ApiResponse<MenuCategoryResponseDTO>> updateDisplayOrder(
            @PathVariable UUID menuId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody DisplayOrderUpdateRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                menuCategoryService.updateDisplayOrder(restaurantId, menuId, categoryId, request.getDisplayOrder())
        ));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable UUID menuId,
            @PathVariable UUID categoryId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        menuCategoryService.deleteCategory(restaurantId, menuId, categoryId);
        return ResponseEntity.ok(ApiResponse.success("deleted", null));
    }
}
