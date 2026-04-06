package com.alaCarte.service;

import com.alaCarte.entity.Menu;
import com.alaCarte.entity.MenuCategory;
import com.alaCarte.entity.MenuItem;
import com.alaCarte.exception.DuplicateResourceException;
import com.alaCarte.exception.ResourceNotFoundException;
import com.alaCarte.menu.dto.request.MenuItemFilterParams;
import com.alaCarte.menu.dto.request.MenuItemRequestDTO;
import com.alaCarte.menu.dto.response.AddonGroupDetailResponseDTO;
import com.alaCarte.menu.dto.response.MenuItemDetailResponseDTO;
import com.alaCarte.menu.dto.response.MenuItemResponseDTO;
import com.alaCarte.menu.dto.response.TagResponseDTO;
import com.alaCarte.menu.repository.MenuCategoryRepository;
import com.alaCarte.menu.repository.MenuItemAddonGroupMapRepository;
import com.alaCarte.menu.repository.MenuItemAddonRepository;
import com.alaCarte.menu.repository.MenuItemTagMapRepository;
import com.alaCarte.menu.repository.MenuItemVariantRepository;
import com.alaCarte.menu.service.MenuModuleSupportService;
import com.alaCarte.menu.util.MenuModuleMapper;
import com.alaCarte.repository.MenuItemRepository;
import com.alaCarte.repository.MenuRepository;
import com.alaCarte.repository.RestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class MenuItemService {

    private static final short NOT_DELETED = 0;
    private static final short DELETED = 1;

    private final MenuItemRepository menuItemRepository;
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemVariantRepository variantRepository;
    private final MenuItemAddonGroupMapRepository addonGroupMapRepository;
    private final MenuItemAddonRepository addonRepository;
    private final MenuItemTagMapRepository tagMapRepository;
    private final MenuModuleSupportService supportService;
    private final MenuModuleMapper mapper;

    public MenuItemService(
            MenuItemRepository menuItemRepository,
            MenuRepository menuRepository,
            RestaurantRepository restaurantRepository,
            MenuCategoryRepository menuCategoryRepository,
            MenuItemVariantRepository variantRepository,
            MenuItemAddonGroupMapRepository addonGroupMapRepository,
            MenuItemAddonRepository addonRepository,
            MenuItemTagMapRepository tagMapRepository,
            MenuModuleSupportService supportService,
            MenuModuleMapper mapper
    ) {
        this.menuItemRepository = menuItemRepository;
        this.menuRepository = menuRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuCategoryRepository = menuCategoryRepository;
        this.variantRepository = variantRepository;
        this.addonGroupMapRepository = addonGroupMapRepository;
        this.addonRepository = addonRepository;
        this.tagMapRepository = tagMapRepository;
        this.supportService = supportService;
        this.mapper = mapper;
    }

    public MenuItemResponseDTO createItem(UUID restaurantId, UUID menuId, UUID staffId, MenuItemRequestDTO request) {
        MenuItem menuItem = new MenuItem();
        menuItem.setRestaurant(restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", restaurantId)));
        menuItem.setMenu(requireMenu(restaurantId, menuId));
        applyRequest(menuItem, restaurantId, menuId, request);
        menuItem.setCreatedBy(staffId);
        menuItem.setUpdatedBy(staffId);

        validateUniqueName(menuId, request.getName(), null);
        menuItem.setDisplayOrder(defaultDisplayOrder(menuId, request.getDisplayOrder()));

        return mapper.toMenuItemResponse(menuItemRepository.save(menuItem));
    }

    public List<MenuItemResponseDTO> createItemsBulk(
            UUID restaurantId,
            UUID menuId,
            UUID staffId,
            List<MenuItemRequestDTO> requests
    ) {
        if (requests == null || requests.isEmpty()) {
            throw new com.alaCarte.exception.ValidationException("At least one menu item is required for bulk upload");
        }

        Menu menu = requireMenu(restaurantId, menuId);
        var restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", restaurantId));

        Set<String> namesInRequest = new HashSet<>();
        short nextDisplayOrder = menuItemRepository.getMaxDisplayOrder(menuId);
        List<MenuItem> items = new java.util.ArrayList<>();
        for (MenuItemRequestDTO request : requests) {
            String normalizedName = request.getName().trim().toLowerCase();
            if (!namesInRequest.add(normalizedName)) {
                throw new DuplicateResourceException("Duplicate menu item name '" + request.getName() + "' found in bulk upload");
            }
            validateUniqueName(menuId, request.getName(), null);

            MenuItem menuItem = new MenuItem();
            menuItem.setRestaurant(restaurant);
            menuItem.setMenu(menu);
            applyRequest(menuItem, restaurantId, menuId, request);
            menuItem.setCreatedBy(staffId);
            menuItem.setUpdatedBy(staffId);
            if (request.getDisplayOrder() == null || request.getDisplayOrder() <= 0) {
                nextDisplayOrder++;
                menuItem.setDisplayOrder(nextDisplayOrder);
            }
            items.add(menuItem);
        }

        return menuItemRepository.saveAll(items).stream().map(mapper::toMenuItemResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponseDTO> getItems(
            UUID restaurantId,
            UUID menuId,
            MenuItemFilterParams filters,
            Pageable pageable
    ) {
        requireMenu(restaurantId, menuId);
        Pageable normalized = supportService.normalizePageable(pageable, "displayOrder");
        return menuItemRepository.findAllWithFilters(
                        menuId,
                        filters.getCategoryId(),
                        filters.getIsAvailable(),
                        filters.getIsVeg(),
                        normalize(filters.getSearch()),
                        filters.getMinPrice(),
                        filters.getMaxPrice(),
                        normalize(filters.getBadge()),
                        normalize(filters.getTag()),
                        filters.getHasVariants(),
                        filters.getHasAddons(),
                        normalized
                )
                .map(mapper::toMenuItemResponse);
    }

    @Transactional(readOnly = true)
    public MenuItemDetailResponseDTO getItemDetail(UUID restaurantId, UUID menuId, UUID itemId) {
        MenuItem menuItem = menuItemRepository.findByIdAndMenuIdAndRestaurantIdAndDeleted(itemId, menuId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));

        var variants = variantRepository.findByMenuItemIdAndDeletedOrderByDisplayOrderAsc(itemId, NOT_DELETED)
                .stream()
                .map(mapper::toVariantResponse)
                .toList();

        var addonGroups = addonGroupMapRepository.findByMenuItemIdWithGroup(itemId)
                .stream()
                .map(map -> mapper.toAddonGroupDetailResponse(
                        map.getAddonGroup(),
                        addonRepository.findByAddonGroupIdAndDeletedOrderByDisplayOrderAsc(map.getAddonGroup().getId(), NOT_DELETED)
                                .stream()
                                .map(mapper::toAddonResponse)
                                .toList()
                ))
                .toList();

        List<TagResponseDTO> tags = tagMapRepository.findByMenuItemIdWithTag(itemId)
                .stream()
                .map(map -> mapper.toTagResponse(map.getTag()))
                .toList();

        return mapper.toMenuItemDetailResponse(menuItem, variants, addonGroups, tags);
    }

    public MenuItemResponseDTO updateItem(
            UUID restaurantId,
            UUID menuId,
            UUID itemId,
            UUID staffId,
            MenuItemRequestDTO request
    ) {
        MenuItem menuItem = requireMenuItem(restaurantId, menuId, itemId);
        validateUniqueName(menuId, request.getName(), itemId);
        applyRequest(menuItem, restaurantId, menuId, request);
        menuItem.setUpdatedBy(staffId);
        if (request.getDisplayOrder() == null || request.getDisplayOrder() <= 0) {
            menuItem.setDisplayOrder(menuItem.getDisplayOrder() == null ? defaultDisplayOrder(menuId, null) : menuItem.getDisplayOrder());
        }
        return mapper.toMenuItemResponse(menuItemRepository.save(menuItem));
    }

    public MenuItemResponseDTO toggleAvailability(
            UUID restaurantId,
            UUID menuId,
            UUID itemId,
            boolean available
    ) {
        MenuItem menuItem = requireMenuItem(restaurantId, menuId, itemId);
        menuItem.setIsAvailable(available);
        return mapper.toMenuItemResponse(menuItemRepository.save(menuItem));
    }

    public MenuItemResponseDTO reassignCategory(
            UUID restaurantId,
            UUID menuId,
            UUID itemId,
            UUID categoryId
    ) {
        MenuItem menuItem = requireMenuItem(restaurantId, menuId, itemId);
        menuItem.setCategory(resolveCategory(restaurantId, menuId, categoryId));
        return mapper.toMenuItemResponse(menuItemRepository.save(menuItem));
    }

    public MenuItemResponseDTO updateDisplayOrder(
            UUID restaurantId,
            UUID menuId,
            UUID itemId,
            int displayOrder
    ) {
        MenuItem menuItem = requireMenuItem(restaurantId, menuId, itemId);
        menuItem.setDisplayOrder((short) displayOrder);
        return mapper.toMenuItemResponse(menuItemRepository.save(menuItem));
    }

    public MenuItemResponseDTO updateNutrition(
            UUID restaurantId,
            UUID menuId,
            UUID itemId,
            Map<String, Object> nutrition
    ) {
        MenuItem menuItem = requireMenuItem(restaurantId, menuId, itemId);
        menuItem.setNutrition(nutrition);
        return mapper.toMenuItemResponse(menuItemRepository.save(menuItem));
    }

    public MenuItemResponseDTO restoreItem(UUID restaurantId, UUID menuId, UUID itemId, UUID staffId) {
        requireMenu(restaurantId, menuId);
        MenuItem menuItem = menuItemRepository.findDeletedByIdAndMenuIdAndRestaurantId(itemId, menuId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));
        menuItem.setDeleted(NOT_DELETED);
        menuItem.setUpdatedBy(staffId);
        return mapper.toMenuItemResponse(menuItemRepository.save(menuItem));
    }

    public void deleteItem(UUID restaurantId, UUID menuId, UUID itemId, UUID staffId) {
        MenuItem menuItem = requireMenuItem(restaurantId, menuId, itemId);
        menuItem.setDeleted(DELETED);
        menuItem.setIsAvailable(false);
        menuItem.setUpdatedBy(staffId);
        menuItemRepository.save(menuItem);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getItemsWithNutrition(UUID restaurantId, UUID menuId) {
        requireMenu(restaurantId, menuId);
        return menuItemRepository.findItemsWithNutrition(menuId).stream().map(mapper::toMenuItemResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getItemsWithVariants(UUID restaurantId, UUID menuId) {
        requireMenu(restaurantId, menuId);
        return menuItemRepository.findItemsWithVariants(menuId).stream().map(mapper::toMenuItemResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getItemsWithAddons(UUID restaurantId, UUID menuId) {
        requireMenu(restaurantId, menuId);
        return menuItemRepository.findItemsWithAddons(menuId).stream().map(mapper::toMenuItemResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getPopularItems(UUID restaurantId, UUID menuId) {
        requireMenu(restaurantId, menuId);
        return menuItemRepository.findPopularItems(menuId).stream().map(mapper::toMenuItemResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getNewItems(UUID restaurantId, UUID menuId) {
        requireMenu(restaurantId, menuId);
        return menuItemRepository.findNewItems(menuId).stream().map(mapper::toMenuItemResponse).toList();
    }

    public void refreshHasVariants(UUID itemId) {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));
        int count = variantRepository.countByMenuItemIdAndDeleted(itemId, NOT_DELETED);
        menuItem.setHasVariants(count > 0);
        menuItemRepository.save(menuItem);
    }

    public void refreshHasAddons(UUID itemId) {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));
        int count = addonGroupMapRepository.countByMenuItemIdAndDeleted(itemId, NOT_DELETED);
        menuItem.setHasAddons(count > 0);
        menuItemRepository.save(menuItem);
    }

    private Menu requireMenu(UUID restaurantId, UUID menuId) {
        return menuRepository.findByIdAndRestaurantIdAndDeleted(menuId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", menuId));
    }

    private MenuItem requireMenuItem(UUID restaurantId, UUID menuId, UUID itemId) {
        return menuItemRepository.findByIdAndMenuIdAndRestaurantIdAndDeleted(itemId, menuId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", itemId));
    }

    private MenuCategory resolveCategory(UUID restaurantId, UUID menuId, UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return menuCategoryRepository.findByIdAndRestaurantIdAndDeleted(categoryId, restaurantId, NOT_DELETED)
                .filter(category -> menuId.equals(category.getMenu().getId()))
                .orElseThrow(() -> new ResourceNotFoundException("MenuCategory", categoryId));
    }

    private void applyRequest(MenuItem menuItem, UUID restaurantId, UUID menuId, MenuItemRequestDTO request) {
        menuItem.setCategory(resolveCategory(restaurantId, menuId, request.getCategoryId()));
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setAiDescription(request.getAiDescription());
        menuItem.setBasePrice(request.getBasePrice());
        menuItem.setDiscountPrice(request.getDiscountPrice());
        menuItem.setIsVeg(request.getIsVeg());
        menuItem.setIsVegan(request.getIsVegan());
        menuItem.setIsSpicy(request.getIsSpicy());
        menuItem.setContainsEgg(request.getContainsEgg());
        menuItem.setContainsGluten(request.getContainsGluten());
        menuItem.setContainsNuts(request.getContainsNuts());
        menuItem.setContainsDairy(request.getContainsDairy());
        menuItem.setSpiceLevel(request.getSpiceLevel() == null ? null : request.getSpiceLevel().shortValue());
        menuItem.setPrepTimeMinutes(request.getPrepTimeMinutes() == null ? null : request.getPrepTimeMinutes().shortValue());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setGallery(request.getGallery() == null ? null : request.getGallery().toArray(String[]::new));
        menuItem.setIngredients(request.getIngredients() == null ? null : request.getIngredients().toArray(String[]::new));
        menuItem.setCuisineType(request.getCuisineType());
        menuItem.setCourseType(request.getCourseType());
        menuItem.setDisplayOrder(request.getDisplayOrder() == null ? menuItem.getDisplayOrder() : request.getDisplayOrder().shortValue());
        menuItem.setBadge(request.getBadge());
        menuItem.setNutrition(request.getNutrition());
        menuItem.setMeta(request.getMeta());
    }

    private void validateUniqueName(UUID menuId, String name, UUID excludeId) {
        if (menuItemRepository.existsByMenuIdAndName(menuId, name, excludeId)) {
            throw new DuplicateResourceException("Menu item with name '" + name + "' already exists in this menu");
        }
    }

    private short defaultDisplayOrder(UUID menuId, Integer requestedDisplayOrder) {
        if (requestedDisplayOrder != null && requestedDisplayOrder > 0) {
            return requestedDisplayOrder.shortValue();
        }
        return (short) (menuItemRepository.getMaxDisplayOrder(menuId) + 1);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
