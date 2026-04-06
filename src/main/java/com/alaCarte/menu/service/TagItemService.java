package com.alaCarte.menu.service;

import com.alaCarte.entity.MenuItem;
import com.alaCarte.entity.MenuItemTag;
import com.alaCarte.entity.MenuItemTagMap;
import com.alaCarte.exception.DuplicateResourceException;
import com.alaCarte.menu.dto.request.TagItemRequestDTO;
import com.alaCarte.menu.dto.response.TagResponseDTO;
import com.alaCarte.menu.repository.MenuItemTagMapRepository;
import com.alaCarte.menu.repository.MenuItemTagRepository;
import com.alaCarte.menu.util.MenuModuleMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TagItemService {

    private static final short DELETED = 1;
    private static final short NOT_DELETED = 0;

    private final MenuItemTagMapRepository tagMapRepository;
    private final MenuItemTagRepository tagRepository;
    private final MenuModuleSupportService supportService;
    private final MenuModuleMapper mapper;

    public TagItemService(
            MenuItemTagMapRepository tagMapRepository,
            MenuItemTagRepository tagRepository,
            MenuModuleSupportService supportService,
            MenuModuleMapper mapper
    ) {
        this.tagMapRepository = tagMapRepository;
        this.tagRepository = tagRepository;
        this.supportService = supportService;
        this.mapper = mapper;
    }

    public void applyTag(UUID restaurantId, UUID itemId, TagItemRequestDTO request) {
        MenuItem menuItem = supportService.requireMenuItem(restaurantId, itemId);
        MenuItemTag tag = tagRepository.findVisibleById(request.getTagId(), restaurantId)
                .orElseThrow(() -> new com.alaCarte.exception.ResourceNotFoundException("MenuItemTag", request.getTagId()));

        if (tagMapRepository.existsByMenuItemIdAndTagIdAndDeleted(itemId, tag.getId(), NOT_DELETED)) {
            throw new DuplicateResourceException("Tag is already applied to this item");
        }

        MenuItemTagMap map = new MenuItemTagMap();
        map.setMenuItem(menuItem);
        map.setTag(tag);

        try {
            tagMapRepository.save(map);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("Tag is already applied to this item");
        }
    }

    @Transactional(readOnly = true)
    public List<TagResponseDTO> getTagsForItem(UUID restaurantId, UUID itemId) {
        supportService.requireMenuItem(restaurantId, itemId);
        return tagMapRepository.findByMenuItemIdWithTag(itemId)
                .stream()
                .map(MenuItemTagMap::getTag)
                .map(mapper::toTagResponse)
                .toList();
    }

    public void removeTag(UUID restaurantId, UUID itemId, UUID tagId) {
        supportService.requireMenuItem(restaurantId, itemId);
        tagRepository.findVisibleById(tagId, restaurantId)
                .orElseThrow(() -> new com.alaCarte.exception.ResourceNotFoundException("MenuItemTag", tagId));

        MenuItemTagMap map = tagMapRepository.findByMenuItemIdAndTagIdAndDeleted(itemId, tagId, NOT_DELETED)
                .orElseThrow(() -> new com.alaCarte.exception.ResourceNotFoundException(
                        "Tag link not found for itemId: " + itemId + " and tagId: " + tagId
                ));
        map.setDeleted(DELETED);
        tagMapRepository.save(map);
    }
}
