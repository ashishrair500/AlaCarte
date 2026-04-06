package com.alaCarte.menu.service;

import com.alaCarte.entity.Combo;
import com.alaCarte.entity.ComboItem;
import com.alaCarte.entity.Menu;
import com.alaCarte.entity.MenuCategory;
import com.alaCarte.entity.MenuItem;
import com.alaCarte.entity.MenuItemAddon;
import com.alaCarte.entity.MenuItemAddonGroup;
import com.alaCarte.entity.MenuItemVariant;
import com.alaCarte.entity.Restaurant;
import com.alaCarte.exception.ResourceNotFoundException;
import com.alaCarte.menu.repository.ComboItemRepository;
import com.alaCarte.menu.repository.ComboRepository;
import com.alaCarte.menu.repository.MenuCategoryRepository;
import com.alaCarte.menu.repository.MenuItemAddonGroupMapRepository;
import com.alaCarte.menu.repository.MenuItemAddonGroupRepository;
import com.alaCarte.menu.repository.MenuItemAddonRepository;
import com.alaCarte.menu.repository.MenuItemVariantRepository;
import com.alaCarte.repository.MenuItemRepository;
import com.alaCarte.repository.MenuRepository;
import com.alaCarte.repository.RestaurantRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MenuModuleSupportService {

    private static final short NOT_DELETED = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemVariantRepository menuItemVariantRepository;
    private final MenuItemAddonGroupRepository addonGroupRepository;
    private final MenuItemAddonRepository addonRepository;
    private final MenuItemAddonGroupMapRepository addonGroupMapRepository;
    private final ComboRepository comboRepository;
    private final ComboItemRepository comboItemRepository;

    public MenuModuleSupportService(
            RestaurantRepository restaurantRepository,
            MenuRepository menuRepository,
            MenuItemRepository menuItemRepository,
            MenuCategoryRepository menuCategoryRepository,
            MenuItemVariantRepository menuItemVariantRepository,
            MenuItemAddonGroupRepository addonGroupRepository,
            MenuItemAddonRepository addonRepository,
            MenuItemAddonGroupMapRepository addonGroupMapRepository,
            ComboRepository comboRepository,
            ComboItemRepository comboItemRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.menuRepository = menuRepository;
        this.menuItemRepository = menuItemRepository;
        this.menuCategoryRepository = menuCategoryRepository;
        this.menuItemVariantRepository = menuItemVariantRepository;
        this.addonGroupRepository = addonGroupRepository;
        this.addonRepository = addonRepository;
        this.addonGroupMapRepository = addonGroupMapRepository;
        this.comboRepository = comboRepository;
        this.comboItemRepository = comboItemRepository;
    }

    public Restaurant requireRestaurant(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", restaurantId));
    }

    public Menu requireMenu(UUID restaurantId, UUID menuId) {
        return menuRepository.findByIdAndRestaurantIdAndDeleted(menuId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", menuId));
    }

    public MenuItem requireMenuItem(UUID restaurantId, UUID itemId) {
        return menuItemRepository.findByIdAndRestaurantIdAndDeleted(itemId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));
    }

    public MenuItem requireMenuItem(UUID restaurantId, UUID menuId, UUID itemId) {
        return menuItemRepository.findByIdAndMenuIdAndRestaurantIdAndDeleted(itemId, menuId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));
    }

    public MenuCategory requireCategory(UUID restaurantId, UUID menuId, UUID categoryId) {
        MenuCategory category = menuCategoryRepository.findByIdAndRestaurantIdAndDeleted(categoryId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", categoryId));

        if (!menuId.equals(category.getMenu().getId())) {
            throw new AccessDeniedException("MenuCategory does not belong to this menu");
        }
        return category;
    }

    public MenuCategory requireCategory(UUID restaurantId, UUID categoryId) {
        return menuCategoryRepository.findByIdAndRestaurantIdAndDeleted(categoryId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", categoryId));
    }

    public MenuItemVariant requireVariant(UUID restaurantId, UUID itemId, UUID variantId) {
        return menuItemVariantRepository.findByIdAndMenuItemIdAndRestaurantIdAndDeleted(variantId, itemId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItemVariant", variantId));
    }

    public MenuItemAddonGroup requireAddonGroup(UUID restaurantId, UUID groupId) {
        return addonGroupRepository.findByIdAndRestaurantIdAndDeleted(groupId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItemAddonGroup", groupId));
    }

    public MenuItemAddon requireAddon(UUID restaurantId, UUID groupId, UUID addonId) {
        return addonRepository.findByIdAndAddonGroupIdAndRestaurantIdAndDeleted(addonId, groupId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItemAddon", addonId));
    }

    public Combo requireCombo(UUID restaurantId, UUID menuId, UUID comboId) {
        return comboRepository.findByIdAndMenuIdAndRestaurantIdAndDeleted(comboId, menuId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Combo", comboId));
    }

    public ComboItem requireComboItem(UUID comboId, UUID slotId) {
        return comboItemRepository.findByIdAndComboIdAndDeleted(slotId, comboId, NOT_DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("ComboItem", slotId));
    }

    public Pageable normalizePageable(Pageable pageable, String defaultSortField) {
        int page = pageable == null ? 0 : Math.max(pageable.getPageNumber(), 0);
        int size = pageable == null ? DEFAULT_PAGE_SIZE : pageable.getPageSize();
        if (size <= 0) {
            size = DEFAULT_PAGE_SIZE;
        }
        size = Math.min(size, MAX_PAGE_SIZE);

        Sort sort = pageable == null || pageable.getSort().isUnsorted()
                ? Sort.by(defaultSortField).ascending()
                : pageable.getSort();

        return PageRequest.of(page, size, sort);
    }

    @Transactional
    public void refreshHasVariants(UUID itemId) {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));
        int count = menuItemVariantRepository.countByMenuItemIdAndDeleted(itemId, NOT_DELETED);
        menuItem.setHasVariants(count > 0);
        menuItemRepository.save(menuItem);
    }

    @Transactional
    public void refreshHasAddons(UUID itemId) {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));
        int count = addonGroupMapRepository.countByMenuItemIdAndDeleted(itemId, NOT_DELETED);
        menuItem.setHasAddons(count > 0);
        menuItemRepository.save(menuItem);
    }
}
