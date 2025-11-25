package com.alaCarte.repository;

import com.alaCarte.entity.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SuperAdmin entity
 * Spring Data JPA automatically implements this interface
 */
@Repository
public interface SuperAdminRepository extends JpaRepository<SuperAdmin, UUID> {

    // Find by username
    Optional<SuperAdmin> findByUsername(String username);

    // Find by email
    Optional<SuperAdmin> findByEmail(String email);

    // Find by username or email (for login)
    @Query("SELECT sa FROM SuperAdmin sa WHERE sa.username = :usernameOrEmail OR sa.email = :usernameOrEmail")
    Optional<SuperAdmin> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    // Find active super admins
    List<SuperAdmin> findByIsActiveTrue();

    // Find inactive super admins
    List<SuperAdmin> findByIsActiveFalse();

    // Find by role level
    List<SuperAdmin> findByRoleLevel(Short roleLevel);

    // Find by role level greater than or equal to
    List<SuperAdmin> findByRoleLevelGreaterThanEqual(Short roleLevel);

    // Find by role level less than or equal to
    List<SuperAdmin> findByRoleLevelLessThanEqual(Short roleLevel);

    // Find active super admins by role level
    @Query("SELECT sa FROM SuperAdmin sa WHERE sa.isActive = true AND sa.roleLevel = :roleLevel")
    List<SuperAdmin> findActiveByRoleLevel(@Param("roleLevel") Short roleLevel);

    // Find all super admins created by a specific admin
    List<SuperAdmin> findByCreatedBy(UUID createdBy);

    // Find active super admins created by a specific admin
    @Query("SELECT sa FROM SuperAdmin sa WHERE sa.createdBy = :createdBy AND sa.isActive = true")
    List<SuperAdmin> findActiveByCreatedBy(@Param("createdBy") UUID createdBy);

    // Check if username exists
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);

    // Check if username exists (case-insensitive)
    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END FROM SuperAdmin sa WHERE LOWER(sa.username) = LOWER(:username)")
    boolean existsByUsernameIgnoreCase(@Param("username") String username);

    // Check if email exists (case-insensitive)
    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END FROM SuperAdmin sa WHERE LOWER(sa.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    // Find by full name containing (search)
    List<SuperAdmin> findByFullNameContainingIgnoreCase(String fullName);

    // Count active super admins
    long countByIsActiveTrue();

    // Count by role level
    long countByRoleLevel(Short roleLevel);

    // Custom query to find super admins with specific permission in JSONB
    @Query(value = "SELECT * FROM super_admin WHERE permissions @> :permission::jsonb AND is_active = true", nativeQuery = true)
    List<SuperAdmin> findByPermission(@Param("permission") String permission);

    // Find all ordered by role level descending (highest privileges first)
    List<SuperAdmin> findAllByOrderByRoleLevelDesc();

    // Find active admins ordered by creation date
    @Query("SELECT sa FROM SuperAdmin sa WHERE sa.isActive = true ORDER BY sa.createdAt DESC")
    List<SuperAdmin> findActiveOrderedByCreatedAt();

    // Advanced search with multiple criteria
    @Query("SELECT sa FROM SuperAdmin sa WHERE " +
            "(:isActive IS NULL OR sa.isActive = :isActive) AND " +
            "(:roleLevel IS NULL OR sa.roleLevel = :roleLevel) AND " +
            "(:createdBy IS NULL OR sa.createdBy = :createdBy)")
    List<SuperAdmin> searchSuperAdmins(
            @Param("isActive") Boolean isActive,
            @Param("roleLevel") Short roleLevel,
            @Param("createdBy") UUID createdBy
    );
}

