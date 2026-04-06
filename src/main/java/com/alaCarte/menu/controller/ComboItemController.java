package com.alaCarte.menu.controller;

import com.alaCarte.dto.common.ApiResponse;
import com.alaCarte.menu.dto.request.ComboItemRequestDTO;
import com.alaCarte.menu.dto.response.ComboItemResponseDTO;
import com.alaCarte.menu.service.ComboItemService;
import com.alaCarte.menu.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/combos/{comboId}/items")
public class ComboItemController {

    private final ComboItemService comboItemService;
    private final RequestContextUtil requestContextUtil;

    public ComboItemController(ComboItemService comboItemService, RequestContextUtil requestContextUtil) {
        this.comboItemService = comboItemService;
        this.requestContextUtil = requestContextUtil;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ComboItemResponseDTO>> addSlot(
            @PathVariable UUID comboId,
            @Valid @RequestBody ComboItemRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        ComboItemResponseDTO response = comboItemService.addSlot(restaurantId, comboId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComboItemResponseDTO>>> getSlots(
            @PathVariable UUID comboId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(comboItemService.getSlots(restaurantId, comboId)));
    }

    @PutMapping("/{slotId}")
    public ResponseEntity<ApiResponse<ComboItemResponseDTO>> updateSlot(
            @PathVariable UUID comboId,
            @PathVariable UUID slotId,
            @Valid @RequestBody ComboItemRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(comboItemService.updateSlot(restaurantId, comboId, slotId, request)));
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<ApiResponse<Void>> deleteSlot(
            @PathVariable UUID comboId,
            @PathVariable UUID slotId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        comboItemService.deleteSlot(restaurantId, comboId, slotId);
        return ResponseEntity.ok(ApiResponse.success("deleted", null));
    }
}
