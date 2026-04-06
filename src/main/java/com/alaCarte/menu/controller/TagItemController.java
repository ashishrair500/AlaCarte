package com.alaCarte.menu.controller;

import com.alaCarte.dto.common.ApiResponse;
import com.alaCarte.menu.dto.request.TagItemRequestDTO;
import com.alaCarte.menu.dto.response.TagResponseDTO;
import com.alaCarte.menu.service.TagItemService;
import com.alaCarte.menu.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/items/{itemId}/tags")
public class TagItemController {

    private final TagItemService tagItemService;
    private final RequestContextUtil requestContextUtil;

    public TagItemController(TagItemService tagItemService, RequestContextUtil requestContextUtil) {
        this.tagItemService = tagItemService;
        this.requestContextUtil = requestContextUtil;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> applyTag(
            @PathVariable UUID itemId,
            @Valid @RequestBody TagItemRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        tagItemService.applyTag(restaurantId, itemId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("applied", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponseDTO>>> getTagsForItem(
            @PathVariable UUID itemId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(tagItemService.getTagsForItem(restaurantId, itemId)));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiResponse<Void>> removeTag(
            @PathVariable UUID itemId,
            @PathVariable UUID tagId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        tagItemService.removeTag(restaurantId, itemId, tagId);
        return ResponseEntity.ok(ApiResponse.success("removed", null));
    }
}
