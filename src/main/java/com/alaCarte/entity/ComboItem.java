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
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "combo_item", indexes = {
    @Index(name = "idx_combo_item_combo", columnList = "combo_id"),
    @Index(name = "idx_combo_item_menu_item", columnList = "menu_item_id")
})
@SQLRestriction("deleted = 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"combo", "menuItem", "variant", "substitutionCategory"})
public class ComboItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id", nullable = false)
    private Combo combo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private MenuItemVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitution_category_id")
    private MenuCategory substitutionCategory;

    @Column(nullable = false)
    private Short quantity = 1;

    @Column(name = "is_substitutable", nullable = false)
    private Boolean isSubstitutable = false;

    @Column(name = "display_order", nullable = false)
    private Short displayOrder = 0;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "deleted", nullable = false)
    private Short deleted = 0;

    @PrePersist
    protected void initializeDefaults() {
        if (quantity == null) {
            quantity = 1;
        }
        if (isSubstitutable == null) {
            isSubstitutable = false;
        }
        if (displayOrder == null) {
            displayOrder = 0;
        }
        if (deleted == null) {
            deleted = 0;
        }
    }
}
