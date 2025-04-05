package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find user by tenant and user ID
    Optional<User> findByTenantIdAndUserId(Integer tenantId, Long userId);
    
    // Find all users for a tenant
    List<User> findByTenantId(Integer tenantId);
    
    // Find active users for a tenant
    List<User> findByTenantIdAndStatus(Integer tenantId, String status);
    
    // Find user by username
    Optional<User> findByUsername(String username);
    
    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Find user by phone
    Optional<User> findByPhone(String phone);
    
    // Delete user by tenant and user ID
    void deleteByTenantIdAndUserId(Integer tenantId, Long userId);
    
    // Check if username exists
    boolean existsByUsername(String username);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Check if phone exists
    boolean existsByPhone(String phone);
}