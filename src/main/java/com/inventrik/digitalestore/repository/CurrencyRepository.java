package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.currency.Currency;
import com.inventrik.digitalestore.domain.currency.CurrencyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, CurrencyId> {
    
    List<Currency> findByTenantIdAndStatus(Integer tenantId, String status);
    
    Optional<Currency> findByTenantIdAndCurrencyCodeAndStatus(Integer tenantId, String currencyCode, String status);
    
    @Query("SELECT c FROM Currency c WHERE c.tenantId = ?1 AND c.isDefault = '1' AND c.status = '0'")
    Optional<Currency> findDefaultCurrency(Integer tenantId);
    
    List<Currency> findByTenantId(Integer tenantId);
} 