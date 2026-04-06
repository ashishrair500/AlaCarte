package com.alaCarte.menu.controller;

import com.alaCarte.dto.common.ApiResponse;
import com.alaCarte.menu.dto.request.AvailabilityUpdateRequestDTO;
import com.alaCarte.menu.dto.request.ComboRequestDTO;
import com.alaCarte.menu.dto.response.ComboDetailResponseDTO;
import com.alaCarte.menu.dto.response.ComboResponseDTO;
import com.alaCarte.menu.service.ComboService;
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
@RequestMapping("/api/v1/menus/{menuId}/combos")
public class ComboController {

    private final ComboService comboService;
    private final RequestContextUtil requestContextUtil;

    public ComboController(ComboService comboService, RequestContextUtil requestContextUtil) {
        this.comboService = comboService;
        this.requestContextUtil = requestContextUtil;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ComboResponseDTO>> createCombo(
            @PathVariable UUID menuId,
            @Valid @RequestBody ComboRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        ComboResponseDTO response = comboService.createCombo(restaurantId, menuId, staffId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ComboResponseDTO>>> getCombos(
            @PathVariable UUID menuId,
            @PageableDefault(size = 20, sort = "displayOrder") Pageable pageable,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(comboService.getCombos(restaurantId, menuId, pageable)));
    }

    @GetMapping("/{comboId}")
    public ResponseEntity<ApiResponse<ComboDetailResponseDTO>> getComboDetail(
            @PathVariable UUID menuId,
            @PathVariable UUID comboId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(comboService.getComboDetail(restaurantId, menuId, comboId)));
    }

    @PutMapping("/{comboId}")
    public ResponseEntity<ApiResponse<ComboResponseDTO>> updateCombo(
            @PathVariable UUID menuId,
            @PathVariable UUID comboId,
            @Valid @RequestBody ComboRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(comboService.updateCombo(restaurantId, menuId, comboId, request)));
    }

    @PatchMapping("/{comboId}/availability")
    public ResponseEntity<ApiResponse<ComboResponseDTO>> toggleAvailability(
            @PathVariable UUID menuId,
            @PathVariable UUID comboId,
            @Valid @RequestBody AvailabilityUpdateRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                comboService.toggleAvailability(restaurantId, menuId, comboId, request.getIsAvailable())
        ));
    }

    @DeleteMapping("/{comboId}")
    public ResponseEntity<ApiResponse<Void>> deleteCombo(
            @PathVariable UUID menuId,
            @PathVariable UUID comboId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        comboService.deleteCombo(restaurantId, menuId, comboId);
        return ResponseEntity.ok(ApiResponse.success("deleted", null));
    }
}
