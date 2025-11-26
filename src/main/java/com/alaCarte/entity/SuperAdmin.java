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
 * Entity representing a SuperAdmin in the database
 * SuperAdmins have highest level access to the system
 */
@Entity
@Table(name = "super_admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // Login Credentials
    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String username;

    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    // Personal / Identification Info
    @Column(name = "full_name", columnDefinition = "TEXT")
    private String fullName;

    @Column(unique = true, columnDefinition = "TEXT")
    private String email;

    @Column(columnDefinition = "TEXT")
    private String phone;

    // Access Control
    @Column(name = "role_level")
    private Short roleLevel = 1; // SMALLINT maps to Short in Java

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> permissions;

    // Operational Fields
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy; // Reference to another super_admin

    // Audit & Metadata
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> meta;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (roleLevel == null) {
            roleLevel = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
