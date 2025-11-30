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
    
    // Find user by username (WARNING: Not tenant-aware! Use findByTenantIdAndUsername instead)
    Optional<User> findByUsername(String username);

    // Find user by tenant and username (TENANT-AWARE)
    Optional<User> findByTenantIdAndUsername(Integer tenantId, String username);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Find user by tenant and email (TENANT-AWARE)
    Optional<User> findByTenantIdAndEmail(Integer tenantId, String email);
    
    // Find user by phone
    Optional<User> findByPhone(String phone);
    
    // Delete user by tenant and user ID
    void deleteByTenantIdAndUserId(Integer tenantId, Long userId);
    
    // Check if username exists (WARNING: Not tenant-aware! Use existsByTenantIdAndUsername instead)
    boolean existsByUsername(String username);

    // Check if username exists for a specific tenant (TENANT-AWARE)
    boolean existsByTenantIdAndUsername(Integer tenantId, String username);

    // Check if email exists (WARNING: Not tenant-aware! Use existsByTenantIdAndEmail instead)
    boolean existsByEmail(String email);

    // Check if email exists for a specific tenant (TENANT-AWARE)
    boolean existsByTenantIdAndEmail(Integer tenantId, String email);

    // Check if phone exists (WARNING: Not tenant-aware! Use existsByTenantIdAndPhone instead)
    boolean existsByPhone(String phone);

    // Check if phone exists for a specific tenant (TENANT-AWARE)
    boolean existsByTenantIdAndPhone(Integer tenantId, String phone);
}