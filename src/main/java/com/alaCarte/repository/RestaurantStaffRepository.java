package com.alaCarte.repository;

import com.alaCarte.entity.RestaurantStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for RestaurantStaff entity
 * Spring Data JPA automatically implements this interface
 */
@Repository
public interface RestaurantStaffRepository extends JpaRepository<RestaurantStaff, UUID> {

    // Find all staff by restaurant ID
    List<RestaurantStaff> findByRestaurantId(Long restaurantId);

    // Find active staff by restaurant ID
    List<RestaurantStaff> findByRestaurantIdAndIsActiveTrue(Long restaurantId);

    // Find inactive staff by restaurant ID
    List<RestaurantStaff> findByRestaurantIdAndIsActiveFalse(Long restaurantId);

    // Find by employee code
    Optional<RestaurantStaff> findByRestaurantIdAndEmployeeCode(Long restaurantId, String employeeCode);

    // Find by username
    Optional<RestaurantStaff> findByUserName(String userName);

    // Find by email
    Optional<RestaurantStaff> findByEmail(String email);

    // Find by username or email (for login)
    @Query("SELECT rs FROM RestaurantStaff rs WHERE rs.userName = :usernameOrEmail OR rs.email = :usernameOrEmail")
    Optional<RestaurantStaff> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    // Find by staff role
    List<RestaurantStaff> findByRestaurantIdAndStaffRole(Long restaurantId, String staffRole);

    // Find active staff by role
    @Query("SELECT rs FROM RestaurantStaff rs WHERE rs.restaurantId = :restaurantId AND rs.staffRole = :staffRole AND rs.isActive = true")
    List<RestaurantStaff> findActiveByRestaurantIdAndStaffRole(@Param("restaurantId") Long restaurantId, @Param("staffRole") String staffRole);

    // Find by status
    List<RestaurantStaff> findByRestaurantIdAndStatus(Long restaurantId, String status);

    // Find active staff with specific status
    @Query("SELECT rs FROM RestaurantStaff rs WHERE rs.restaurantId = :restaurantId AND rs.status = :status AND rs.isActive = true")
    List<RestaurantStaff> findActiveByRestaurantIdAndStatus(@Param("restaurantId") Long restaurantId, @Param("status") String status);

    // Find staff reporting to a specific manager
    List<RestaurantStaff> findByReportingTo(UUID reportingTo);

    // Find active staff reporting to a specific manager
    @Query("SELECT rs FROM RestaurantStaff rs WHERE rs.reportingTo = :reportingTo AND rs.isActive = true")
    List<RestaurantStaff> findActiveByReportingTo(@Param("reportingTo") UUID reportingTo);

    // Find by designation
    List<RestaurantStaff> findByRestaurantIdAndDesignationContainingIgnoreCase(Long restaurantId, String designation);

    // Check if employee code exists for a restaurant
    boolean existsByRestaurantIdAndEmployeeCode(Long restaurantId, String employeeCode);

    // Check if username exists
    boolean existsByUserName(String userName);

    // Check if email exists
    boolean existsByEmail(String email);

    // Count staff by restaurant
    long countByRestaurantId(Long restaurantId);

    // Count active staff by restaurant
    long countByRestaurantIdAndIsActiveTrue(Long restaurantId);

    // Count by restaurant and status
    long countByRestaurantIdAndStatus(Long restaurantId, String status);

    // Count by restaurant and role
    long countByRestaurantIdAndStaffRole(Long restaurantId, String staffRole);

    // Find staff who joined within a date range
    @Query("SELECT rs FROM RestaurantStaff rs WHERE rs.restaurantId = :restaurantId AND rs.joinedAt BETWEEN :startDate AND :endDate")
    List<RestaurantStaff> findByJoinedAtBetween(@Param("restaurantId") Long restaurantId, 
                                                 @Param("startDate") OffsetDateTime startDate, 
                                                 @Param("endDate") OffsetDateTime endDate);

    // Find staff who left within a date range
    @Query("SELECT rs FROM RestaurantStaff rs WHERE rs.restaurantId = :restaurantId AND rs.leftAt BETWEEN :startDate AND :endDate")
    List<RestaurantStaff> findByLeftAtBetween(@Param("restaurantId") Long restaurantId, 
                                               @Param("startDate") OffsetDateTime startDate, 
                                               @Param("endDate") OffsetDateTime endDate);

    // Find currently employed staff (joined but not left)
    @Query("SELECT rs FROM RestaurantStaff rs WHERE rs.restaurantId = :restaurantId AND rs.joinedAt IS NOT NULL AND rs.leftAt IS NULL AND rs.isActive = true")
    List<RestaurantStaff> findCurrentlyEmployed(@Param("restaurantId") Long restaurantId);

    // Find soft-deleted staff
    @Query("SELECT rs FROM RestaurantStaff rs WHERE rs.restaurantId = :restaurantId AND rs.deletedAt IS NOT NULL")
    List<RestaurantStaff> findSoftDeleted(@Param("restaurantId") Long restaurantId);

    // Find not soft-deleted staff
    @Query("SELECT rs FROM RestaurantStaff rs WHERE rs.restaurantId = :restaurantId AND rs.deletedAt IS NULL")
    List<RestaurantStaff> findNotSoftDeleted(@Param("restaurantId") Long restaurantId);

    // Custom query to find staff with specific permissions using JSONB
    @Query(value = "SELECT * FROM restaurant_staff WHERE restaurant_id = :restaurantId AND permissions @> :permissionJson::jsonb AND is_active = true", nativeQuery = true)
    List<RestaurantStaff> findByRestaurantIdAndPermissions(@Param("restaurantId") Long restaurantId, @Param("permissionJson") String permissionJson);

    // Advanced search with multiple criteria
    @Query("SELECT rs FROM RestaurantStaff rs WHERE rs.restaurantId = :restaurantId AND " +
           "(:isActive IS NULL OR rs.isActive = :isActive) AND " +
           "(:status IS NULL OR rs.status = :status) AND " +
           "(:staffRole IS NULL OR rs.staffRole = :staffRole) AND " +
           "(CAST(:searchTerm AS string) IS NULL OR " +
           "LOWER(rs.userName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(rs.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(rs.employeeCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<RestaurantStaff> searchStaff(
            @Param("restaurantId") Long restaurantId,
            @Param("isActive") Boolean isActive,
            @Param("status") String status,
            @Param("staffRole") String staffRole,
            @Param("searchTerm") String searchTerm
    );

    // Find all managers (staff with direct reports)
    @Query("SELECT DISTINCT rs FROM RestaurantStaff rs WHERE rs.id IN " +
           "(SELECT DISTINCT rs2.reportingTo FROM RestaurantStaff rs2 WHERE rs2.reportingTo IS NOT NULL AND rs2.restaurantId = :restaurantId)")
    List<RestaurantStaff> findAllManagers(@Param("restaurantId") Long restaurantId);

    // Get staff hierarchy (team members under a manager)
    @Query("SELECT rs FROM RestaurantStaff rs WHERE rs.reportingTo = :managerId AND rs.isActive = true ORDER BY rs.staffRole, rs.userName")
    List<RestaurantStaff> findTeamMembers(@Param("managerId") UUID managerId);
}

