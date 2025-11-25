package com.inventrik.digitalestore.service.tax;

import com.inventrik.digitalestore.domain.tax.Tax;
import com.inventrik.digitalestore.dto.request.TaxRequest;
import com.inventrik.digitalestore.dto.response.TaxResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.TaxRepository;
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
    public List<TaxResponse> getAllTaxes(Integer tenantId) {
        return taxRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TaxResponse getTax(Integer tenantId, Long taxId) {
        Tax tax = taxRepository.findByTenantIdAndTaxId(tenantId, taxId)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found"));
        return mapToDTO(tax);
    }

    @Override
    @Transactional
    public TaxResponse createTax(Integer tenantId, TaxRequest request, String username) {
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
        tax.setCreatedBy(username.length() > 2 ? username.substring(0, 2) : username);
        tax.setUpdatedBy(username.length() > 2 ? username.substring(0, 2) : username);

        Tax saved = taxRepository.save(tax);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public TaxResponse updateTax(Integer tenantId, Long taxId, TaxRequest request, String username) {
        Tax tax = taxRepository.findByTenantIdAndTaxId(tenantId, taxId)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found"));

        tax.setCode(request.getCode());
        tax.setDescription(request.getDescription());
        tax.setValue(request.getValue());
        tax.setDefaultFlag(request.getDefaultFlag());
        tax.setStartDate(request.getStartDate());
        tax.setEndDate(request.getEndDate());
        tax.setUpdatedBy(username.length() > 2 ? username.substring(0, 2) : username);

        Tax updated = taxRepository.save(tax);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteTax(Integer tenantId, Long taxId) {
        if (!taxRepository.findByTenantIdAndTaxId(tenantId, taxId).isPresent()) {
            throw new ResourceNotFoundException("Tax not found");
        }
        taxRepository.deleteByTenantIdAndTaxId(tenantId, taxId);
    }
}
