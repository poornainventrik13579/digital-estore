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
    
    Optional<User> findByTenantIdAndUserId(Integer tenantId, Long userId);
    
    Optional<User> findByTenantIdAndUsername(Integer tenantId, String username);
    
    Optional<User> findByTenantIdAndEmail(Integer tenantId, String email);
    
    Optional<User> findByTenantIdAndPhone(Integer tenantId, String phone);
    
    List<User> findByTenantId(Integer tenantId);
    
    List<User> findByTenantIdAndStatus(Integer tenantId, String status);
    
    List<User> findByTenantIdAndUserRole(Integer tenantId, UserRole userRole);
    
    List<User> findByTenantIdAndUserRoleAndStatus(Integer tenantId, UserRole userRole, String status);
    
    boolean existsByTenantIdAndUsername(Integer tenantId, String username);
    
    boolean existsByTenantIdAndEmail(Integer tenantId, String email);
    
    boolean existsByTenantIdAndPhone(Integer tenantId, String phone);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhone(String phone);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    @Modifying
    @Query("DELETE FROM User u WHERE u.tenantId = :tenantId AND u.userId = :userId")
    void deleteByTenantIdAndUserId(@Param("tenantId") Integer tenantId, @Param("userId") Long userId);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") Integer tenantId);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.status = 'A'")
    long countActiveUsersByTenantId(@Param("tenantId") Integer tenantId);
}