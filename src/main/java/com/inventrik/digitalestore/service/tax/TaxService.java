package com.inventrik.digitalestore.service.tax;

import com.inventrik.digitalestore.dto.request.TaxRequest;
import com.inventrik.digitalestore.dto.response.TaxResponse;

import java.util.List;

public interface TaxService {
    List<TaxResponse> getAllTaxes(Integer tenantId, String status, String defaultFlag);
    TaxResponse getTax(Integer tenantId, Long taxId);
    TaxResponse createTax(Integer tenantId, TaxRequest request, String username);
    TaxResponse updateTax(Integer tenantId, Long taxId, TaxRequest request, String username);
    void deleteTax(Integer tenantId, Long taxId);
}
