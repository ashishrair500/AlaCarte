package com.alaCarte.menu.service;

import com.alaCarte.entity.Combo;
import com.alaCarte.entity.ComboItem;
import com.alaCarte.entity.MenuCategory;
import com.alaCarte.entity.MenuItem;
import com.alaCarte.entity.MenuItemVariant;
import com.alaCarte.exception.ValidationException;
import com.alaCarte.menu.dto.request.ComboItemRequestDTO;
import com.alaCarte.menu.dto.response.ComboItemResponseDTO;
import com.alaCarte.menu.repository.ComboItemRepository;
import com.alaCarte.menu.repository.ComboRepository;
import com.alaCarte.menu.util.MenuModuleMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ComboItemService {

    private static final short DELETED = 1;
    private static final short NOT_DELETED = 0;

    private final ComboRepository comboRepository;
    private final ComboItemRepository comboItemRepository;
    private final MenuModuleSupportService supportService;
    private final MenuModuleMapper mapper;

    public ComboItemService(
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

    public ComboItemResponseDTO addSlot(UUID restaurantId, UUID comboId, ComboItemRequestDTO request) {
        Combo combo = requireCombo(restaurantId, comboId);
        ComboItem slot = new ComboItem();
        slot.setCombo(combo);
        applyRequest(restaurantId, slot, request);
        return mapper.toComboItemResponse(comboItemRepository.save(slot));
    }

    @Transactional(readOnly = true)
    public List<ComboItemResponseDTO> getSlots(UUID restaurantId, UUID comboId) {
        requireCombo(restaurantId, comboId);
        return comboItemRepository.findByComboIdWithDetails(comboId)
                .stream()
                .map(mapper::toComboItemResponse)
                .toList();
    }

    public ComboItemResponseDTO updateSlot(UUID restaurantId, UUID comboId, UUID slotId, ComboItemRequestDTO request) {
        requireCombo(restaurantId, comboId);
        ComboItem slot = supportService.requireComboItem(comboId, slotId);
        applyRequest(restaurantId, slot, request);
        return mapper.toComboItemResponse(comboItemRepository.save(slot));
    }

    public void deleteSlot(UUID restaurantId, UUID comboId, UUID slotId) {
        requireCombo(restaurantId, comboId);
        ComboItem slot = supportService.requireComboItem(comboId, slotId);
        slot.setDeleted(DELETED);
        comboItemRepository.save(slot);
    }

    private Combo requireCombo(UUID restaurantId, UUID comboId) {
        Combo combo = comboRepository.findById(comboId)
                .orElseThrow(() -> new com.alaCarte.exception.ResourceNotFoundException("Combo", comboId));
        if (!restaurantId.equals(combo.getRestaurant().getId())) {
            throw new AccessDeniedException("Combo does not belong to this restaurant");
        }
        return combo;
    }

    private void applyRequest(UUID restaurantId, ComboItem slot, ComboItemRequestDTO request) {
        MenuItem menuItem = supportService.requireMenuItem(restaurantId, request.getMenuItemId());
        MenuItemVariant variant = request.getVariantId() == null
                ? null
                : supportService.requireVariant(restaurantId, request.getMenuItemId(), request.getVariantId());

        boolean substitutable = Boolean.TRUE.equals(request.getIsSubstitutable());
        if (substitutable && request.getSubstitutionCategoryId() == null) {
            throw new ValidationException("substitutionCategoryId is required when isSubstitutable is true");
        }

        MenuCategory substitutionCategory = request.getSubstitutionCategoryId() == null
                ? null
                : supportService.requireCategory(restaurantId, request.getSubstitutionCategoryId());

        slot.setMenuItem(menuItem);
        slot.setVariant(variant);
        slot.setQuantity((short) defaultQuantity(request.getQuantity()));
        slot.setIsSubstitutable(substitutable);
        slot.setSubstitutionCategory(substitutionCategory);
        slot.setDisplayOrder((short) defaultNumber(request.getDisplayOrder()));
        slot.setNote(request.getNote());
    }

    private int defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }

    private int defaultQuantity(Integer value) {
        return value == null ? 1 : value;
    }
}
