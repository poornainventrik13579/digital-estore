package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.domain.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, User.UserPK> {
    
    // ================================
    // TENANT-SCOPED USER QUERIES
    // ================================
    
    // Find user by tenant and user ID
    Optional<User> findByTenantIdAndUserId(Integer tenantId, Long userId);
    
    // Find user by tenant and username (TENANT-SCOPED)
    Optional<User> findByTenantIdAndUsername(Integer tenantId, String username);
    
    // Find user by tenant and email (TENANT-SCOPED)
    Optional<User> findByTenantIdAndEmail(Integer tenantId, String email);
    
    // Find user by tenant and phone (TENANT-SCOPED)
    Optional<User> findByTenantIdAndPhone(Integer tenantId, String phone);
    
    // Find all users for a tenant
    List<User> findByTenantId(Integer tenantId);
    
    // Find active users for a tenant
    List<User> findByTenantIdAndStatus(Integer tenantId, String status);
    
    // Find users by tenant and role
    List<User> findByTenantIdAndUserRole(Integer tenantId, UserRole userRole);
    
    // Find tenant admins for a tenant
    List<User> findByTenantIdAndUserRoleAndStatus(Integer tenantId, UserRole userRole, String status);
    
    // ================================
    // TENANT-SCOPED EXISTENCE CHECKS
    // ================================
    
    // Check if username exists in tenant
    boolean existsByTenantIdAndUsername(Integer tenantId, String username);
    
    // Check if email exists in tenant
    boolean existsByTenantIdAndEmail(Integer tenantId, String email);
    
    // Check if phone exists in tenant
    boolean existsByTenantIdAndPhone(Integer tenantId, String phone);
    
    // ================================
    // LEGACY METHODS (AVOID USING - NOT TENANT-SCOPED)
    // ================================
    
    // Find user by username (NOT TENANT-SCOPED - use for system admin only)
    Optional<User> findByUsername(String username);
    
    // Find user by email (NOT TENANT-SCOPED - use for system admin only)
    Optional<User> findByEmail(String email);
    
    // Find user by phone (NOT TENANT-SCOPED - use for system admin only)
    Optional<User> findByPhone(String phone);
    
    // Check if username exists (NOT TENANT-SCOPED - use for system admin only)
    boolean existsByUsername(String username);
    
    // Check if email exists (NOT TENANT-SCOPED - use for system admin only)
    boolean existsByEmail(String email);
    
    // Check if phone exists (NOT TENANT-SCOPED - use for system admin only)
    boolean existsByPhone(String phone);
    
    // ================================
    // CUSTOM QUERIES
    // ================================
    
    // Delete user by composite key
    @Modifying
    @Query("DELETE FROM User u WHERE u.tenantId = :tenantId AND u.userId = :userId")
    void deleteByTenantIdAndUserId(@Param("tenantId") Integer tenantId, @Param("userId") Long userId);
    
    // Count users in tenant
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") Integer tenantId);
    
    // Count active users in tenant
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.status = 'A'")
    long countActiveUsersByTenantId(@Param("tenantId") Integer tenantId);
}