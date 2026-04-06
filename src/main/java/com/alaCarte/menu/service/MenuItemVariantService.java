package com.alaCarte.menu.service;

import com.alaCarte.entity.MenuItem;
import com.alaCarte.entity.MenuItemVariant;
import com.alaCarte.menu.dto.request.MenuItemVariantRequestDTO;
import com.alaCarte.menu.dto.response.MenuItemVariantResponseDTO;
import com.alaCarte.menu.repository.MenuItemVariantRepository;
import com.alaCarte.menu.util.MenuModuleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MenuItemVariantService {

    private static final short DELETED = 1;
    private static final short NOT_DELETED = 0;

    private final MenuItemVariantRepository menuItemVariantRepository;
    private final MenuModuleSupportService supportService;
    private final MenuModuleMapper mapper;

    public MenuItemVariantService(
            MenuItemVariantRepository menuItemVariantRepository,
            MenuModuleSupportService supportService,
            MenuModuleMapper mapper
    ) {
        this.menuItemVariantRepository = menuItemVariantRepository;
        this.supportService = supportService;
        this.mapper = mapper;
    }

    public MenuItemVariantResponseDTO addVariant(UUID restaurantId, UUID itemId, MenuItemVariantRequestDTO request) {
        MenuItem menuItem = supportService.requireMenuItem(restaurantId, itemId);
        MenuItemVariant variant = new MenuItemVariant();
        variant.setMenuItem(menuItem);
        variant.setRestaurant(menuItem.getRestaurant());
        applyRequest(variant, request);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            menuItemVariantRepository.clearDefaultByMenuItemId(itemId);
            variant.setIsDefault(true);
        }

        MenuItemVariant saved = menuItemVariantRepository.save(variant);
        supportService.refreshHasVariants(itemId);
        return mapper.toVariantResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MenuItemVariantResponseDTO> getVariants(UUID restaurantId, UUID itemId) {
        supportService.requireMenuItem(restaurantId, itemId);
        return menuItemVariantRepository.findByMenuItemIdAndDeletedOrderByDisplayOrderAsc(itemId, NOT_DELETED)
                .stream()
                .map(mapper::toVariantResponse)
                .toList();
    }

    public MenuItemVariantResponseDTO updateVariant(
            UUID restaurantId,
            UUID itemId,
            UUID variantId,
            MenuItemVariantRequestDTO request
    ) {
        MenuItemVariant variant = supportService.requireVariant(restaurantId, itemId, variantId);
        applyRequest(variant, request);
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            menuItemVariantRepository.clearDefaultByMenuItemId(itemId);
            variant.setIsDefault(true);
        }
        return mapper.toVariantResponse(menuItemVariantRepository.save(variant));
    }

    public MenuItemVariantResponseDTO setDefault(UUID restaurantId, UUID itemId, UUID variantId) {
        MenuItemVariant variant = supportService.requireVariant(restaurantId, itemId, variantId);
        menuItemVariantRepository.clearDefaultByMenuItemId(itemId);
        variant.setIsDefault(true);
        return mapper.toVariantResponse(menuItemVariantRepository.save(variant));
    }

    public MenuItemVariantResponseDTO toggleAvailability(
            UUID restaurantId,
            UUID itemId,
            UUID variantId,
            boolean available
    ) {
        MenuItemVariant variant = supportService.requireVariant(restaurantId, itemId, variantId);
        variant.setIsAvailable(available);
        return mapper.toVariantResponse(menuItemVariantRepository.save(variant));
    }

    public void deleteVariant(UUID restaurantId, UUID itemId, UUID variantId) {
        MenuItemVariant variant = supportService.requireVariant(restaurantId, itemId, variantId);
        variant.setDeleted(DELETED);
        menuItemVariantRepository.save(variant);
        supportService.refreshHasVariants(itemId);
    }

    private void applyRequest(MenuItemVariant variant, MenuItemVariantRequestDTO request) {
        variant.setName(request.getName());
        variant.setVariantType(request.getVariantType());
        variant.setDescription(request.getDescription());
        variant.setPrice(request.getPrice());
        variant.setDiscountPrice(request.getDiscountPrice());
        variant.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        variant.setDisplayOrder((short) defaultNumber(request.getDisplayOrder()));
    }

    private int defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }
}
