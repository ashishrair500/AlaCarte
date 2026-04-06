package com.alaCarte.menu.controller;

import com.alaCarte.dto.common.ApiResponse;
import com.alaCarte.menu.dto.request.TagRequestDTO;
import com.alaCarte.menu.dto.response.TagResponseDTO;
import com.alaCarte.menu.service.TagService;
import com.alaCarte.menu.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;
    private final RequestContextUtil requestContextUtil;

    public TagController(TagService tagService, RequestContextUtil requestContextUtil) {
        this.tagService = tagService;
        this.requestContextUtil = requestContextUtil;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TagResponseDTO>> createTag(
            @Valid @RequestBody TagRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        UUID staffId = requestContextUtil.getStaffId(httpRequest);
        TagResponseDTO response = tagService.createTag(restaurantId, staffId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponseDTO>>> getAllTags(
            @RequestParam(required = false) String type,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(tagService.getAllTags(restaurantId, type)));
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<ApiResponse<TagResponseDTO>> updateTag(
            @PathVariable UUID tagId,
            @Valid @RequestBody TagRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(tagService.updateTag(restaurantId, tagId, request)));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @PathVariable UUID tagId,
            HttpServletRequest httpRequest
    ) {
        UUID restaurantId = requestContextUtil.getRestaurantId(httpRequest);
        tagService.deleteTag(restaurantId, tagId);
        return ResponseEntity.ok(ApiResponse.success("deleted", null));
    }
}
