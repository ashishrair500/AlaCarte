package com.alaCarte.menu.service;

import com.alaCarte.entity.MenuItemTag;
import com.alaCarte.exception.DuplicateResourceException;
import com.alaCarte.menu.dto.request.TagRequestDTO;
import com.alaCarte.menu.dto.response.TagResponseDTO;
import com.alaCarte.menu.repository.MenuItemTagRepository;
import com.alaCarte.menu.util.MenuModuleMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TagService {

    private static final short DELETED = 1;
    private static final short NOT_DELETED = 0;

    private final MenuItemTagRepository tagRepository;
    private final MenuModuleSupportService supportService;
    private final MenuModuleMapper mapper;

    public TagService(
            MenuItemTagRepository tagRepository,
            MenuModuleSupportService supportService,
            MenuModuleMapper mapper
    ) {
        this.tagRepository = tagRepository;
        this.supportService = supportService;
        this.mapper = mapper;
    }

    public TagResponseDTO createTag(UUID restaurantId, UUID staffId, TagRequestDTO request) {
        supportService.requireRestaurant(restaurantId);
        if (tagRepository.existsBySlugAndRestaurantIdAndDeleted(request.getSlug(), restaurantId, NOT_DELETED)) {
            throw new DuplicateResourceException("Tag slug already exists for this restaurant");
        }

        MenuItemTag tag = new MenuItemTag();
        tag.setRestaurant(supportService.requireRestaurant(restaurantId));
        tag.setName(request.getName());
        tag.setSlug(request.getSlug());
        tag.setTagType(request.getTagType());
        tag.setDescription(request.getDescription());
        tag.setColorHex(request.getColorHex());
        tag.setIconUrl(request.getIconUrl());

        try {
            return mapper.toTagResponse(tagRepository.save(tag));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("Tag slug already exists for this restaurant");
        }
    }

    @Transactional(readOnly = true)
    public List<TagResponseDTO> getAllTags(UUID restaurantId, String tagType) {
        List<MenuItemTag> tags = tagType == null || tagType.isBlank()
                ? tagRepository.findAllVisibleToRestaurant(restaurantId)
                : tagRepository.findByTagTypeVisibleToRestaurant(restaurantId, tagType);
        return tags.stream().map(mapper::toTagResponse).toList();
    }

    public TagResponseDTO updateTag(UUID restaurantId, UUID tagId, TagRequestDTO request) {
        MenuItemTag tag = tagRepository.findByIdAndRestaurantIdAndDeleted(tagId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new com.alaCarte.exception.ResourceNotFoundException("MenuItemTag", tagId));

        if (!tag.getSlug().equals(request.getSlug())
                && tagRepository.existsBySlugAndRestaurantIdAndDeleted(request.getSlug(), restaurantId, NOT_DELETED)) {
            throw new DuplicateResourceException("Tag slug already exists for this restaurant");
        }

        tag.setName(request.getName());
        tag.setSlug(request.getSlug());
        tag.setTagType(request.getTagType());
        tag.setDescription(request.getDescription());
        tag.setColorHex(request.getColorHex());
        tag.setIconUrl(request.getIconUrl());

        try {
            return mapper.toTagResponse(tagRepository.save(tag));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("Tag slug already exists for this restaurant");
        }
    }

    public void deleteTag(UUID restaurantId, UUID tagId) {
        MenuItemTag tag = tagRepository.findByIdAndRestaurantIdAndDeleted(tagId, restaurantId, NOT_DELETED)
                .orElseThrow(() -> new com.alaCarte.exception.ResourceNotFoundException("MenuItemTag", tagId));
        tag.setDeleted(DELETED);
        tagRepository.save(tag);
    }
}
