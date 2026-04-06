package com.alaCarte.menu.service;

import com.alaCarte.entity.MenuItemAddon;
import com.alaCarte.entity.MenuItemAddonGroup;
import com.alaCarte.menu.dto.request.AddonRequestDTO;
import com.alaCarte.menu.dto.response.AddonResponseDTO;
import com.alaCarte.menu.repository.MenuItemAddonRepository;
import com.alaCarte.menu.util.MenuModuleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AddonService {

    private static final short DELETED = 1;
    private static final short NOT_DELETED = 0;

    private final MenuItemAddonRepository addonRepository;
    private final MenuModuleSupportService supportService;
    private final MenuModuleMapper mapper;

    public AddonService(
            MenuItemAddonRepository addonRepository,
            MenuModuleSupportService supportService,
            MenuModuleMapper mapper
    ) {
        this.addonRepository = addonRepository;
        this.supportService = supportService;
        this.mapper = mapper;
    }

    public AddonResponseDTO addAddon(UUID restaurantId, UUID groupId, AddonRequestDTO request) {
        MenuItemAddonGroup group = supportService.requireAddonGroup(restaurantId, groupId);
        MenuItemAddon addon = new MenuItemAddon();
        addon.setAddonGroup(group);
        addon.setRestaurant(group.getRestaurant());
        applyRequest(addon, request);
        return mapper.toAddonResponse(addonRepository.save(addon));
    }

    @Transactional(readOnly = true)
    public List<AddonResponseDTO> getAddons(UUID restaurantId, UUID groupId) {
        supportService.requireAddonGroup(restaurantId, groupId);
        return addonRepository.findByAddonGroupIdAndDeletedOrderByDisplayOrderAsc(groupId, NOT_DELETED)
                .stream()
                .map(mapper::toAddonResponse)
                .toList();
    }

    public AddonResponseDTO updateAddon(UUID restaurantId, UUID groupId, UUID addonId, AddonRequestDTO request) {
        MenuItemAddon addon = supportService.requireAddon(restaurantId, groupId, addonId);
        applyRequest(addon, request);
        return mapper.toAddonResponse(addonRepository.save(addon));
    }

    public AddonResponseDTO toggleAvailability(UUID restaurantId, UUID groupId, UUID addonId, boolean available) {
        MenuItemAddon addon = supportService.requireAddon(restaurantId, groupId, addonId);
        addon.setIsAvailable(available);
        return mapper.toAddonResponse(addonRepository.save(addon));
    }

    public void deleteAddon(UUID restaurantId, UUID groupId, UUID addonId) {
        MenuItemAddon addon = supportService.requireAddon(restaurantId, groupId, addonId);
        addon.setDeleted(DELETED);
        addonRepository.save(addon);
    }

    private void applyRequest(MenuItemAddon addon, AddonRequestDTO request) {
        addon.setName(request.getName());
        addon.setDescription(request.getDescription());
        addon.setPrice(request.getPrice());
        addon.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        addon.setDisplayOrder((short) defaultNumber(request.getDisplayOrder()));
    }

    private int defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }
}
