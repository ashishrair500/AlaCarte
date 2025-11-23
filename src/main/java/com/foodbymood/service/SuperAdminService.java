package com.foodbymood.service;

import com.foodbymood.entity.SuperAdmin;
import com.foodbymood.repository.SuperAdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for SuperAdmin operations
 * Provides business logic for managing super administrators
 */
@Service
public class SuperAdminService {

    private final SuperAdminRepository superAdminRepository;

    // Constructor injection
    public SuperAdminService(SuperAdminRepository superAdminRepository) {
        this.superAdminRepository = superAdminRepository;
    }

    /**
     * Create a new super admin
     */
    @Transactional
    public SuperAdmin createSuperAdmin(SuperAdmin superAdmin) {
        // Check if username already exists
        if (superAdminRepository.existsByUsername(superAdmin.getUsername())) {
            throw new RuntimeException("Username already exists: " + superAdmin.getUsername());
        }
        
        // Check if email already exists (if provided)
        if (superAdmin.getEmail() != null && superAdminRepository.existsByEmail(superAdmin.getEmail())) {
            throw new RuntimeException("Email already exists: " + superAdmin.getEmail());
        }
        
        return superAdminRepository.save(superAdmin);
    }

    /**
     * Get all super admins
     */
    public List<SuperAdmin> getAllSuperAdmins() {
        return superAdminRepository.findAll();
    }

    /**
     * Get super admin by ID
     */
    public Optional<SuperAdmin> getSuperAdminById(UUID id) {
        return superAdminRepository.findById(id);
    }

    /**
     * Get super admin by username
     */
    public Optional<SuperAdmin> getSuperAdminByUsername(String username) {
        return superAdminRepository.findByUsername(username);
    }

    /**
     * Get super admin by email
     */
    public Optional<SuperAdmin> getSuperAdminByEmail(String email) {
        return superAdminRepository.findByEmail(email);
    }

    /**
     * Find super admin by username or email (useful for login)
     */
    public Optional<SuperAdmin> findByUsernameOrEmail(String usernameOrEmail) {
        return superAdminRepository.findByUsernameOrEmail(usernameOrEmail);
    }

    /**
     * Get all active super admins
     */
    public List<SuperAdmin> getActiveSuperAdmins() {
        return superAdminRepository.findByIsActiveTrue();
    }

    /**
     * Get all inactive super admins
     */
    public List<SuperAdmin> getInactiveSuperAdmins() {
        return superAdminRepository.findByIsActiveFalse();
    }

    /**
     * Get super admins by role level
     */
    public List<SuperAdmin> getSuperAdminsByRoleLevel(Short roleLevel) {
        return superAdminRepository.findByRoleLevel(roleLevel);
    }

    /**
     * Get super admins with role level greater than or equal to specified
     */
    public List<SuperAdmin> getSuperAdminsByMinRoleLevel(Short minRoleLevel) {
        return superAdminRepository.findByRoleLevelGreaterThanEqual(minRoleLevel);
    }

    /**
     * Get super admins with role level less than or equal to specified
     */
    public List<SuperAdmin> getSuperAdminsByMaxRoleLevel(Short maxRoleLevel) {
        return superAdminRepository.findByRoleLevelLessThanEqual(maxRoleLevel);
    }

    /**
     * Get active super admins by role level
     */
    public List<SuperAdmin> getActiveSuperAdminsByRoleLevel(Short roleLevel) {
        return superAdminRepository.findActiveByRoleLevel(roleLevel);
    }

    /**
     * Get super admins created by a specific admin
     */
    public List<SuperAdmin> getSuperAdminsCreatedBy(UUID createdBy) {
        return superAdminRepository.findByCreatedBy(createdBy);
    }

    /**
     * Get active super admins created by a specific admin
     */
    public List<SuperAdmin> getActiveSuperAdminsCreatedBy(UUID createdBy) {
        return superAdminRepository.findActiveByCreatedBy(createdBy);
    }

