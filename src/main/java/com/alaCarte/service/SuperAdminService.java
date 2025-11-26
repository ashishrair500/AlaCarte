package com.alaCarte.service;

import com.alaCarte.entity.SuperAdmin;
import com.alaCarte.repository.SuperAdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service class for SuperAdmin operations
 */
@Service
public class SuperAdminService {

    private final SuperAdminRepository superAdminRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminService(SuperAdminRepository superAdminRepository, 
                             PasswordEncoder passwordEncoder) {
        this.superAdminRepository = superAdminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new super admin with encrypted password
     */
    @Transactional
    public SuperAdmin createSuperAdmin(SuperAdmin superAdmin, String rawPassword) {
        // Check if username already exists
        if (superAdminRepository.existsByUsername(superAdmin.getUsername())) {
            throw new RuntimeException("Username already exists: " + superAdmin.getUsername());
        }
        
        // Check if email already exists (if provided)
        if (superAdmin.getEmail() != null && superAdminRepository.existsByEmail(superAdmin.getEmail())) {
            throw new RuntimeException("Email already exists: " + superAdmin.getEmail());
        }
        
        // Encrypt password
        superAdmin.setPasswordHash(passwordEncoder.encode(rawPassword));
        
        return superAdminRepository.save(superAdmin);
    }

    /**
     * Find super admin by username
     */
    public Optional<SuperAdmin> findByUsername(String username) {
        return superAdminRepository.findByUsername(username);
    }

    /**
     * Find active super admin by username
     */
    public Optional<SuperAdmin> findActiveByUsername(String username) {
        return superAdminRepository.findActiveByUsername(username);
    }

    /**
     * Find super admin by username or email
     */
    public Optional<SuperAdmin> findByUsernameOrEmail(String usernameOrEmail) {
        return superAdminRepository.findByUsernameOrEmail(usernameOrEmail);
    }

    /**
     * Verify password
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Authenticate super admin
     */
    public Optional<SuperAdmin> authenticate(String usernameOrEmail, String password) {
        Optional<SuperAdmin> superAdminOpt = findActiveByUsername(usernameOrEmail);
        
        if (superAdminOpt.isEmpty()) {
            // Try finding by email
            superAdminOpt = superAdminRepository.findByUsernameOrEmail(usernameOrEmail)
                    .filter(SuperAdmin::getIsActive);
        }
        
        if (superAdminOpt.isPresent()) {
            SuperAdmin superAdmin = superAdminOpt.get();
            if (verifyPassword(password, superAdmin.getPasswordHash())) {
                return Optional.of(superAdmin);
            }
        }
        
        return Optional.empty();
    }

    /**
     * Get super admin by ID
     */
    public Optional<SuperAdmin> findById(UUID id) {
        return superAdminRepository.findById(id);
    }

    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        return superAdminRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return superAdminRepository.existsByEmail(email);
    }
}
