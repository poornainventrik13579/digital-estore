package com.inventrik.digitalestore.service.tax;

import com.inventrik.digitalestore.domain.tax.Tax;
import com.inventrik.digitalestore.dto.request.TaxRequest;
import com.inventrik.digitalestore.dto.request.TaxUpdateRequest;
import com.inventrik.digitalestore.dto.response.TaxCalculationResponse;
import com.inventrik.digitalestore.dto.response.TaxResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.TaxRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaxServiceImpl implements TaxService {
    
    private final TaxRepository taxRepository;
    private final IdGeneratorService idGeneratorService;
    
    private TaxResponse mapToDTO(Tax tax) {
        TaxResponse response = new TaxResponse();
        response.setId(tax.getId());
        response.setTenantId(tax.getTenantId());
        response.setCode(tax.getCode());
        response.setDescription(tax.getDescription());
        response.setValue(tax.getValue());
        response.setDefaultFlag(tax.getDefaultFlag());
        response.setIsDefault("Y".equals(tax.getDefaultFlag()));
        response.setStartDate(tax.getStartDate());
        response.setEndDate(tax.getEndDate());
        response.setStatus(tax.getStatus());
        response.setIsActive("A".equals(tax.getStatus()));
        response.setIsCurrentlyValid(tax.isCurrentlyValid());
        response.setModified(tax.getModified());
        response.setModifiedBy(tax.getModifiedBy());
        response.setCreatedBy(tax.getCreatedBy());
        response.setCreated(tax.getCreated());
        response.setUpdatedBy(tax.getUpdatedBy());
        response.setUpdated(tax.getUpdated());
        return response;
    }
    
    private Tax mapToEntity(TaxRequest request, String username) {
        Tax tax = new Tax();
        tax.setId(idGeneratorService.generateTenantId());
        tax.setTenantId(request.getTenantId());
        tax.setCode(request.getCode());
        tax.setDescription(request.getDescription());
        tax.setValue(request.getValue());
        tax.setDefaultFlag(request.getDefaultFlag());
        tax.setStartDate(request.getStartDate());
        tax.setEndDate(request.getEndDate());
        tax.setStatus(request.getStatus());
        tax.setCreatedBy(username);
        tax.setUpdatedBy(username);
        tax.setModifiedBy(username);
        return tax;
    }
    
    private void updateEntityFromRequest(Tax tax, TaxUpdateRequest request, String username) {
        if (request.getCode() != null) {
            tax.setCode(request.getCode());
        }
        if (request.getDescription() != null) {
            tax.setDescription(request.getDescription());
        }
        if (request.getValue() != null) {
            tax.setValue(request.getValue());
        }
        if (request.getDefaultFlag() != null) {
            tax.setDefaultFlag(request.getDefaultFlag());
        }
        if (request.getStartDate() != null) {
            tax.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            tax.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            tax.setStatus(request.getStatus());
        }
        tax.setUpdatedBy(username);
        tax.setModifiedBy(username);
        tax.setUpdated(LocalDateTime.now());
        tax.setModified(LocalDateTime.now());
    }
    
    @Override
    public List<TaxResponse> getAllTaxes() {
        return taxRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public TaxResponse getTax(Integer tenantId, Integer taxId) {
        Tax tax = taxRepository.findByTenantIdAndId(tenantId, taxId)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found with tenant id: " + tenantId + " and tax id: " + taxId));
        return mapToDTO(tax);
    }
    
    @Override
    public List<TaxResponse> getTaxesByTenant(Integer tenantId) {
        return taxRepository.findByTenantId(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TaxResponse> getActiveTaxesByTenant(Integer tenantId) {
        return taxRepository.findActiveTaxesByTenant(tenantId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public TaxResponse getDefaultTaxByTenant(Integer tenantId) {
        Tax tax = taxRepository.findDefaultTaxByTenant(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Default tax not found for tenant: " + tenantId));
        return mapToDTO(tax);
    }
    
    @Override
    public List<TaxResponse> getValidTaxesForDate(Integer tenantId, LocalDate date) {
        return taxRepository.findValidTaxesForDate(tenantId, date).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public TaxResponse getValidDefaultTaxForDate(Integer tenantId, LocalDate date) {
        Tax tax = taxRepository.findValidDefaultTaxForDate(tenantId, date)
                .orElseThrow(() -> new ResourceNotFoundException("Valid default tax not found for tenant: " + tenantId + " and date: " + date));
        return mapToDTO(tax);
    }
    
    @Override
    @Transactional
    public TaxResponse createTax(String username, TaxRequest taxRequest) {
        if ("Y".equals(taxRequest.getDefaultFlag())) {
            taxRepository.clearDefaultFlags(taxRequest.getTenantId(), username);
        }
        
        Tax tax = mapToEntity(taxRequest, username);
        Tax savedTax = taxRepository.save(tax);
        return mapToDTO(savedTax);
    }
    
    @Override
    @Transactional
    public TaxResponse updateTax(Integer tenantId, Integer taxId, String username, TaxUpdateRequest updateRequest) {
        Tax tax = taxRepository.findByTenantIdAndId(tenantId, taxId)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found with tenant id: " + tenantId + " and tax id: " + taxId));
        
        if ("Y".equals(updateRequest.getDefaultFlag()) && !"Y".equals(tax.getDefaultFlag())) {
            taxRepository.clearDefaultFlags(tenantId, username);
        }
        
        updateEntityFromRequest(tax, updateRequest, username);
        Tax updatedTax = taxRepository.save(tax);
        return mapToDTO(updatedTax);
    }
    
    @Override
    @Transactional
    public void deleteTax(Integer tenantId, Integer taxId) {
        Tax tax = taxRepository.findByTenantIdAndId(tenantId, taxId)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found with tenant id: " + tenantId + " and tax id: " + taxId));
        taxRepository.delete(tax);
    }
    
    @Override
    @Transactional
    public TaxResponse setAsDefaultTax(Integer tenantId, Integer taxId, String username) {
        Tax tax = taxRepository.findByTenantIdAndId(tenantId, taxId)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found with tenant id: " + tenantId + " and tax id: " + taxId));
        
        taxRepository.clearDefaultFlags(tenantId, username);
        
        tax.setAsDefault();
        tax.setModifiedBy(username);
        tax.setUpdatedBy(username);
        tax.setModified(LocalDateTime.now());
        tax.setUpdated(LocalDateTime.now());
        
        Tax updatedTax = taxRepository.save(tax);
        return mapToDTO(updatedTax);
    }
    
    @Override
    public List<TaxResponse> searchTaxes(Integer tenantId, String keyword) {
        return taxRepository.searchTaxes(tenantId, keyword).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public TaxCalculationResponse calculateTax(Integer tenantId, BigDecimal baseAmount) {
        return calculateTaxForDate(tenantId, baseAmount, LocalDate.now());
    }
    
    @Override
    public TaxCalculationResponse calculateTaxForDate(Integer tenantId, BigDecimal baseAmount, LocalDate date) {
        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return new TaxCalculationResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new ArrayList<>());
        }
        
        List<Tax> validTaxes = taxRepository.findValidTaxesForDate(tenantId, date);
        List<TaxCalculationResponse.TaxLineItem> taxBreakdown = new ArrayList<>();
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        
        for (Tax tax : validTaxes) {
            BigDecimal taxAmount = tax.calculateTaxAmount(baseAmount);
            if (taxAmount.compareTo(BigDecimal.ZERO) > 0) {
                taxBreakdown.add(new TaxCalculationResponse.TaxLineItem(
                    tax.getCode(),
                    tax.getDescription(),
                    tax.getValue(),
                    taxAmount.setScale(2, RoundingMode.HALF_UP),
                    tax.isDefault()
                ));
                totalTaxAmount = totalTaxAmount.add(taxAmount);
            }
        }
        
        BigDecimal finalAmount = baseAmount.add(totalTaxAmount);
        
        return new TaxCalculationResponse(
            baseAmount.setScale(2, RoundingMode.HALF_UP),
            totalTaxAmount.setScale(2, RoundingMode.HALF_UP),
            finalAmount.setScale(2, RoundingMode.HALF_UP),
            taxBreakdown
        );
    }
    
    @Override
    public BigDecimal calculateDefaultTaxAmount(Integer tenantId, BigDecimal baseAmount) {
        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        try {
            Tax defaultTax = taxRepository.findValidDefaultTaxForDate(tenantId, LocalDate.now())
                    .orElse(null);
            
            if (defaultTax == null) {
                return BigDecimal.ZERO;
            }
            
            return defaultTax.calculateTaxAmount(baseAmount).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    
    @Override
    public boolean existsByTenantAndCode(Integer tenantId, String code) {
        return taxRepository.existsByTenantIdAndCode(tenantId, code);
    }
    
    @Override
    public long countActiveTaxesByTenant(Integer tenantId) {
        return taxRepository.countActiveTaxesByTenant(tenantId);
    }
}
