package com.alaCarte.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a MenuItem in the database
 * Represents individual food/drink items in a restaurant's menu
 */
@Entity
@Table(name = "menu_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // Relationships
    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "menu_id", nullable = false)
    private UUID menuId;

    // Basic Info
    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String category; // e.g. "Pizza", "Starters", "Beverages"

    // Pricing
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // base price

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice; // optional discounted price

    // Availability
    @Column(name = "is_available")
    private Boolean isAvailable = true; // staff updates this in daily workflow

    @Column(name = "is_veg")
    private Boolean isVeg; // veg/non-veg indicator

    @Column(name = "is_spicy")
    private Boolean isSpicy; // optional quick tag

    @Column(name = "contains_egg")
    private Boolean containsEgg; // common in India

    // Variants & Add-ons
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private List<Map<String, Object>> variants; // e.g. sizes, flavors

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private List<Map<String, Object>> addons; // toppings, extras

    // Tags & Filters
    @Column(columnDefinition = "TEXT[]")
    private String[] tags; // gluten-free, vegan, chef-special etc.

    // Images
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private List<String> gallery; // multiple images

    // Nutrition Info
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> nutrition; // calories, protein, carbs, etc.

    // Display Customization
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(columnDefinition = "TEXT")
    private String badge; // e.g. "Chef's Special", "New", "Bestseller"

    // Extra metadata
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> meta;

    // Audit
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Soft Delete
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
        if (isAvailable == null) {
            isAvailable = true;
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
