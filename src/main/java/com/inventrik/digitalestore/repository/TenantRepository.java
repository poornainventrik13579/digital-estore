package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Integer> {
    
    Optional<Tenant> findByDomainName(String domainName);
    Optional<Tenant> findBySubdomain(String subdomain);
    List<Tenant> findByStatus(String status);
    boolean existsByShopEmail(String shopEmail);
    boolean existsByDomainName(String domainName);
    boolean existsBySubdomain(String subdomain);
    List<Tenant> findByCountryRegion(String countryRegion);
}
