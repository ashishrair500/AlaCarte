package com.alaCarte.entity;

import com.alaCarte.entity.blueprint.BaseModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseModel {

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
        if (visitCount == null) visitCount = 0;
        if (isActive == null) isActive = true;
    }
}
