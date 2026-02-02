package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.certificate.UserCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCertificateRepository extends JpaRepository<UserCertificate, String> {

    Optional<UserCertificate> findBySessionId(String sessionId);

    Optional<UserCertificate> findByTenantIdAndUserId(Integer tenantId, String userId);

    @Query("SELECT c FROM UserCertificate c WHERE c.sessionId = :sessionId AND c.status = :status")
    Optional<UserCertificate> findBySessionIdAndStatus(@Param("sessionId") String sessionId, @Param("status") String status);

    @Query("SELECT c FROM UserCertificate c WHERE c.tenantId = :tenantId AND c.userId = :userId AND c.status = :status")
    Optional<UserCertificate> findByTenantIdAndUserIdAndStatus(@Param("tenantId") Integer tenantId, @Param("userId") String userId, @Param("status") String status);

    void deleteBySessionId(String sessionId);

    void deleteByTenantIdAndUserId(Integer tenantId, String userId);

    boolean existsBySessionId(String sessionId);
}
