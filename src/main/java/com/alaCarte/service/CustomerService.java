package com.alaCarte.service;

import com.alaCarte.entity.Customer;
import com.alaCarte.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for Customer management
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Create a new customer
     */
    @Transactional
    public Customer createCustomer(Customer customer) {
        // Validation
        if (customer.getPhone() != null && customerRepository.existsByPhone(customer.getPhone())) {
            throw new RuntimeException("Customer with phone '" + customer.getPhone() + "' already exists");
        }
        if (customer.getEmail() != null && customerRepository.existsByEmail(customer.getEmail())) {
            throw new RuntimeException("Customer with email '" + customer.getEmail() + "' already exists");
        }

        return customerRepository.save(customer);
    }

    /**
     * Get customer by ID
     */
    public Optional<Customer> getCustomerById(UUID id) {
        return customerRepository.findById(id);
    }

    /**
     * Get customer by Phone
     */
    public Optional<Customer> getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone);
    }

    /**
     * Get customer by Email
     */
    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    /**
     * Update customer
     */
    @Transactional
    public Customer updateCustomer(UUID id, Customer updatedCustomer) {
        return customerRepository.findById(id)
                .map(existing -> {
                    if (updatedCustomer.getPhone() != null && 
                        !updatedCustomer.getPhone().equals(existing.getPhone()) &&
                        customerRepository.existsByPhone(updatedCustomer.getPhone())) {
                        throw new RuntimeException("Phone number already exists");
                    }
                    if (updatedCustomer.getEmail() != null && 
                        !updatedCustomer.getEmail().equals(existing.getEmail()) &&
                        customerRepository.existsByEmail(updatedCustomer.getEmail())) {
                        throw new RuntimeException("Email already exists");
                    }

                    if (updatedCustomer.getFullName() != null) existing.setFullName(updatedCustomer.getFullName());
                    if (updatedCustomer.getPhone() != null) existing.setPhone(updatedCustomer.getPhone());
                    if (updatedCustomer.getEmail() != null) existing.setEmail(updatedCustomer.getEmail());
                    if (updatedCustomer.getDietaryPref() != null) existing.setDietaryPref(updatedCustomer.getDietaryPref());
                    if (updatedCustomer.getAllergies() != null) existing.setAllergies(updatedCustomer.getAllergies());
                    if (updatedCustomer.getCuisinePreferences() != null) existing.setCuisinePreferences(updatedCustomer.getCuisinePreferences());
                    if (updatedCustomer.getHealthGoals() != null) existing.setHealthGoals(updatedCustomer.getHealthGoals());
                    if (updatedCustomer.getMeta() != null) existing.setMeta(updatedCustomer.getMeta());
                    
                    existing.setUpdatedBy(updatedCustomer.getUpdatedBy());

                    return customerRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    /**
     * Soft delete customer
     */
    @Transactional
    public void deleteCustomer(UUID id) {
        customerRepository.findById(id).ifPresent(customer -> {
            customer.setDeletedAt(OffsetDateTime.now());
            customer.setIsActive(false);
            customerRepository.save(customer);
        });
    }
}
