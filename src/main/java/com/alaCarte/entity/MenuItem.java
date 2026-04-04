package com.alaCarte.entity;

import com.alaCarte.entity.blueprint.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a MenuItem in the database
 * Represents individual food/drink items in a restaurant's menu
 */
@Entity
@Table(name = "menu_item", indexes = {
    @Index(name = "idx_item_menu_category", columnList = "menu_id, category_id, display_order"),
    @Index(name = "idx_item_availability", columnList = "restaurant_id, is_available"),
    @Index(name = "idx_item_price", columnList = "restaurant_id, base_price"),
    @Index(name = "idx_item_dietary", columnList = "restaurant_id, is_veg, is_vegan")
})
@SQLRestriction("deleted = 0")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"restaurant", "menu", "category"})
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private MenuCategory category;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ai_description", columnDefinition = "TEXT")
    private String aiDescription;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "is_veg")
    private Boolean isVeg;

    @Column(name = "is_vegan")
    private Boolean isVegan;

    @Column(name = "is_spicy")
    private Boolean isSpicy;

    @Column(name = "contains_egg")
    private Boolean containsEgg;

    @Column(name = "contains_gluten")
    private Boolean containsGluten;

    @Column(name = "contains_nuts")
    private Boolean containsNuts;

    @Column(name = "contains_dairy")
    private Boolean containsDairy;

    @Column(name = "spice_level")
    private Short spiceLevel;

    @Column(name = "prep_time_minutes")
    private Short prepTimeMinutes;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "gallery", columnDefinition = "TEXT[]")
    private String[] gallery;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> nutrition;

    @Column(name = "ingredients", columnDefinition = "TEXT[]")
    private String[] ingredients;

    @Column(name = "cuisine_type", length = 100)
    private String cuisineType;

    @Column(name = "course_type", length = 50)
    private String courseType;

    @Column(name = "display_order", nullable = false)
    private Short displayOrder = 0;

    @Column(length = 100)
    private String badge;

    @Column(name = "has_variants", nullable = false)
    private Boolean hasVariants = false;

    @Column(name = "has_addons", nullable = false)
    private Boolean hasAddons = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> meta;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted", nullable = false)
    private Short deleted = 0;

    @PrePersist
    protected void initializeDefaults() {
        if (isAvailable == null) {
            isAvailable = true;
        }
        if (displayOrder == null) {
            displayOrder = 0;
        }
        if (hasVariants == null) {
            hasVariants = false;
        }
        if (hasAddons == null) {
            hasAddons = false;
        }
        if (deleted == null) {
            deleted = 0;
        }
    }
}
