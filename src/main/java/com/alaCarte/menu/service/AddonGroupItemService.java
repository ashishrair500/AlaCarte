package com.alaCarte.menu.service;

import com.alaCarte.entity.MenuItem;
import com.alaCarte.entity.MenuItemAddonGroup;
import com.alaCarte.entity.MenuItemAddonGroupMap;
import com.alaCarte.exception.DuplicateResourceException;
import com.alaCarte.menu.dto.request.AddonGroupItemRequestDTO;
import com.alaCarte.menu.dto.response.AddonGroupDetailResponseDTO;
import com.alaCarte.menu.repository.MenuItemAddonGroupMapRepository;
import com.alaCarte.menu.repository.MenuItemAddonRepository;
import com.alaCarte.menu.util.MenuModuleMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AddonGroupItemService {

    private static final short DELETED = 1;
    private static final short NOT_DELETED = 0;

    private final MenuItemAddonGroupMapRepository addonGroupMapRepository;
    private final MenuItemAddonRepository addonRepository;
    private final MenuModuleSupportService supportService;
    private final MenuModuleMapper mapper;

    public AddonGroupItemService(
            MenuItemAddonGroupMapRepository addonGroupMapRepository,
            MenuItemAddonRepository addonRepository,
            MenuModuleSupportService supportService,
            MenuModuleMapper mapper
    ) {
        this.addonGroupMapRepository = addonGroupMapRepository;
        this.addonRepository = addonRepository;
        this.supportService = supportService;
        this.mapper = mapper;
    }

    public void linkGroupToItem(UUID restaurantId, UUID itemId, AddonGroupItemRequestDTO request) {
        MenuItem menuItem = supportService.requireMenuItem(restaurantId, itemId);
        MenuItemAddonGroup addonGroup = supportService.requireAddonGroup(restaurantId, request.getAddonGroupId());

        if (addonGroupMapRepository.existsByMenuItemIdAndAddonGroupIdAndDeleted(itemId, addonGroup.getId(), NOT_DELETED)) {
            throw new DuplicateResourceException("Addon group is already linked to this item");
        }

        MenuItemAddonGroupMap map = new MenuItemAddonGroupMap();
        map.setMenuItem(menuItem);
        map.setAddonGroup(addonGroup);
        map.setDisplayOrder((short) defaultNumber(request.getDisplayOrder()));

        try {
            addonGroupMapRepository.save(map);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("Addon group is already linked to this item");
        }

        supportService.refreshHasAddons(itemId);
    }

    @Transactional(readOnly = true)
    public List<AddonGroupDetailResponseDTO> getGroupsForItem(UUID restaurantId, UUID itemId) {
        supportService.requireMenuItem(restaurantId, itemId);
        return addonGroupMapRepository.findByMenuItemIdWithGroup(itemId)
                .stream()
                .map(map -> mapper.toAddonGroupDetailResponse(
                        map.getAddonGroup(),
                        addonRepository.findByAddonGroupIdAndDeletedOrderByDisplayOrderAsc(map.getAddonGroup().getId(), NOT_DELETED)
                                .stream()
                                .map(mapper::toAddonResponse)
                                .toList()
                ))
                .toList();
    }

    public void updateDisplayOrder(UUID restaurantId, UUID itemId, UUID groupId, int order) {
        supportService.requireMenuItem(restaurantId, itemId);
        supportService.requireAddonGroup(restaurantId, groupId);
        MenuItemAddonGroupMap map = addonGroupMapRepository.findByMenuItemIdAndAddonGroupIdAndDeleted(itemId, groupId, NOT_DELETED)
                .orElseThrow(() -> new com.alaCarte.exception.ResourceNotFoundException(
                        "Addon group link not found for itemId: " + itemId + " and groupId: " + groupId
                ));
        map.setDisplayOrder((short) order);
        addonGroupMapRepository.save(map);
    }

    public void unlinkGroupFromItem(UUID restaurantId, UUID itemId, UUID groupId) {
        supportService.requireMenuItem(restaurantId, itemId);
        supportService.requireAddonGroup(restaurantId, groupId);
        MenuItemAddonGroupMap map = addonGroupMapRepository.findByMenuItemIdAndAddonGroupIdAndDeleted(itemId, groupId, NOT_DELETED)
                .orElseThrow(() -> new com.alaCarte.exception.ResourceNotFoundException(
                        "Addon group link not found for itemId: " + itemId + " and groupId: " + groupId
                ));
        map.setDeleted(DELETED);
        addonGroupMapRepository.save(map);
        supportService.refreshHasAddons(itemId);
    }

    private int defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }
}
