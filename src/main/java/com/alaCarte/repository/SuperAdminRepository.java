package com.alaCarte.repository;

import com.alaCarte.entity.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SuperAdmin entity
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

    // Find active admin by username
    @Query("SELECT sa FROM SuperAdmin sa WHERE sa.username = :username AND sa.isActive = true")
    Optional<SuperAdmin> findActiveByUsername(@Param("username") String username);

    // Check if username exists
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);
}
