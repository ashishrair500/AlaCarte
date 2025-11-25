package com.alaCarte.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a Menu in the database
 * A menu belongs to a restaurant and contains categorized menu items
 */
@Entity
@Table(name = "menu", indexes = {
    @Index(name = "idx_menu_restaurant", columnList = "restaurant_id"),
    @Index(name = "idx_menu_active", columnList = "restaurant_id, is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String name; // e.g., Breakfast, Lunch, Dinner, Bar Menu

    @Column(columnDefinition = "TEXT")
    private String description; // optional

    @Column(name = "menu_type", columnDefinition = "TEXT")
    private String menuType; // optional: BREAKFAST / LUNCH / BAR / CUSTOM

    @Column(name = "is_active")
    private Boolean isActive = true; // to hide/deactivate menu

    @Column(name = "valid_from")
    private OffsetDateTime validFrom; // optional (seasonal menu)

    @Column(name = "valid_to")
    private OffsetDateTime validTo; // optional

    @Column(name = "display_order")
    private Integer displayOrder = 0; // UI ordering for menus

    @Column(name = "cover_image", columnDefinition = "TEXT")
    private String coverImage; // menu banner image URL (optional)

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> meta; // extra fields (tags, styling, filters, etc.)

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy; // who added this menu

    @Column(name = "updated_by")
    private UUID updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (displayOrder == null) {
            displayOrder = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
