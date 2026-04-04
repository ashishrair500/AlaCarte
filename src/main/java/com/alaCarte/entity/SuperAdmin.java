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

import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a SuperAdmin in the database
 * SuperAdmins have highest level access to the system
 */
@Entity
@Table(name = "super_admin")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdmin extends BaseModel {

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

    @Column(name = "created_by")
    private UUID createdBy; // Reference to another super_admin

    // Audit & Metadata
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private Map<String, Object> meta;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
        if (roleLevel == null) {
            roleLevel = 1;
        }
    }
}