    /**
     * Search super admins by full name
     */
    public List<SuperAdmin> searchByFullName(String fullName) {
        return superAdminRepository.findByFullNameContainingIgnoreCase(fullName);
    }

    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        return superAdminRepository.existsByUsername(username);
    }

    /**
     * Check if username exists (case-insensitive)
     */
    public boolean usernameExistsIgnoreCase(String username) {
        return superAdminRepository.existsByUsernameIgnoreCase(username);
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return superAdminRepository.existsByEmail(email);
    }

    /**
     * Check if email exists (case-insensitive)
     */
    public boolean emailExistsIgnoreCase(String email) {
        return superAdminRepository.existsByEmailIgnoreCase(email);
    }

    /**
     * Get all super admins ordered by role level (highest first)
     */
    public List<SuperAdmin> getAllOrderedByRoleLevel() {
        return superAdminRepository.findAllByOrderByRoleLevelDesc();
    }

    /**
     * Get active super admins ordered by creation date
     */
    public List<SuperAdmin> getActiveOrderedByCreatedAt() {
        return superAdminRepository.findActiveOrderedByCreatedAt();
    }

    /**
     * Count active super admins
     */
    public long countActiveSuperAdmins() {
        return superAdminRepository.countByIsActiveTrue();
    }

    /**
     * Count super admins by role level
     */
    public long countByRoleLevel(Short roleLevel) {
        return superAdminRepository.countByRoleLevel(roleLevel);
    }

    /**
     * Find super admins by permission
     */
    public List<SuperAdmin> findByPermission(String permission) {
        return superAdminRepository.findByPermission(permission);
    }

    /**
     * Advanced search with multiple criteria
     */
    public List<SuperAdmin> searchSuperAdmins(Boolean isActive, Short roleLevel, UUID createdBy) {
        return superAdminRepository.searchSuperAdmins(isActive, roleLevel, createdBy);
    }

    /**
     * Update an existing super admin
     */
    @Transactional
    public SuperAdmin updateSuperAdmin(UUID id, SuperAdmin updatedSuperAdmin) {
        return superAdminRepository.findById(id)
                .map(existingSuperAdmin -> {
                    // Don't allow username change if it conflicts with another admin
                    if (!existingSuperAdmin.getUsername().equals(updatedSuperAdmin.getUsername())) {
                        if (superAdminRepository.existsByUsername(updatedSuperAdmin.getUsername())) {
                            throw new RuntimeException("Username already exists: " + updatedSuperAdmin.getUsername());
                        }
                        existingSuperAdmin.setUsername(updatedSuperAdmin.getUsername());
                    }
                    
                    // Don't allow email change if it conflicts with another admin
                    if (updatedSuperAdmin.getEmail() != null && 
                        !updatedSuperAdmin.getEmail().equals(existingSuperAdmin.getEmail())) {
                        if (superAdminRepository.existsByEmail(updatedSuperAdmin.getEmail())) {
                            throw new RuntimeException("Email already exists: " + updatedSuperAdmin.getEmail());
                        }
                        existingSuperAdmin.setEmail(updatedSuperAdmin.getEmail());
                    }
                    
                    existingSuperAdmin.setPasswordHash(updatedSuperAdmin.getPasswordHash());
                    existingSuperAdmin.setFullName(updatedSuperAdmin.getFullName());
                    existingSuperAdmin.setPhone(updatedSuperAdmin.getPhone());
                    existingSuperAdmin.setRoleLevel(updatedSuperAdmin.getRoleLevel());
                    existingSuperAdmin.setPermissions(updatedSuperAdmin.getPermissions());
                    existingSuperAdmin.setIsActive(updatedSuperAdmin.getIsActive());
                    existingSuperAdmin.setMeta(updatedSuperAdmin.getMeta());
                    
                    return superAdminRepository.save(existingSuperAdmin);
                })
                .orElseThrow(() -> new RuntimeException("SuperAdmin not found with id: " + id));
    }

    /**
     * Partially update super admin (only non-null fields)
     */
    @Transactional
    public SuperAdmin patchSuperAdmin(UUID id, SuperAdmin updates) {
        return superAdminRepository.findById(id)
                .map(existing -> {
                    if (updates.getUsername() != null && !existing.getUsername().equals(updates.getUsername())) {
                        if (superAdminRepository.existsByUsername(updates.getUsername())) {
                            throw new RuntimeException("Username already exists: " + updates.getUsername());
                        }
                        existing.setUsername(updates.getUsername());
                    }
                    if (updates.getEmail() != null && !updates.getEmail().equals(existing.getEmail())) {
                        if (superAdminRepository.existsByEmail(updates.getEmail())) {
                            throw new RuntimeException("Email already exists: " + updates.getEmail());
                        }
                        existing.setEmail(updates.getEmail());
                    }
                    if (updates.getPasswordHash() != null) existing.setPasswordHash(updates.getPasswordHash());
                    if (updates.getFullName() != null) existing.setFullName(updates.getFullName());
                    if (updates.getPhone() != null) existing.setPhone(updates.getPhone());
                    if (updates.getRoleLevel() != null) existing.setRoleLevel(updates.getRoleLevel());
                    if (updates.getPermissions() != null) existing.setPermissions(updates.getPermissions());
                    if (updates.getIsActive() != null) existing.setIsActive(updates.getIsActive());
                    if (updates.getMeta() != null) existing.setMeta(updates.getMeta());
                    
                    return superAdminRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("SuperAdmin not found with id: " + id));
    }

    /**
     * Delete a super admin by ID
     */
    @Transactional
    public void deleteSuperAdmin(UUID id) {
        superAdminRepository.deleteById(id);
    }

    /**
     * Soft delete - deactivate super admin
     */
    @Transactional
    public SuperAdmin deactivateSuperAdmin(UUID id) {
        return superAdminRepository.findById(id)
                .map(superAdmin -> {
                    superAdmin.setIsActive(false);
                    return superAdminRepository.save(superAdmin);
                })
                .orElseThrow(() -> new RuntimeException("SuperAdmin not found with id: " + id));
    }

    /**
     * Reactivate a deactivated super admin
     */
    @Transactional
    public SuperAdmin activateSuperAdmin(UUID id) {
        return superAdminRepository.findById(id)
                .map(superAdmin -> {
                    superAdmin.setIsActive(true);
                    return superAdminRepository.save(superAdmin);
                })
                .orElseThrow(() -> new RuntimeException("SuperAdmin not found with id: " + id));
    }

    /**
     * Update super admin password
     */
    @Transactional
    public SuperAdmin updatePassword(UUID id, String newPasswordHash) {
        return superAdminRepository.findById(id)
                .map(superAdmin -> {
                    superAdmin.setPasswordHash(newPasswordHash);
                    return superAdminRepository.save(superAdmin);
                })
                .orElseThrow(() -> new RuntimeException("SuperAdmin not found with id: " + id));
    }

    /**
     * Update super admin role level
     */
    @Transactional
    public SuperAdmin updateRoleLevel(UUID id, Short newRoleLevel) {
        return superAdminRepository.findById(id)
                .map(superAdmin -> {
                    superAdmin.setRoleLevel(newRoleLevel);
                    return superAdminRepository.save(superAdmin);
                })
                .orElseThrow(() -> new RuntimeException("SuperAdmin not found with id: " + id));
    }

    /**
     * Update super admin permissions
     */
    @Transactional
    public SuperAdmin updatePermissions(UUID id, Map<String, Object> newPermissions) {
        return superAdminRepository.findById(id)
                .map(superAdmin -> {
                    superAdmin.setPermissions(newPermissions);
                    return superAdminRepository.save(superAdmin);
                })
                .orElseThrow(() -> new RuntimeException("SuperAdmin not found with id: " + id));
    }

    /**
     * Update super admin metadata
     */
    @Transactional
    public SuperAdmin updateMeta(UUID id, Map<String, Object> newMeta) {
        return superAdminRepository.findById(id)
                .map(superAdmin -> {
                    superAdmin.setMeta(newMeta);
                    return superAdminRepository.save(superAdmin);
                })
                .orElseThrow(() -> new RuntimeException("SuperAdmin not found with id: " + id));
    }

    /**
     * Check if a super admin exists
     */
    public boolean superAdminExists(UUID id) {
        return superAdminRepository.existsById(id);
    }

    /**
     * Count total super admins
     */
    public long countSuperAdmins() {
        return superAdminRepository.count();
    }
}

