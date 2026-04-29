package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.certificate.CertificateStatus;
import com.inventrik.digitalestore.domain.certificate.UserCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCertificateRepository extends JpaRepository<UserCertificate, String> {

    Optional<UserCertificate> findBySessionId(String sessionId);

    Optional<UserCertificate> findFirstByTenantIdAndUserIdOrderByUpdatedDesc(Integer tenantId, String userId);

    boolean existsBySessionId(String sessionId);

    // Status-filtered finders (for active certificate lookups)
    Optional<UserCertificate> findBySessionIdAndStatus(String sessionId, CertificateStatus status);

    Optional<UserCertificate> findFirstByTenantIdAndUserIdAndStatusOrderByUpdatedDesc(Integer tenantId, String userId, CertificateStatus status);

    List<UserCertificate> findAllByTenantIdAndUserIdAndStatus(Integer tenantId, String userId, CertificateStatus status);

    // Soft-delete: revoke by session ID
    @Modifying
    @Query("UPDATE UserCertificate c SET c.status = :newStatus WHERE c.sessionId = :sessionId AND c.status = :currentStatus")
    int revokeBySessionId(@Param("sessionId") String sessionId,
                          @Param("newStatus") CertificateStatus newStatus,
                          @Param("currentStatus") CertificateStatus currentStatus);

    // Soft-delete: revoke all active certs for a user in a tenant
    @Modifying
    @Query("UPDATE UserCertificate c SET c.status = :newStatus WHERE c.tenantId = :tenantId AND c.userId = :userId AND c.status = :currentStatus")
    int revokeByTenantIdAndUserId(@Param("tenantId") Integer tenantId,
                                  @Param("userId") String userId,
                                  @Param("newStatus") CertificateStatus newStatus,
                                  @Param("currentStatus") CertificateStatus currentStatus);

    @Modifying
    @Query("UPDATE UserCertificate c SET c.status = :newStatus, c.updated = CURRENT_TIMESTAMP " +
            "WHERE c.sessionId = :sessionId AND c.status = :currentStatus")
    int reactivateBySessionId(@Param("sessionId") String sessionId,
                              @Param("newStatus") CertificateStatus newStatus,
                              @Param("currentStatus") CertificateStatus currentStatus);

    @Modifying
    @Query("UPDATE UserCertificate c SET c.status = :newStatus " +
            "WHERE c.tenantId = :tenantId AND c.userId = :userId " +
            "AND c.sessionId <> :currentSessionId AND c.status = :currentStatus")
    int revokeByTenantIdAndUserIdExcludingSession(@Param("tenantId") Integer tenantId,
                                                  @Param("userId") String userId,
                                                  @Param("currentSessionId") String currentSessionId,
                                                  @Param("newStatus") CertificateStatus newStatus,
                                                  @Param("currentStatus") CertificateStatus currentStatus);
}
