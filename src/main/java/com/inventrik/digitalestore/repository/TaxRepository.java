package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.tax.Tax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaxRepository extends JpaRepository<Tax, Tax.TaxPK> {
    List<Tax> findByTenantId(Integer tenantId);
    Optional<Tax> findByTenantIdAndTaxId(Integer tenantId, Long taxId);
    List<Tax> findByTenantIdAndStatus(Integer tenantId, String status);
    void deleteByTenantIdAndTaxId(Integer tenantId, Long taxId);
}
