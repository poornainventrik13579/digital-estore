package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Integer> {
    Optional<Tenant> findByTenantId(Integer tenantId);
    Optional<Tenant> findByShopEmail(String shopEmail);
    Optional<Tenant> findBySubdomain(String subdomain);
    boolean existsByTenantId(Integer tenantId);
    boolean existsByShopEmail(String shopEmail);
    boolean existsBySubdomain(String subdomain);
}
