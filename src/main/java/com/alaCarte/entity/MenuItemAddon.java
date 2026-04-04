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

@Entity
@Table(name = "menu_item_addon", indexes = {
    @Index(name = "idx_addon_group_fk", columnList = "addon_group_id"),
    @Index(name = "idx_addon_restaurant", columnList = "restaurant_id, is_available")
})
@SQLRestriction("deleted = 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"addonGroup", "restaurant"})
public class MenuItemAddon extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addon_group_id", nullable = false)
    private MenuItemAddonGroup addonGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "display_order", nullable = false)
    private Short displayOrder = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> meta;

    @Column(name = "deleted", nullable = false)
    private Short deleted = 0;

    @PrePersist
    protected void initializeDefaults() {
        if (price == null) {
            price = BigDecimal.ZERO;
        }
        if (isAvailable == null) {
            isAvailable = true;
        }
        if (isDefault == null) {
            isDefault = false;
        }
        if (displayOrder == null) {
            displayOrder = 0;
        }
        if (deleted == null) {
            deleted = 0;
        }
    }
}
