package com.alaCarte.menu.controller;

import com.alaCarte.dto.common.ApiResponse;
import com.alaCarte.menu.dto.request.DisplayOrderUpdateRequestDTO;
import com.alaCarte.menu.dto.request.MenuRequestDTO;
import com.alaCarte.menu.dto.request.MetaUpdateRequestDTO;
import com.alaCarte.menu.dto.request.StatusUpdateRequestDTO;
import com.alaCarte.menu.dto.response.MenuResponseDTO;
import com.alaCarte.menu.util.RequestContextUtil;
import com.alaCarte.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MenuService menuService;
    private final RequestContextUtil requestContextUtil;

    public MenuController(MenuService menuService, RequestContextUtil requestContextUtil) {
        this.menuService = menuService;
        this.requestContextUtil = requestContextUtil;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuResponseDTO>> createMenu(
            @Valid @RequestBody MenuRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        MenuResponseDTO response = menuService.createMenu(restaurantId, staffId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MenuResponseDTO>>> getMenus(
            @PageableDefault(size = 20, sort = "displayOrder") Pageable pageable,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String menuType,
            @RequestParam(required = false) String search,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                menuService.getAllMenus(restaurantId, pageable, isActive, menuType, search)
        ));
    }

    @GetMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuResponseDTO>> getMenuById(
            @PathVariable UUID menuId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(menuService.getMenuById(restaurantId, menuId)));
    }

    @PutMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuResponseDTO>> updateMenu(
            @PathVariable UUID menuId,
            @Valid @RequestBody MenuRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(menuService.updateMenu(restaurantId, menuId, staffId, request)));
    }

    @PatchMapping("/{menuId}/status")
    public ResponseEntity<ApiResponse<MenuResponseDTO>> updateStatus(
            @PathVariable UUID menuId,
            @Valid @RequestBody StatusUpdateRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                menuService.toggleMenuStatus(restaurantId, menuId, staffId, request.getIsActive())
        ));
    }

    @PatchMapping("/{menuId}/order")
    public ResponseEntity<ApiResponse<MenuResponseDTO>> updateDisplayOrder(
            @PathVariable UUID menuId,
            @Valid @RequestBody DisplayOrderUpdateRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                menuService.updateDisplayOrder(restaurantId, menuId, staffId, request.getDisplayOrder())
        ));
    }

    @PatchMapping("/{menuId}/meta")
    public ResponseEntity<ApiResponse<MenuResponseDTO>> updateMeta(
            @PathVariable UUID menuId,
            @Valid @RequestBody MetaUpdateRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                menuService.updateMeta(restaurantId, menuId, staffId, request.getMeta())
        ));
    }

    @DeleteMapping("/{menuId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(
            @PathVariable UUID menuId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        menuService.deleteMenu(restaurantId, menuId, staffId);
        return ResponseEntity.ok(ApiResponse.success("deleted", null));
    }
}
