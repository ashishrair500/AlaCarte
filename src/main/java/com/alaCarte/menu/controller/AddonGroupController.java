package com.alaCarte.menu.controller;

import com.alaCarte.dto.common.ApiResponse;
import com.alaCarte.menu.dto.request.AddonGroupRequestDTO;
import com.alaCarte.menu.dto.request.StatusUpdateRequestDTO;
import com.alaCarte.menu.dto.response.AddonGroupDetailResponseDTO;
import com.alaCarte.menu.dto.response.AddonGroupResponseDTO;
import com.alaCarte.menu.service.AddonGroupService;
import com.alaCarte.menu.util.RequestContextUtil;
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
@RequestMapping("/api/v1/addon-groups")
public class AddonGroupController {

    private final AddonGroupService addonGroupService;
    private final RequestContextUtil requestContextUtil;

    public AddonGroupController(AddonGroupService addonGroupService, RequestContextUtil requestContextUtil) {
        this.addonGroupService = addonGroupService;
        this.requestContextUtil = requestContextUtil;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddonGroupResponseDTO>> createGroup(
            @Valid @RequestBody AddonGroupRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        AddonGroupResponseDTO response = addonGroupService.createGroup(restaurantId, staffId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AddonGroupResponseDTO>>> getGroups(
            @PageableDefault(size = 20, sort = "displayOrder") Pageable pageable,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(addonGroupService.getGroups(restaurantId, pageable)));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<AddonGroupDetailResponseDTO>> getGroupDetail(
            @PathVariable UUID groupId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(addonGroupService.getGroupDetail(restaurantId, groupId)));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<AddonGroupResponseDTO>> updateGroup(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddonGroupRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(addonGroupService.updateGroup(restaurantId, groupId, request)));
    }

    @PatchMapping("/{groupId}/status")
    public ResponseEntity<ApiResponse<AddonGroupResponseDTO>> toggleStatus(
            @PathVariable UUID groupId,
            @Valid @RequestBody StatusUpdateRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                addonGroupService.toggleGroupStatus(restaurantId, groupId, request.getIsActive())
        ));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @PathVariable UUID groupId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        addonGroupService.deleteGroup(restaurantId, groupId);
        return ResponseEntity.ok(ApiResponse.success("deleted", null));
    }
}
