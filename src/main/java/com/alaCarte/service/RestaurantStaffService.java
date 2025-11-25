package com.alaCarte.service;

import com.alaCarte.entity.RestaurantStaff;
import com.alaCarte.repository.RestaurantStaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for RestaurantStaff operations
 * Provides business logic for managing restaurant staff members
 */
@Service
public class RestaurantStaffService {

    private final RestaurantStaffRepository staffRepository;

    // Constructor injection
    public RestaurantStaffService(RestaurantStaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    /**
     * Create a new staff member
     */
    @Transactional
    public RestaurantStaff createStaff(RestaurantStaff staff) {
        // Check if employee code already exists for this restaurant
        if (staff.getEmployeeCode() != null && 
            staffRepository.existsByRestaurantIdAndEmployeeCode(staff.getRestaurantId(), staff.getEmployeeCode())) {
            throw new RuntimeException("Employee code '" + staff.getEmployeeCode() + "' already exists for this restaurant");
        }
        
        // Check if username already exists
        if (staff.getUserName() != null && staffRepository.existsByUserName(staff.getUserName())) {
            throw new RuntimeException("Username already exists: " + staff.getUserName());
        }
        
        // Check if email already exists
        if (staff.getEmail() != null && staffRepository.existsByEmail(staff.getEmail())) {
            throw new RuntimeException("Email already exists: " + staff.getEmail());
        }
        
        return staffRepository.save(staff);
    }

    /**
     * Get all staff
     */
    public List<RestaurantStaff> getAllStaff() {
        return staffRepository.findAll();
    }

    /**
     * Get staff by ID
     */
    public Optional<RestaurantStaff> getStaffById(UUID id) {
        return staffRepository.findById(id);
    }

    /**
     * Get all staff by restaurant ID
     */
    public List<RestaurantStaff> getStaffByRestaurantId(Long restaurantId) {
        return staffRepository.findByRestaurantId(restaurantId);
    }

    /**
     * Get active staff by restaurant ID
     */
    public List<RestaurantStaff> getActiveStaffByRestaurantId(Long restaurantId) {
        return staffRepository.findByRestaurantIdAndIsActiveTrue(restaurantId);
    }

    /**
     * Get inactive staff by restaurant ID
     */
    public List<RestaurantStaff> getInactiveStaffByRestaurantId(Long restaurantId) {
        return staffRepository.findByRestaurantIdAndIsActiveFalse(restaurantId);
    }

    /**
     * Get staff by employee code
     */
    public Optional<RestaurantStaff> getStaffByEmployeeCode(Long restaurantId, String employeeCode) {
        return staffRepository.findByRestaurantIdAndEmployeeCode(restaurantId, employeeCode);
    }

    /**
     * Get staff by username
     */
    public Optional<RestaurantStaff> getStaffByUsername(String userName) {
        return staffRepository.findByUserName(userName);
    }

    /**
     * Get staff by email
     */
    public Optional<RestaurantStaff> getStaffByEmail(String email) {
        return staffRepository.findByEmail(email);
    }

    /**
     * Find staff by username or email (useful for login)
     */
    public Optional<RestaurantStaff> findByUsernameOrEmail(String usernameOrEmail) {
        return staffRepository.findByUsernameOrEmail(usernameOrEmail);
    }

    /**
     * Get staff by role
     */
    public List<RestaurantStaff> getStaffByRole(Long restaurantId, String staffRole) {
        return staffRepository.findByRestaurantIdAndStaffRole(restaurantId, staffRole);
    }

    /**
     * Get active staff by role
     */
    public List<RestaurantStaff> getActiveStaffByRole(Long restaurantId, String staffRole) {
        return staffRepository.findActiveByRestaurantIdAndStaffRole(restaurantId, staffRole);
    }

    /**
     * Get staff by status
     */
    public List<RestaurantStaff> getStaffByStatus(Long restaurantId, String status) {
        return staffRepository.findByRestaurantIdAndStatus(restaurantId, status);
    }

    /**
     * Get active staff by status
     */
    public List<RestaurantStaff> getActiveStaffByStatus(Long restaurantId, String status) {
        return staffRepository.findActiveByRestaurantIdAndStatus(restaurantId, status);
    }

    /**
     * Get staff reporting to a specific manager
     */
    public List<RestaurantStaff> getStaffReportingTo(UUID managerId) {
        return staffRepository.findByReportingTo(managerId);
    }

    /**
     * Get active staff reporting to a specific manager
     */
    public List<RestaurantStaff> getActiveStaffReportingTo(UUID managerId) {
        return staffRepository.findActiveByReportingTo(managerId);
    }

    /**
     * Search staff by designation
     */
    public List<RestaurantStaff> searchByDesignation(Long restaurantId, String designation) {
        return staffRepository.findByRestaurantIdAndDesignationContainingIgnoreCase(restaurantId, designation);
    }

    /**
     * Get staff who joined within a date range
     */
    public List<RestaurantStaff> getStaffByJoinedDateRange(Long restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
        return staffRepository.findByJoinedAtBetween(restaurantId, startDate, endDate);
    }

    /**
     * Get staff who left within a date range
     */
    public List<RestaurantStaff> getStaffByLeftDateRange(Long restaurantId, OffsetDateTime startDate, OffsetDateTime endDate) {
        return staffRepository.findByLeftAtBetween(restaurantId, startDate, endDate);
    }

    /**
     * Get currently employed staff
     */
    public List<RestaurantStaff> getCurrentlyEmployedStaff(Long restaurantId) {
        return staffRepository.findCurrentlyEmployed(restaurantId);
    }

    /**
     * Get soft-deleted staff
     */
    public List<RestaurantStaff> getSoftDeletedStaff(Long restaurantId) {
        return staffRepository.findSoftDeleted(restaurantId);
    }

    /**
     * Get not soft-deleted staff
     */
    public List<RestaurantStaff> getNotSoftDeletedStaff(Long restaurantId) {
        return staffRepository.findNotSoftDeleted(restaurantId);
    }

    /**
     * Find staff by permissions
     */
    public List<RestaurantStaff> findStaffByPermissions(Long restaurantId, String permissionJson) {
        return staffRepository.findByRestaurantIdAndPermissions(restaurantId, permissionJson);
    }

    /**
     * Advanced search with multiple criteria
     */
    public List<RestaurantStaff> searchStaff(Long restaurantId, Boolean isActive, String status, 
                                              String staffRole, String searchTerm) {
        return staffRepository.searchStaff(restaurantId, isActive, status, staffRole, searchTerm);
    }

    /**
     * Get all managers in a restaurant
     */
    public List<RestaurantStaff> getAllManagers(Long restaurantId) {
        return staffRepository.findAllManagers(restaurantId);
    }

    /**
     * Get team members under a manager
     */
    public List<RestaurantStaff> getTeamMembers(UUID managerId) {
        return staffRepository.findTeamMembers(managerId);
    }

    /**
     * Count staff by restaurant
     */
    public long countStaffByRestaurant(Long restaurantId) {
        return staffRepository.countByRestaurantId(restaurantId);
    }

    /**
     * Count active staff by restaurant
     */
    public long countActiveStaffByRestaurant(Long restaurantId) {
        return staffRepository.countByRestaurantIdAndIsActiveTrue(restaurantId);
    }

    /**
     * Count by restaurant and status
     */
    public long countByRestaurantAndStatus(Long restaurantId, String status) {
        return staffRepository.countByRestaurantIdAndStatus(restaurantId, status);
    }

    /**
     * Count by restaurant and role
     */
    public long countByRestaurantAndRole(Long restaurantId, String staffRole) {
        return staffRepository.countByRestaurantIdAndStaffRole(restaurantId, staffRole);
    }

    /**
     * Check if employee code exists
     */
    public boolean employeeCodeExists(Long restaurantId, String employeeCode) {
        return staffRepository.existsByRestaurantIdAndEmployeeCode(restaurantId, employeeCode);
    }

    /**
     * Check if username exists
     */
    public boolean usernameExists(String userName) {
        return staffRepository.existsByUserName(userName);
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return staffRepository.existsByEmail(email);
    }

    /**
     * Update an existing staff member
     */
    @Transactional
    public RestaurantStaff updateStaff(UUID id, RestaurantStaff updatedStaff) {
        return staffRepository.findById(id)
                .map(existingStaff -> {
                    // Check employee code conflicts
                    if (updatedStaff.getEmployeeCode() != null && 
                        !updatedStaff.getEmployeeCode().equals(existingStaff.getEmployeeCode())) {
                        if (staffRepository.existsByRestaurantIdAndEmployeeCode(
                                existingStaff.getRestaurantId(), updatedStaff.getEmployeeCode())) {
                            throw new RuntimeException("Employee code already exists: " + updatedStaff.getEmployeeCode());
                        }
                    }
                    
                    // Check username conflicts
                    if (updatedStaff.getUserName() != null && 
                        !updatedStaff.getUserName().equals(existingStaff.getUserName())) {
                        if (staffRepository.existsByUserName(updatedStaff.getUserName())) {
                            throw new RuntimeException("Username already exists: " + updatedStaff.getUserName());
                        }
                    }
                    
                    // Check email conflicts
                    if (updatedStaff.getEmail() != null && 
                        !updatedStaff.getEmail().equals(existingStaff.getEmail())) {
                        if (staffRepository.existsByEmail(updatedStaff.getEmail())) {
                            throw new RuntimeException("Email already exists: " + updatedStaff.getEmail());
                        }
                    }
                    
                    existingStaff.setEmployeeCode(updatedStaff.getEmployeeCode());
                    existingStaff.setStaffRole(updatedStaff.getStaffRole());
                    existingStaff.setDesignation(updatedStaff.getDesignation());
                    existingStaff.setReportingTo(updatedStaff.getReportingTo());
                    existingStaff.setPhone(updatedStaff.getPhone());
                    existingStaff.setEmail(updatedStaff.getEmail());
                    existingStaff.setUserName(updatedStaff.getUserName());
                    existingStaff.setPasswordHash(updatedStaff.getPasswordHash());
                    existingStaff.setStatus(updatedStaff.getStatus());
                    existingStaff.setJoinedAt(updatedStaff.getJoinedAt());
                    existingStaff.setLeftAt(updatedStaff.getLeftAt());
                    existingStaff.setShiftMeta(updatedStaff.getShiftMeta());
                    existingStaff.setPermissions(updatedStaff.getPermissions());
                    existingStaff.setMeta(updatedStaff.getMeta());
                    existingStaff.setIsActive(updatedStaff.getIsActive());
                    
                    return staffRepository.save(existingStaff);
                })
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    /**
     * Partially update staff (only non-null fields)
     */
    @Transactional
    public RestaurantStaff patchStaff(UUID id, RestaurantStaff updates) {
        return staffRepository.findById(id)
                .map(existing -> {
                    if (updates.getEmployeeCode() != null) {
                        if (!updates.getEmployeeCode().equals(existing.getEmployeeCode())) {
                            if (staffRepository.existsByRestaurantIdAndEmployeeCode(
                                    existing.getRestaurantId(), updates.getEmployeeCode())) {
                                throw new RuntimeException("Employee code already exists");
                            }
                        }
                        existing.setEmployeeCode(updates.getEmployeeCode());
                    }
                    if (updates.getUserName() != null) {
                        if (!updates.getUserName().equals(existing.getUserName())) {
                            if (staffRepository.existsByUserName(updates.getUserName())) {
                                throw new RuntimeException("Username already exists");
                            }
                        }
                        existing.setUserName(updates.getUserName());
                    }
                    if (updates.getEmail() != null) {
                        if (!updates.getEmail().equals(existing.getEmail())) {
                            if (staffRepository.existsByEmail(updates.getEmail())) {
                                throw new RuntimeException("Email already exists");
                            }
                        }
                        existing.setEmail(updates.getEmail());
                    }
                    
                    if (updates.getStaffRole() != null) existing.setStaffRole(updates.getStaffRole());
                    if (updates.getDesignation() != null) existing.setDesignation(updates.getDesignation());
                    if (updates.getReportingTo() != null) existing.setReportingTo(updates.getReportingTo());
                    if (updates.getPhone() != null) existing.setPhone(updates.getPhone());
                    if (updates.getPasswordHash() != null) existing.setPasswordHash(updates.getPasswordHash());
                    if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
                    if (updates.getJoinedAt() != null) existing.setJoinedAt(updates.getJoinedAt());
                    if (updates.getLeftAt() != null) existing.setLeftAt(updates.getLeftAt());
                    if (updates.getShiftMeta() != null) existing.setShiftMeta(updates.getShiftMeta());
                    if (updates.getPermissions() != null) existing.setPermissions(updates.getPermissions());
                    if (updates.getMeta() != null) existing.setMeta(updates.getMeta());
                    if (updates.getIsActive() != null) existing.setIsActive(updates.getIsActive());
                    
                    return staffRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    /**
     * Delete a staff member by ID (hard delete)
     */
    @Transactional
    public void deleteStaff(UUID id) {
        staffRepository.deleteById(id);
    }

    /**
     * Soft delete - mark as deleted with timestamp
     */
    @Transactional
    public RestaurantStaff softDeleteStaff(UUID id) {
        return staffRepository.findById(id)
                .map(staff -> {
                    staff.setDeletedAt(OffsetDateTime.now());
                    staff.setIsActive(false);
                    return staffRepository.save(staff);
                })
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    /**
     * Restore soft-deleted staff
     */
    @Transactional
    public RestaurantStaff restoreStaff(UUID id) {
        return staffRepository.findById(id)
                .map(staff -> {
                    staff.setDeletedAt(null);
                    staff.setIsActive(true);
                    return staffRepository.save(staff);
                })
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    /**
     * Deactivate staff
     */
    @Transactional
    public RestaurantStaff deactivateStaff(UUID id) {
        return staffRepository.findById(id)
                .map(staff -> {
                    staff.setIsActive(false);
                    return staffRepository.save(staff);
                })
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    /**
     * Activate staff
     */
    @Transactional
    public RestaurantStaff activateStaff(UUID id) {
        return staffRepository.findById(id)
                .map(staff -> {
                    staff.setIsActive(true);
                    return staffRepository.save(staff);
                })
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    /**
     * Update staff status
     */
    @Transactional
    public RestaurantStaff updateStatus(UUID id, String newStatus) {
        return staffRepository.findById(id)
                .map(staff -> {
                    staff.setStatus(newStatus);
                    // If status is RESIGNED or TERMINATED, set leftAt date
                    if ("RESIGNED".equals(newStatus) || "TERMINATED".equals(newStatus)) {
                        if (staff.getLeftAt() == null) {
                            staff.setLeftAt(OffsetDateTime.now());
                        }
                    }
                    return staffRepository.save(staff);
                })
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    /**
     * Update staff role
     */
    @Transactional
    public RestaurantStaff updateRole(UUID id, String newRole) {
        return staffRepository.findById(id)
                .map(staff -> {
                    staff.setStaffRole(newRole);
                    return staffRepository.save(staff);
                })
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    /**
     * Update staff permissions
     */
    @Transactional
    public RestaurantStaff updatePermissions(UUID id, Map<String, Object> newPermissions) {
        return staffRepository.findById(id)
                .map(staff -> {
                    staff.setPermissions(newPermissions);
                    return staffRepository.save(staff);
                })
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    /**
     * Update shift metadata
     */
    @Transactional
    public RestaurantStaff updateShiftMeta(UUID id, Map<String, Object> newShiftMeta) {
        return staffRepository.findById(id)
                .map(staff -> {
                    staff.setShiftMeta(newShiftMeta);
                    return staffRepository.save(staff);
                })
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    /**
     * Assign reporting manager
     */
    @Transactional
    public RestaurantStaff assignManager(UUID staffId, UUID managerId) {
        return staffRepository.findById(staffId)
                .map(staff -> {
                    staff.setReportingTo(managerId);
                    return staffRepository.save(staff);
                })
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + staffId));
    }

    /**
     * Check if a staff member exists
     */
    public boolean staffExists(UUID id) {
        return staffRepository.existsById(id);
    }

    /**
     * Count total staff
     */
    public long countStaff() {
        return staffRepository.count();
    }
}

