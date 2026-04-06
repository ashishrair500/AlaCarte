package com.alaCarte.menu.service;

import com.alaCarte.entity.MenuItemAddonGroup;
import com.alaCarte.menu.dto.request.AddonGroupRequestDTO;
import com.alaCarte.menu.dto.response.AddonGroupDetailResponseDTO;
import com.alaCarte.menu.dto.response.AddonGroupResponseDTO;
import com.alaCarte.menu.repository.MenuItemAddonGroupRepository;
import com.alaCarte.menu.repository.MenuItemAddonRepository;
import com.alaCarte.menu.util.MenuModuleMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AddonGroupService {

    private static final short DELETED = 1;
    private static final short NOT_DELETED = 0;

    private final MenuItemAddonGroupRepository addonGroupRepository;
    private final MenuItemAddonRepository addonRepository;
    private final MenuModuleSupportService supportService;
    private final MenuModuleMapper mapper;

    public AddonGroupService(
            MenuItemAddonGroupRepository addonGroupRepository,
            MenuItemAddonRepository addonRepository,
            MenuModuleSupportService supportService,
            MenuModuleMapper mapper
    ) {
        this.addonGroupRepository = addonGroupRepository;
        this.addonRepository = addonRepository;
        this.supportService = supportService;
        this.mapper = mapper;
    }

    public AddonGroupResponseDTO createGroup(UUID restaurantId, UUID staffId, AddonGroupRequestDTO request) {
        MenuItemAddonGroup group = new MenuItemAddonGroup();
        group.setRestaurant(supportService.requireRestaurant(restaurantId));
        applyRequest(group, request);
        group.setCreatedBy(staffId);
        return toResponse(addonGroupRepository.save(group));
    }

    @Transactional(readOnly = true)
    public Page<AddonGroupResponseDTO> getGroups(UUID restaurantId, Pageable pageable) {
        Pageable normalized = supportService.normalizePageable(pageable, "displayOrder");
        return addonGroupRepository.findByRestaurantIdAndDeleted(restaurantId, NOT_DELETED, normalized)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AddonGroupDetailResponseDTO getGroupDetail(UUID restaurantId, UUID groupId) {
        MenuItemAddonGroup group = supportService.requireAddonGroup(restaurantId, groupId);
        return mapper.toAddonGroupDetailResponse(
                group,
                addonRepository.findByAddonGroupIdAndDeletedOrderByDisplayOrderAsc(groupId, NOT_DELETED)
                        .stream()
                        .map(mapper::toAddonResponse)
                        .toList()
        );
    }

    public AddonGroupResponseDTO updateGroup(UUID restaurantId, UUID groupId, AddonGroupRequestDTO request) {
        MenuItemAddonGroup group = supportService.requireAddonGroup(restaurantId, groupId);
        applyRequest(group, request);
        return toResponse(addonGroupRepository.save(group));
    }

    public AddonGroupResponseDTO toggleGroupStatus(UUID restaurantId, UUID groupId, boolean isActive) {
        MenuItemAddonGroup group = supportService.requireAddonGroup(restaurantId, groupId);
        group.setIsActive(isActive);
        return toResponse(addonGroupRepository.save(group));
    }

    public void deleteGroup(UUID restaurantId, UUID groupId) {
        MenuItemAddonGroup group = supportService.requireAddonGroup(restaurantId, groupId);
        group.setDeleted(DELETED);
        addonGroupRepository.save(group);
    }

    private void applyRequest(MenuItemAddonGroup group, AddonGroupRequestDTO request) {
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setSelectionType(request.getSelectionType());
        group.setMinSelections((short) defaultNumber(request.getMinSelections()));
        group.setMaxSelections(request.getMaxSelections() == null ? null : request.getMaxSelections().shortValue());
        group.setDisplayOrder((short) defaultNumber(request.getDisplayOrder()));
    }

    private AddonGroupResponseDTO toResponse(MenuItemAddonGroup group) {
        Integer addonCount = addonRepository.countByAddonGroupIdAndDeleted(group.getId(), NOT_DELETED);
        return mapper.toAddonGroupResponse(group, addonCount == null ? 0 : addonCount);
    }

    private int defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }
}
