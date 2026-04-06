package com.alaCarte.menu.util;

import com.alaCarte.entity.Combo;
import com.alaCarte.entity.ComboItem;
import com.alaCarte.entity.Menu;
import com.alaCarte.entity.MenuCategory;
import com.alaCarte.entity.MenuItem;
import com.alaCarte.entity.MenuItemAddon;
import com.alaCarte.entity.MenuItemAddonGroup;
import com.alaCarte.entity.MenuItemTag;
import com.alaCarte.entity.MenuItemVariant;
import com.alaCarte.menu.dto.response.AddonGroupDetailResponseDTO;
import com.alaCarte.menu.dto.response.AddonGroupResponseDTO;
import com.alaCarte.menu.dto.response.AddonResponseDTO;
import com.alaCarte.menu.dto.response.ComboDetailResponseDTO;
import com.alaCarte.menu.dto.response.ComboItemResponseDTO;
import com.alaCarte.menu.dto.response.ComboResponseDTO;
import com.alaCarte.menu.dto.response.MenuItemDetailResponseDTO;
import com.alaCarte.menu.dto.response.MenuItemResponseDTO;
import com.alaCarte.menu.dto.response.MenuResponseDTO;
import com.alaCarte.menu.dto.response.MenuCategoryResponseDTO;
import com.alaCarte.menu.dto.response.MenuItemVariantResponseDTO;
import com.alaCarte.menu.dto.response.TagResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MenuModuleMapper {

    public MenuResponseDTO toMenuResponse(Menu menu, int totalCategories, int totalItems) {
        return MenuResponseDTO.builder()
                .id(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .menuType(menu.getMenuType())
                .isActive(menu.getIsActive())
                .validFrom(menu.getValidFrom())
                .validTo(menu.getValidTo())
                .displayOrder(menu.getDisplayOrder())
                .coverImage(menu.getCoverImage())
                .totalCategories(totalCategories)
                .totalItems(totalItems)
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }

    public MenuCategoryResponseDTO toMenuCategoryResponse(MenuCategory category, int itemCount) {
        return MenuCategoryResponseDTO.builder()
                .id(category.getId())
                .menuId(category.getMenu().getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .displayOrder(intValue(category.getDisplayOrder()))
                .isActive(category.getIsActive())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .parentCategoryName(category.getParentCategory() != null ? category.getParentCategory().getName() : null)
                .itemCount(itemCount)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    public MenuItemResponseDTO toMenuItemResponse(MenuItem menuItem) {
        return MenuItemResponseDTO.builder()
                .id(menuItem.getId())
                .menuId(menuItem.getMenu().getId())
                .categoryId(menuItem.getCategory() != null ? menuItem.getCategory().getId() : null)
                .categoryName(menuItem.getCategory() != null ? menuItem.getCategory().getName() : null)
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .basePrice(menuItem.getBasePrice())
                .discountPrice(menuItem.getDiscountPrice())
                .isAvailable(menuItem.getIsAvailable())
                .isVeg(menuItem.getIsVeg())
                .isVegan(menuItem.getIsVegan())
                .isSpicy(menuItem.getIsSpicy())
                .spiceLevel(intValue(menuItem.getSpiceLevel()))
                .imageUrl(menuItem.getImageUrl())
                .badge(menuItem.getBadge())
                .hasVariants(menuItem.getHasVariants())
                .hasAddons(menuItem.getHasAddons())
                .displayOrder(intValue(menuItem.getDisplayOrder()))
                .courseType(menuItem.getCourseType())
                .createdAt(menuItem.getCreatedAt())
                .updatedAt(menuItem.getUpdatedAt())
                .build();
    }

    public MenuItemDetailResponseDTO toMenuItemDetailResponse(
            MenuItem menuItem,
            List<MenuItemVariantResponseDTO> variants,
            List<AddonGroupDetailResponseDTO> addonGroups,
            List<TagResponseDTO> tags
    ) {
        return MenuItemDetailResponseDTO.builder()
                .id(menuItem.getId())
                .menuId(menuItem.getMenu().getId())
                .categoryId(menuItem.getCategory() != null ? menuItem.getCategory().getId() : null)
                .categoryName(menuItem.getCategory() != null ? menuItem.getCategory().getName() : null)
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .aiDescription(menuItem.getAiDescription())
                .basePrice(menuItem.getBasePrice())
                .discountPrice(menuItem.getDiscountPrice())
                .isAvailable(menuItem.getIsAvailable())
                .isVeg(menuItem.getIsVeg())
                .isVegan(menuItem.getIsVegan())
                .isSpicy(menuItem.getIsSpicy())
                .containsEgg(menuItem.getContainsEgg())
                .containsGluten(menuItem.getContainsGluten())
                .containsNuts(menuItem.getContainsNuts())
                .containsDairy(menuItem.getContainsDairy())
                .spiceLevel(intValue(menuItem.getSpiceLevel()))
                .prepTimeMinutes(intValue(menuItem.getPrepTimeMinutes()))
                .imageUrl(menuItem.getImageUrl())
                .gallery(stringList(menuItem.getGallery()))
                .ingredients(stringList(menuItem.getIngredients()))
                .cuisineType(menuItem.getCuisineType())
                .courseType(menuItem.getCourseType())
                .badge(menuItem.getBadge())
                .hasVariants(menuItem.getHasVariants())
                .hasAddons(menuItem.getHasAddons())
                .displayOrder(intValue(menuItem.getDisplayOrder()))
                .nutrition(menuItem.getNutrition())
                .meta(menuItem.getMeta())
                .createdAt(menuItem.getCreatedAt())
                .updatedAt(menuItem.getUpdatedAt())
                .variants(variants)
                .addonGroups(addonGroups)
                .tags(tags)
                .build();
    }

    public MenuItemVariantResponseDTO toVariantResponse(MenuItemVariant variant) {
        return MenuItemVariantResponseDTO.builder()
                .id(variant.getId())
                .menuItemId(variant.getMenuItem().getId())
                .variantType(variant.getVariantType())
                .name(variant.getName())
                .description(variant.getDescription())
                .price(variant.getPrice())
                .discountPrice(variant.getDiscountPrice())
                .isAvailable(variant.getIsAvailable())
                .isDefault(variant.getIsDefault())
                .displayOrder(intValue(variant.getDisplayOrder()))
                .build();
    }

    public AddonResponseDTO toAddonResponse(MenuItemAddon addon) {
        return AddonResponseDTO.builder()
                .id(addon.getId())
                .addonGroupId(addon.getAddonGroup().getId())
                .name(addon.getName())
                .description(addon.getDescription())
                .price(addon.getPrice())
                .isAvailable(addon.getIsAvailable())
                .isDefault(addon.getIsDefault())
                .displayOrder(intValue(addon.getDisplayOrder()))
                .build();
    }

    public AddonGroupResponseDTO toAddonGroupResponse(MenuItemAddonGroup group, int addonCount) {
        return AddonGroupResponseDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .selectionType(group.getSelectionType())
                .minSelections(intValue(group.getMinSelections()))
                .maxSelections(intValue(group.getMaxSelections()))
                .isRequired(group.getIsRequired())
                .isActive(group.getIsActive())
                .displayOrder(intValue(group.getDisplayOrder()))
                .addonCount(addonCount)
                .build();
    }

    public AddonGroupDetailResponseDTO toAddonGroupDetailResponse(MenuItemAddonGroup group, List<AddonResponseDTO> addons) {
        return AddonGroupDetailResponseDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .selectionType(group.getSelectionType())
                .minSelections(intValue(group.getMinSelections()))
                .maxSelections(intValue(group.getMaxSelections()))
                .isRequired(group.getIsRequired())
                .isActive(group.getIsActive())
                .displayOrder(intValue(group.getDisplayOrder()))
                .addons(addons)
                .build();
    }

    public TagResponseDTO toTagResponse(MenuItemTag tag) {
        return TagResponseDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .tagType(tag.getTagType())
                .description(tag.getDescription())
                .colorHex(tag.getColorHex())
                .iconUrl(tag.getIconUrl())
                .isRestaurantSpecific(tag.getRestaurant() != null)
                .build();
    }

    public ComboResponseDTO toComboResponse(Combo combo, int slotCount) {
        return ComboResponseDTO.builder()
                .id(combo.getId())
                .menuId(combo.getMenu().getId())
                .name(combo.getName())
                .description(combo.getDescription())
                .imageUrl(combo.getImageUrl())
                .comboPrice(combo.getComboPrice())
                .originalPrice(combo.getOriginalPrice())
                .discountPrice(combo.getDiscountPrice())
                .isAvailable(combo.getIsAvailable())
                .validFrom(combo.getValidFrom())
                .validTo(combo.getValidTo())
                .availableDays(stringList(combo.getAvailableDays()))
                .badge(combo.getBadge())
                .displayOrder(intValue(combo.getDisplayOrder()))
                .slotCount(slotCount)
                .build();
    }

    public ComboDetailResponseDTO toComboDetailResponse(Combo combo, int slotCount, List<ComboItemResponseDTO> items) {
        return ComboDetailResponseDTO.builder()
                .id(combo.getId())
                .menuId(combo.getMenu().getId())
                .name(combo.getName())
                .description(combo.getDescription())
                .imageUrl(combo.getImageUrl())
                .comboPrice(combo.getComboPrice())
                .originalPrice(combo.getOriginalPrice())
                .discountPrice(combo.getDiscountPrice())
                .isAvailable(combo.getIsAvailable())
                .validFrom(combo.getValidFrom())
                .validTo(combo.getValidTo())
                .availableDays(stringList(combo.getAvailableDays()))
                .badge(combo.getBadge())
                .displayOrder(intValue(combo.getDisplayOrder()))
                .slotCount(slotCount)
                .aiDescription(combo.getAiDescription())
                .maxQuantityPerOrder(intValue(combo.getMaxQuantityPerOrder()))
                .items(items)
                .build();
    }

    public ComboItemResponseDTO toComboItemResponse(ComboItem comboItem) {
        return ComboItemResponseDTO.builder()
                .id(comboItem.getId())
                .comboId(comboItem.getCombo().getId())
                .menuItemId(comboItem.getMenuItem().getId())
                .menuItemName(comboItem.getMenuItem().getName())
                .menuItemImage(comboItem.getMenuItem().getImageUrl())
                .variantId(comboItem.getVariant() != null ? comboItem.getVariant().getId() : null)
                .variantName(comboItem.getVariant() != null ? comboItem.getVariant().getName() : null)
                .quantity(intValue(comboItem.getQuantity()))
                .isSubstitutable(comboItem.getIsSubstitutable())
                .substitutionCategoryId(comboItem.getSubstitutionCategory() != null ? comboItem.getSubstitutionCategory().getId() : null)
                .substitutionCategoryName(comboItem.getSubstitutionCategory() != null ? comboItem.getSubstitutionCategory().getName() : null)
                .displayOrder(intValue(comboItem.getDisplayOrder()))
                .note(comboItem.getNote())
                .build();
    }

    private Integer intValue(Number number) {
        return number == null ? null : number.intValue();
    }

    private List<String> stringList(String[] values) {
        return values == null ? List.of() : Arrays.asList(values);
    }
}
