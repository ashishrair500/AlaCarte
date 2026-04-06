package com.alaCarte.service;

import com.alaCarte.entity.Menu;
import com.alaCarte.exception.DuplicateResourceException;
import com.alaCarte.exception.ResourceNotFoundException;
import com.alaCarte.menu.dto.request.MenuRequestDTO;
import com.alaCarte.menu.dto.response.MenuResponseDTO;
import com.alaCarte.menu.repository.MenuCategoryRepository;
import com.alaCarte.menu.service.MenuModuleSupportService;
import com.alaCarte.menu.util.MenuModuleMapper;
import com.alaCarte.repository.MenuItemRepository;
import com.alaCarte.repository.MenuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class MenuService {

    private static final short NOT_DELETED = 0;
    private static final short DELETED = 1;

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuModuleSupportService supportService;
    private final MenuModuleMapper mapper;

    public MenuService(
            MenuRepository menuRepository,
            MenuCategoryRepository menuCategoryRepository,
            MenuItemRepository menuItemRepository,
            MenuModuleSupportService supportService,
            MenuModuleMapper mapper
    ) {
        this.menuRepository = menuRepository;
        this.menuCategoryRepository = menuCategoryRepository;
        this.menuItemRepository = menuItemRepository;
        this.supportService = supportService;
        this.mapper = mapper;
    }

    public MenuResponseDTO createMenu(UUID restaurantId, UUID staffId, MenuRequestDTO request) {
        validateUniqueName(restaurantId, request.getName(), null);

        Menu menu = new Menu();
        menu.setRestaurantId(restaurantId);
        menu.setName(request.getName());
        menu.setDescription(request.getDescription());
        menu.setMenuType(request.getMenuType());
        menu.setIsActive(request.getIsActive() == null ? true : request.getIsActive());
        menu.setValidFrom(request.getValidFrom());
        menu.setValidTo(request.getValidTo());
        menu.setDisplayOrder(defaultDisplayOrder(restaurantId, request.getDisplayOrder()));
        menu.setCoverImage(request.getCoverImage());
        menu.setCreatedBy(staffId);
        menu.setUpdatedBy(staffId);

        return toResponse(menuRepository.save(menu));
    }

    @Transactional(readOnly = true)
    public Page<MenuResponseDTO> getAllMenus(
            UUID restaurantId,
            Pageable pageable,
            Boolean isActive,
            String menuType,
            String search
    ) {
        Pageable normalized = supportService.normalizePageable(pageable, "displayOrder");
        return menuRepository.searchMenus(restaurantId, isActive, menuType, normalize(search), normalized)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MenuResponseDTO getMenuById(UUID restaurantId, UUID menuId) {
        return toResponse(requireMenu(restaurantId, menuId));
    }

    public MenuResponseDTO updateMenu(UUID restaurantId, UUID menuId, UUID staffId, MenuRequestDTO request) {
        Menu menu = requireMenu(restaurantId, menuId);
        validateUniqueName(restaurantId, request.getName(), menuId);

        menu.setName(request.getName());
        menu.setDescription(request.getDescription());
        menu.setMenuType(request.getMenuType());
        menu.setIsActive(request.getIsActive() == null ? menu.getIsActive() : request.getIsActive());
        menu.setValidFrom(request.getValidFrom());
        menu.setValidTo(request.getValidTo());
        menu.setDisplayOrder(request.getDisplayOrder() == null ? menu.getDisplayOrder() : request.getDisplayOrder());
        menu.setCoverImage(request.getCoverImage());
        menu.setUpdatedBy(staffId);

        return toResponse(menuRepository.save(menu));
    }

    public MenuResponseDTO toggleMenuStatus(UUID restaurantId, UUID menuId, UUID staffId, boolean isActive) {
        Menu menu = requireMenu(restaurantId, menuId);
        menu.setIsActive(isActive);
        menu.setUpdatedBy(staffId);
        return toResponse(menuRepository.save(menu));
    }

    public MenuResponseDTO updateDisplayOrder(UUID restaurantId, UUID menuId, UUID staffId, int displayOrder) {
        Menu menu = requireMenu(restaurantId, menuId);
        menu.setDisplayOrder(displayOrder);
        menu.setUpdatedBy(staffId);
        return toResponse(menuRepository.save(menu));
    }

    public MenuResponseDTO updateMeta(UUID restaurantId, UUID menuId, UUID staffId, Map<String, Object> meta) {
        Menu menu = requireMenu(restaurantId, menuId);
        menu.setMeta(meta);
        menu.setUpdatedBy(staffId);
        return toResponse(menuRepository.save(menu));
    }

    public void deleteMenu(UUID restaurantId, UUID menuId, UUID staffId) {
        Menu menu = requireMenu(restaurantId, menuId);
        menu.setDeleted(DELETED);
        menu.setIsActive(false);
        menu.setUpdatedBy(staffId);
        menuRepository.save(menu);
    }

    @Transactional(readOnly = true)
    public List<MenuResponseDTO> getCurrentlyValidMenus(UUID restaurantId) {
        return menuRepository.findCurrentlyValidMenus(restaurantId, OffsetDateTime.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MenuResponseDTO> getSeasonalMenus(UUID restaurantId) {
        return menuRepository.findSeasonalMenus(restaurantId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MenuResponseDTO> getExpiredMenus(UUID restaurantId) {
        return menuRepository.findExpiredMenus(restaurantId, OffsetDateTime.now()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MenuResponseDTO> getUpcomingMenus(UUID restaurantId) {
        return menuRepository.findUpcomingMenus(restaurantId, OffsetDateTime.now()).stream().map(this::toResponse).toList();
    }

    private Menu requireMenu(UUID restaurantId, UUID menuId) {
        return menuRepository.findByIdAndRestaurantIdAndDeleted(menuId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", menuId));
    }

    private void validateUniqueName(UUID restaurantId, String name, UUID excludeId) {
        boolean exists = excludeId == null
                ? menuRepository.existsByRestaurantIdAndNameAndDeleted(restaurantId, name, NOT_DELETED)
                : menuRepository.existsByRestaurantIdAndNameAndDeletedAndIdNot(restaurantId, name, NOT_DELETED, excludeId);
        if (exists) {
            throw new DuplicateResourceException("Menu with name '" + name + "' already exists for this restaurant");
        }
    }

    private int defaultDisplayOrder(UUID restaurantId, Integer requestedDisplayOrder) {
        if (requestedDisplayOrder != null && requestedDisplayOrder > 0) {
            return requestedDisplayOrder;
        }
        return menuRepository.getMaxDisplayOrder(restaurantId) + 1;
    }

    private MenuResponseDTO toResponse(Menu menu) {
        int totalCategories = (int) menuCategoryRepository.countByMenuIdAndDeleted(menu.getId(), NOT_DELETED);
        int totalItems = (int) menuItemRepository.countByMenuId(menu.getId());
        return mapper.toMenuResponse(menu, totalCategories, totalItems);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
