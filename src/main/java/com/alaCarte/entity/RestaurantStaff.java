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
 * Entity representing Restaurant Staff in the database
 * Manages staff members, their roles, and permissions
 */
@Entity
@Table(name = "restaurant_staff")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantStaff extends BaseModel {

    // Links
    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    // Identification & role
    @Column(name = "employee_code", columnDefinition = "TEXT")
    private String employeeCode; // optional code unique within a restaurant

    @Column(name = "staff_role", nullable = false, columnDefinition = "TEXT")
    private String staffRole = "STAFF"; // e.g., CHEF, WAITER, MANAGER, BARTENDER

    @Column(columnDefinition = "TEXT")
    private String designation; // free-form job title

    @Column(name = "reporting_to")
    private UUID reportingTo; // user_id of reporting manager (nullable)

    // Contact snapshot (in case user record changes)
    @Column(columnDefinition = "TEXT")
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String email;

    @Column(name = "user_name", columnDefinition = "TEXT")
    private String userName;

    @Column(name = "password_hash", columnDefinition = "TEXT")
    private String passwordHash;

    // Employment & status
    @Column(nullable = false, columnDefinition = "TEXT")
    private String status = "ACTIVE"; // ACTIVE, SUSPENDED, RESIGNED, TERMINATED

    @Column(name = "joined_at")
    private OffsetDateTime joinedAt;

    @Column(name = "left_at")
    private OffsetDateTime leftAt;

    // Operational & permissions
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shift_meta", columnDefinition = "JSONB")
    private Map<String, Object> shiftMeta; // e.g., recurring shifts, timezone, weekly hours

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> permissions; // staff-specific permission overrides

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> meta; // any extra metadata

    // Audit & soft-delete
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (staffRole == null) {
            staffRole = "STAFF";
        }
    }
}

