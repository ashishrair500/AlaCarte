package com.alaCarte.repository;

import com.alaCarte.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Customer entity
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}
