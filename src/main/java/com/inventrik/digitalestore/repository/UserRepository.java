package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, User.UserPK> {

    Optional<User> findByTenantIdAndUserId(Integer tenantId, String userId);

    List<User> findByTenantIdAndUserIdIn(Integer tenantId, List<String> userIds);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId ORDER BY u.created DESC")
    List<User> findByTenantId(@Param("tenantId") Integer tenantId);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.status = :status ORDER BY u.created DESC")
    List<User> findByTenantIdAndStatus(@Param("tenantId") Integer tenantId, @Param("status") String status);

    Optional<User> findByUsername(String username);

    Optional<User> findByTenantIdAndUsername(Integer tenantId, String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByTenantIdAndEmail(Integer tenantId, String email);

    Optional<User> findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByTenantIdAndUsername(Integer tenantId, String username);

    boolean existsByEmail(String email);

    boolean existsByTenantIdAndEmail(Integer tenantId, String email);

    boolean existsByPhone(String phone);

    boolean existsByTenantIdAndPhone(Integer tenantId, String phone);
}
