package com.alaCarte.menu.service;

import com.alaCarte.entity.Combo;
import com.alaCarte.menu.dto.request.ComboRequestDTO;
import com.alaCarte.menu.dto.response.ComboDetailResponseDTO;
import com.alaCarte.menu.dto.response.ComboItemResponseDTO;
import com.alaCarte.menu.dto.response.ComboResponseDTO;
import com.alaCarte.menu.repository.ComboItemRepository;
import com.alaCarte.menu.repository.ComboRepository;
import com.alaCarte.menu.util.MenuModuleMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ComboService {

    private static final short DELETED = 1;
    private static final short NOT_DELETED = 0;

    private final ComboRepository comboRepository;
    private final ComboItemRepository comboItemRepository;
    private final MenuModuleSupportService supportService;
    private final MenuModuleMapper mapper;

    public ComboService(
            ComboRepository comboRepository,
            ComboItemRepository comboItemRepository,
            MenuModuleSupportService supportService,
            MenuModuleMapper mapper
    ) {
        this.comboRepository = comboRepository;
        this.comboItemRepository = comboItemRepository;
        this.supportService = supportService;
        this.mapper = mapper;
    }

    public ComboResponseDTO createCombo(UUID restaurantId, UUID menuId, UUID staffId, ComboRequestDTO request) {
        Combo combo = new Combo();
        combo.setRestaurant(supportService.requireRestaurant(restaurantId));
        combo.setMenu(supportService.requireMenu(restaurantId, menuId));
        combo.setCreatedBy(staffId);
        combo.setUpdatedBy(staffId);
        applyRequest(combo, request);
        return toResponse(comboRepository.save(combo));
    }

    @Transactional(readOnly = true)
    public Page<ComboResponseDTO> getCombos(UUID restaurantId, UUID menuId, Pageable pageable) {
        supportService.requireMenu(restaurantId, menuId);
        Pageable normalized = supportService.normalizePageable(pageable, "displayOrder");
        return comboRepository.findByMenuIdAndRestaurantIdAndDeleted(menuId, restaurantId, NOT_DELETED, normalized)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ComboDetailResponseDTO getComboDetail(UUID restaurantId, UUID menuId, UUID comboId) {
        Combo combo = supportService.requireCombo(restaurantId, menuId, comboId);
        List<ComboItemResponseDTO> items = comboItemRepository.findByComboIdWithDetails(comboId)
                .stream()
                .map(mapper::toComboItemResponse)
                .toList();
        Integer slotCount = comboItemRepository.countByComboIdAndDeleted(comboId, NOT_DELETED);
        return mapper.toComboDetailResponse(combo, slotCount == null ? 0 : slotCount, items);
    }

    public ComboResponseDTO updateCombo(UUID restaurantId, UUID menuId, UUID comboId, ComboRequestDTO request) {
        Combo combo = supportService.requireCombo(restaurantId, menuId, comboId);
        applyRequest(combo, request);
        return toResponse(comboRepository.save(combo));
    }

    public ComboResponseDTO toggleAvailability(UUID restaurantId, UUID menuId, UUID comboId, boolean available) {
        Combo combo = supportService.requireCombo(restaurantId, menuId, comboId);
        combo.setIsAvailable(available);
        return toResponse(comboRepository.save(combo));
    }

    public void deleteCombo(UUID restaurantId, UUID menuId, UUID comboId) {
        Combo combo = supportService.requireCombo(restaurantId, menuId, comboId);
        combo.setDeleted(DELETED);
        comboRepository.save(combo);
    }

    private void applyRequest(Combo combo, ComboRequestDTO request) {
        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setAiDescription(request.getAiDescription());
        combo.setImageUrl(request.getImageUrl());
        combo.setComboPrice(request.getComboPrice());
        combo.setOriginalPrice(request.getOriginalPrice());
        combo.setDiscountPrice(request.getDiscountPrice());
        combo.setValidFrom(request.getValidFrom());
        combo.setValidTo(request.getValidTo());
        combo.setAvailableDays(request.getAvailableDays() == null ? null : request.getAvailableDays().toArray(String[]::new));
        combo.setMaxQuantityPerOrder(request.getMaxQuantityPerOrder() == null ? null : request.getMaxQuantityPerOrder().shortValue());
        combo.setBadge(request.getBadge());
        combo.setDisplayOrder((short) defaultNumber(request.getDisplayOrder()));
    }

    private ComboResponseDTO toResponse(Combo combo) {
        Integer slotCount = comboItemRepository.countByComboIdAndDeleted(combo.getId(), NOT_DELETED);
        return mapper.toComboResponse(combo, slotCount == null ? 0 : slotCount);
    }

    private int defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }
}
