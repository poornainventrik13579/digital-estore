package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.certificate.UserCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCertificateRepository extends JpaRepository<UserCertificate, String> {

    Optional<UserCertificate> findBySessionId(String sessionId);

    Optional<UserCertificate> findByTenantIdAndUserId(Integer tenantId, String userId);

    void deleteBySessionId(String sessionId);

    void deleteByTenantIdAndUserId(Integer tenantId, String userId);

    boolean existsBySessionId(String sessionId);
}
