package com.alaCarte.menu.controller;

import com.alaCarte.dto.common.ApiResponse;
import com.alaCarte.menu.dto.request.AddonGroupItemRequestDTO;
import com.alaCarte.menu.dto.request.DisplayOrderUpdateRequestDTO;
import com.alaCarte.menu.dto.response.AddonGroupDetailResponseDTO;
import com.alaCarte.menu.service.AddonGroupItemService;
import com.alaCarte.menu.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/items/{itemId}/addon-groups")
public class AddonGroupItemController {

    private final AddonGroupItemService addonGroupItemService;
    private final RequestContextUtil requestContextUtil;

    public AddonGroupItemController(AddonGroupItemService addonGroupItemService, RequestContextUtil requestContextUtil) {
        this.addonGroupItemService = addonGroupItemService;
        this.requestContextUtil = requestContextUtil;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> linkGroupToItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody AddonGroupItemRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        addonGroupItemService.linkGroupToItem(restaurantId, itemId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("linked", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddonGroupDetailResponseDTO>>> getGroupsForItem(
            @PathVariable UUID itemId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(addonGroupItemService.getGroupsForItem(restaurantId, itemId)));
    }

    @PatchMapping("/{groupId}/order")
    public ResponseEntity<ApiResponse<Void>> updateDisplayOrder(
            @PathVariable UUID itemId,
            @PathVariable UUID groupId,
            @Valid @RequestBody DisplayOrderUpdateRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        addonGroupItemService.updateDisplayOrder(restaurantId, itemId, groupId, request.getDisplayOrder());
        return ResponseEntity.ok(ApiResponse.success("updated", null));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> unlinkGroupFromItem(
            @PathVariable UUID itemId,
            @PathVariable UUID groupId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        addonGroupItemService.unlinkGroupFromItem(restaurantId, itemId, groupId);
        return ResponseEntity.ok(ApiResponse.success("unlinked", null));
    }
}
