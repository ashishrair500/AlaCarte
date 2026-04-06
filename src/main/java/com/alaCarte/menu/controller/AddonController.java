package com.alaCarte.menu.controller;

import com.alaCarte.dto.common.ApiResponse;
import com.alaCarte.menu.dto.request.AddonRequestDTO;
import com.alaCarte.menu.dto.request.AvailabilityUpdateRequestDTO;
import com.alaCarte.menu.dto.response.AddonResponseDTO;
import com.alaCarte.menu.service.AddonService;
import com.alaCarte.menu.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/addon-groups/{groupId}/addons")
public class AddonController {

    private final AddonService addonService;
    private final RequestContextUtil requestContextUtil;

    public AddonController(AddonService addonService, RequestContextUtil requestContextUtil) {
        this.addonService = addonService;
        this.requestContextUtil = requestContextUtil;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddonResponseDTO>> addAddon(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddonRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        AddonResponseDTO response = addonService.addAddon(restaurantId, groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddonResponseDTO>>> getAddons(
            @PathVariable UUID groupId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(addonService.getAddons(restaurantId, groupId)));
    }

    @PutMapping("/{addonId}")
    public ResponseEntity<ApiResponse<AddonResponseDTO>> updateAddon(
            @PathVariable UUID groupId,
            @PathVariable UUID addonId,
            @Valid @RequestBody AddonRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(addonService.updateAddon(restaurantId, groupId, addonId, request)));
    }

    @PatchMapping("/{addonId}/availability")
    public ResponseEntity<ApiResponse<AddonResponseDTO>> toggleAvailability(
            @PathVariable UUID groupId,
            @PathVariable UUID addonId,
            @Valid @RequestBody AvailabilityUpdateRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                addonService.toggleAvailability(restaurantId, groupId, addonId, request.getIsAvailable())
        ));
    }

    @DeleteMapping("/{addonId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddon(
            @PathVariable UUID groupId,
            @PathVariable UUID addonId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        addonService.deleteAddon(restaurantId, groupId, addonId);
        return ResponseEntity.ok(ApiResponse.success("deleted", null));
    }
}
