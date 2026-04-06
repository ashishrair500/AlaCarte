package com.alaCarte.menu.service;

import com.alaCarte.entity.MenuCategory;
import com.alaCarte.menu.dto.request.MenuCategoryRequestDTO;
import com.alaCarte.menu.dto.response.MenuCategoryResponseDTO;
import com.alaCarte.menu.repository.MenuCategoryRepository;
import com.alaCarte.menu.util.MenuModuleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MenuCategoryService {

    private static final short NOT_DELETED = 0;
    private static final short DELETED = 1;

    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuModuleSupportService supportService;
    private final MenuModuleMapper mapper;

    public MenuCategoryService(
            MenuCategoryRepository menuCategoryRepository,
            MenuModuleSupportService supportService,
            MenuModuleMapper mapper
    ) {
        this.menuCategoryRepository = menuCategoryRepository;
        this.supportService = supportService;
        this.mapper = mapper;
    }

    public MenuCategoryResponseDTO createCategory(
            UUID restaurantId,
            UUID menuId,
            UUID staffId,
            MenuCategoryRequestDTO request
    ) {
        MenuCategory category = new MenuCategory();
        category.setRestaurant(supportService.requireRestaurant(restaurantId));
        category.setMenu(supportService.requireMenu(restaurantId, menuId));
        applyRequest(category, restaurantId, menuId, request);
        category.setCreatedBy(staffId);
        category.setUpdatedBy(staffId);
        return toResponse(menuCategoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<MenuCategoryResponseDTO> getCategories(UUID restaurantId, UUID menuId) {
        supportService.requireMenu(restaurantId, menuId);
        return menuCategoryRepository.findByMenuIdAndDeletedOrderByDisplayOrderAsc(menuId, NOT_DELETED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MenuCategoryResponseDTO getCategoryById(UUID restaurantId, UUID menuId, UUID categoryId) {
        return toResponse(supportService.requireCategory(restaurantId, menuId, categoryId));
    }

    public MenuCategoryResponseDTO updateCategory(
            UUID restaurantId,
            UUID menuId,
            UUID categoryId,
            MenuCategoryRequestDTO request
    ) {
        MenuCategory category = supportService.requireCategory(restaurantId, menuId, categoryId);
        applyRequest(category, restaurantId, menuId, request);
        return toResponse(menuCategoryRepository.save(category));
    }

    public MenuCategoryResponseDTO updateDisplayOrder(UUID restaurantId, UUID menuId, UUID categoryId, int order) {
        MenuCategory category = supportService.requireCategory(restaurantId, menuId, categoryId);
        category.setDisplayOrder((short) order);
        return toResponse(menuCategoryRepository.save(category));
    }

    public void deleteCategory(UUID restaurantId, UUID menuId, UUID categoryId) {
        MenuCategory category = supportService.requireCategory(restaurantId, menuId, categoryId);
        category.setDeleted(DELETED);
        menuCategoryRepository.save(category);
    }

    private void applyRequest(MenuCategory category, UUID restaurantId, UUID menuId, MenuCategoryRequestDTO request) {
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setDisplayOrder((short) defaultNumber(request.getDisplayOrder()));
        category.setParentCategory(
                request.getParentCategoryId() == null
                        ? null
                        : supportService.requireCategory(restaurantId, menuId, request.getParentCategoryId())
        );
    }

    private MenuCategoryResponseDTO toResponse(MenuCategory category) {
        Integer itemCount = menuCategoryRepository.countActiveItemsByCategoryId(category.getId());
        return mapper.toMenuCategoryResponse(category, itemCount == null ? 0 : itemCount);
    }

    private int defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }
}
