package com.inventrik.digitalestore.service.tax;

import com.inventrik.digitalestore.domain.tax.Tax;
import com.inventrik.digitalestore.dto.request.TaxRequest;
import com.inventrik.digitalestore.dto.response.TaxResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.TaxRepository;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaxServiceImpl implements TaxService {

    private final TaxRepository taxRepository;
    private final TenantRepository tenantRepository;
    private final IdGeneratorService idGeneratorService;

    private TaxResponse mapToDTO(Tax tax) {
        return new TaxResponse(
            tax.getTenantId(),
            tax.getTaxId(),
            tax.getCode(),
            tax.getDescription(),
            tax.getValue(),
            tax.getDefaultFlag(),
            tax.getStartDate(),
            tax.getEndDate(),
            tax.getStatus(),
            tax.getCreated(),
            tax.getUpdated()
        );
    }

    @Override
    public List<TaxResponse> getAllTaxes(Integer tenantId, String status, String defaultFlag) {
        List<TaxResponse> taxes = taxRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        if (status != null) {
            taxes = taxes.stream()
                    .filter(tax -> status.equals(tax.getStatus()))
                    .collect(Collectors.toList());
        }
        if (defaultFlag != null) {
            taxes = taxes.stream()
                    .filter(tax -> defaultFlag.equals(tax.getDefaultFlag()))
                    .collect(Collectors.toList());
        }

        return taxes;
    }

    @Override
    public TaxResponse getTax(Integer tenantId, String taxId) {
        Tax tax = taxRepository.findByTenantIdAndTaxId(tenantId, taxId)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found"));
        return mapToDTO(tax);
    }

    @Override
    @Transactional
    public TaxResponse createTax(Integer tenantId, TaxRequest request, String username) {
        tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        Tax tax = new Tax();
        tax.setTenantId(tenantId);
        tax.setTaxId(idGeneratorService.generateId(tenantId, "TAX"));
        tax.setCode(request.getCode());
        tax.setDescription(request.getDescription());
        tax.setValue(request.getValue());
        tax.setDefaultFlag(request.getDefaultFlag() != null ? request.getDefaultFlag() : "N");
        tax.setStartDate(request.getStartDate());
        tax.setEndDate(request.getEndDate());
        tax.setStatus("0");
        tax.setCreatedBy(username.substring(0, Math.min(2, username.length())));
        tax.setUpdatedBy(username.substring(0, Math.min(2, username.length())));

        Tax saved = taxRepository.save(tax);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public TaxResponse updateTax(Integer tenantId, String taxId, TaxRequest request, String username) {
        Tax tax = taxRepository.findByTenantIdAndTaxId(tenantId, taxId)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found"));

        tax.setCode(request.getCode());
        tax.setDescription(request.getDescription());
        tax.setValue(request.getValue());
        tax.setDefaultFlag(request.getDefaultFlag());
        tax.setStartDate(request.getStartDate());
        tax.setEndDate(request.getEndDate());
        tax.setUpdatedBy(username.substring(0, Math.min(2, username.length())));

        Tax updated = taxRepository.save(tax);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteTax(Integer tenantId, String taxId) {
        if (!taxRepository.findByTenantIdAndTaxId(tenantId, taxId).isPresent()) {
            throw new ResourceNotFoundException("Tax not found");
        }
        taxRepository.deleteByTenantIdAndTaxId(tenantId, taxId);
    }
}
