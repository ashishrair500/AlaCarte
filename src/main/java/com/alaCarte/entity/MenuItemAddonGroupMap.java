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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
    name = "menu_item_addon_group_map",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_item_addon_group", columnNames = {"menu_item_id", "addon_group_id"})
    },
    indexes = {
        @Index(name = "idx_addon_map_item", columnList = "menu_item_id"),
        @Index(name = "idx_addon_map_group", columnList = "addon_group_id")
    }
)
@SQLRestriction("deleted = 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"menuItem", "addonGroup"})
public class MenuItemAddonGroupMap extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addon_group_id", nullable = false)
    private MenuItemAddonGroup addonGroup;

    @Column(name = "display_order", nullable = false)
    private Short displayOrder = 0;

    @Column(name = "deleted", nullable = false)
    private Short deleted = 0;

    @PrePersist
    protected void initializeDefaults() {
        if (displayOrder == null) {
            displayOrder = 0;
        }
        if (deleted == null) {
            deleted = 0;
        }
    }
}
