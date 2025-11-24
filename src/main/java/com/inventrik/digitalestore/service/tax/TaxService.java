package com.inventrik.digitalestore.service.tax;

import com.inventrik.digitalestore.dto.request.TaxRequest;
import com.inventrik.digitalestore.dto.request.TaxUpdateRequest;
import com.inventrik.digitalestore.dto.response.TaxCalculationResponse;
import com.inventrik.digitalestore.dto.response.TaxResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TaxService {
    
    List<TaxResponse> getAllTaxes();
    TaxResponse getTax(Integer tenantId, Integer taxId);
    List<TaxResponse> getTaxesByTenant(Integer tenantId);
    List<TaxResponse> getActiveTaxesByTenant(Integer tenantId);
    TaxResponse getDefaultTaxByTenant(Integer tenantId);
    List<TaxResponse> getValidTaxesForDate(Integer tenantId, LocalDate date);
    TaxResponse getValidDefaultTaxForDate(Integer tenantId, LocalDate date);
    TaxResponse createTax(String username, TaxRequest taxRequest);
    TaxResponse updateTax(Integer tenantId, Integer taxId, String username, TaxUpdateRequest updateRequest);
    void deleteTax(Integer tenantId, Integer taxId);
    TaxResponse setAsDefaultTax(Integer tenantId, Integer taxId, String username);
    List<TaxResponse> searchTaxes(Integer tenantId, String keyword);
    TaxCalculationResponse calculateTax(Integer tenantId, BigDecimal baseAmount);
    TaxCalculationResponse calculateTaxForDate(Integer tenantId, BigDecimal baseAmount, LocalDate date);
    BigDecimal calculateDefaultTaxAmount(Integer tenantId, BigDecimal baseAmount);
    boolean existsByTenantAndCode(Integer tenantId, String code);
    long countActiveTaxesByTenant(Integer tenantId);
}
