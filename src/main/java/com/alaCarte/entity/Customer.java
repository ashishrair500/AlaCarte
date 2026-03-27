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
 * Entity representing a Customer in the database
 */
@Entity
@Table(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // Links
    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    // Identification
    @Column(unique = true, columnDefinition = "TEXT")
    private String phone;

    @Column(unique = true, columnDefinition = "TEXT")
    private String email;

    @Column(name = "full_name", columnDefinition = "TEXT")
    private String fullName;

    // Profile & dietary preferences
    @Column(name = "dietary_pref", columnDefinition = "TEXT")
    private String dietaryPref;

    @Column(columnDefinition = "TEXT[]")
    private String[] allergies;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cuisine_preferences", columnDefinition = "JSONB")
    private Map<String, Object> cuisinePreferences;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "health_goals", columnDefinition = "JSONB")
    private Map<String, Object> healthGoals;

    // Visit / Session Info
    @Column(name = "current_session_id")
    private UUID currentSessionId;

    @Column(name = "last_visit_at")
    private OffsetDateTime lastVisitAt;

    @Column(name = "visit_count")
    private Integer visitCount = 0;

    // Optional login credentials
    @Column(name = "password_hash", columnDefinition = "TEXT")
    private String passwordHash;

    @Column(name = "unique_qr_id", columnDefinition = "TEXT")
    private String uniqueQrId;  //this is the unique identifier for the customer(primary key)

    // Status & soft delete
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    // Audit
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    // Extra metadata
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> meta;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "qr_code", columnDefinition = "JSONB")
    private Map<String, Object> qrCode;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
        if (visitCount == null) visitCount = 0;
        if (isActive == null) isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
