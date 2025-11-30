package com.inventrik.digitalestore.service.discount;

import com.inventrik.digitalestore.domain.discount.DiscountCode;
import com.inventrik.digitalestore.domain.discount.DiscountUsage;
import com.inventrik.digitalestore.dto.request.DiscountCodeRequest;
import com.inventrik.digitalestore.dto.request.ValidateDiscountRequest;
import com.inventrik.digitalestore.dto.response.DiscountCodeResponse;
import com.inventrik.digitalestore.dto.response.DiscountValidationResponse;
import com.inventrik.digitalestore.exception.ResourceNotFoundException;
import com.inventrik.digitalestore.repository.DiscountCodeRepository;
import com.inventrik.digitalestore.repository.DiscountUsageRepository;
import com.inventrik.digitalestore.repository.TenantRepository;
import com.inventrik.digitalestore.service.IdGeneratorService;
import com.inventrik.digitalestore.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiscountServiceImpl implements DiscountService {
    
    private final DiscountCodeRepository discountCodeRepository;
    private final DiscountUsageRepository discountUsageRepository;
    private final TenantRepository tenantRepository;
    private final IdGeneratorService idGeneratorService;
    private final UserService userService;
    
    @Override
    public DiscountCodeResponse createDiscountCode(Integer tenantId, DiscountCodeRequest request, String username) {
        tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        if (discountCodeRepository.findByTenantIdAndCodeAndStatus(tenantId, request.getCode(), "0").isPresent()) {
            throw new IllegalArgumentException("Discount code already exists: " + request.getCode());
        }
        
        Long newDiscountId = idGeneratorService.generateId(tenantId, "DISCOUNT");
        String auditCode = userService.getAuditCode(username);
        
        DiscountCode discountCode = new DiscountCode();
        discountCode.setTenantId(tenantId);
        discountCode.setDiscountId(newDiscountId);
        discountCode.setCode(request.getCode().toUpperCase());
        discountCode.setDiscountType(request.getDiscountType());
        discountCode.setDiscountValue(request.getDiscountValue());
        discountCode.setMinOrderAmount(request.getMinOrderAmount());
        discountCode.setMaxUses(request.getMaxUses());
        discountCode.setUsedCount(0);
        discountCode.setValidFrom(request.getValidFrom());
        discountCode.setValidTo(request.getValidTo());
        discountCode.setStatus("0");
        discountCode.setCreatedBy(auditCode);
        discountCode.setUpdatedBy(auditCode);
        
        DiscountCode savedDiscountCode = discountCodeRepository.save(discountCode);
        log.info("Created discount code {} for tenant {}", savedDiscountCode.getCode(), tenantId);
        
        return mapToResponse(savedDiscountCode);
    }
    
    @Override
    public DiscountCodeResponse updateDiscountCode(Integer tenantId, Long discountId, DiscountCodeRequest request, String username) {
        DiscountCode discountCode = discountCodeRepository.findById(new DiscountCode.DiscountCodePK(tenantId, discountId))
                .orElseThrow(() -> new ResourceNotFoundException("Discount code not found with id: " + discountId));
        
        if (!discountCode.getCode().equals(request.getCode().toUpperCase())) {
            if (discountCodeRepository.findByTenantIdAndCodeAndStatus(tenantId, request.getCode(), "0").isPresent()) {
                throw new IllegalArgumentException("Discount code already exists: " + request.getCode());
            }
        }
        
        String truncatedUsername = username;
        
        discountCode.setCode(request.getCode().toUpperCase());
        discountCode.setDiscountType(request.getDiscountType());
        discountCode.setDiscountValue(request.getDiscountValue());
        discountCode.setMinOrderAmount(request.getMinOrderAmount());
        discountCode.setMaxUses(request.getMaxUses());
        discountCode.setValidFrom(request.getValidFrom());
        discountCode.setValidTo(request.getValidTo());
        discountCode.setUpdatedBy(truncatedUsername);
        
        DiscountCode updatedDiscountCode = discountCodeRepository.save(discountCode);
        log.info("Updated discount code {} for tenant {}", updatedDiscountCode.getCode(), tenantId);
        
        return mapToResponse(updatedDiscountCode);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DiscountCodeResponse getDiscountCode(Integer tenantId, Long discountId) {
        DiscountCode discountCode = discountCodeRepository.findById(new DiscountCode.DiscountCodePK(tenantId, discountId))
                .orElseThrow(() -> new ResourceNotFoundException("Discount code not found with id: " + discountId));
        return mapToResponse(discountCode);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DiscountCodeResponse> getAllDiscountCodes(Integer tenantId, String code, String status) {
        if (code != null && !code.trim().isEmpty()) {
            return discountCodeRepository.findByTenantIdAndCodeAndStatus(tenantId, code.toUpperCase(), "0").stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        if ("ACTIVE".equalsIgnoreCase(status)) {
            return discountCodeRepository.findActiveDiscountCodes(tenantId, "0", LocalDateTime.now()).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty()) {
            String statusCode = "ACTIVE".equalsIgnoreCase(status) ? "0" : "1";
            return discountCodeRepository.findByTenantIdAndStatus(tenantId, statusCode).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        List<DiscountCodeResponse> activeDiscounts = discountCodeRepository.findByTenantIdAndStatus(tenantId, "0").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<DiscountCodeResponse> inactiveDiscounts = discountCodeRepository.findByTenantIdAndStatus(tenantId, "1").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        activeDiscounts.addAll(inactiveDiscounts);
        return activeDiscounts;
    }
    
    @Override
    public void deleteDiscountCode(Integer tenantId, Long discountId, String username) {
        DiscountCode discountCode = discountCodeRepository.findById(new DiscountCode.DiscountCodePK(tenantId, discountId))
                .orElseThrow(() -> new ResourceNotFoundException("Discount code not found with id: " + discountId));
        
        String truncatedUsername = username;
        discountCode.setStatus("-1");
        discountCode.setUpdatedBy(truncatedUsername);
        
        discountCodeRepository.save(discountCode);
        log.info("Deleted discount code {} for tenant {}", discountCode.getCode(), tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DiscountValidationResponse validateDiscountCode(Integer tenantId, ValidateDiscountRequest request) {
        try {
            DiscountCode discountCode = discountCodeRepository.findValidDiscountCode(
                    tenantId, request.getDiscountCode().toUpperCase(), "0", LocalDateTime.now())
                    .orElse(null);
            
            if (discountCode == null) {
                return DiscountValidationResponse.invalid(request.getDiscountCode(), 
                        "Invalid or expired discount code", "INVALID_OR_EXPIRED");
            }
            
            if (request.getOrderAmount().compareTo(discountCode.getMinOrderAmount()) < 0) {
                return DiscountValidationResponse.invalid(request.getDiscountCode(), 
                        "Order amount must be at least " + discountCode.getMinOrderAmount(), "MIN_AMOUNT_NOT_MET");
            }
            
            BigDecimal discountAmount = discountCode.calculateDiscount(request.getOrderAmount());
            if (discountAmount.compareTo(request.getOrderAmount()) > 0) {
                discountAmount = request.getOrderAmount();
            }
            
            BigDecimal finalAmount = request.getOrderAmount().subtract(discountAmount);
            
            return DiscountValidationResponse.valid(request.getDiscountCode(), discountAmount, finalAmount);
            
        } catch (Exception e) {
            log.error("Error validating discount code: {}", e.getMessage(), e);
            return DiscountValidationResponse.invalid(request.getDiscountCode(), 
                    "Error validating discount code", "VALIDATION_ERROR");
        }
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BigDecimal applyDiscountToOrder(Integer tenantId, String discountCode, Long orderId, Long userId, BigDecimal orderAmount, String username) {
        DiscountCode discount = discountCodeRepository.findValidDiscountCode(
                tenantId, discountCode.toUpperCase(), "0", LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired discount code: " + discountCode));
        
        if (orderAmount.compareTo(discount.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException("Order amount must be at least " + discount.getMinOrderAmount());
        }
        
        // Check if discount has reached max uses limit
        if (discount.getMaxUses() > 0 && discount.getUsedCount() >= discount.getMaxUses()) {
            throw new IllegalArgumentException("Discount code has reached its usage limit");
        }
        
        BigDecimal discountAmount = discount.calculateDiscount(orderAmount);
        if (discountAmount.compareTo(orderAmount) > 0) {
            discountAmount = orderAmount;
        }
        
        recordDiscountUsage(tenantId, discount.getDiscountId(), orderId, userId, discountAmount, username);
        
        int updatedRows = discountCodeRepository.incrementUsedCount(tenantId, discount.getDiscountId(), 
            username);
        
        if (updatedRows == 0) {
            throw new IllegalStateException("Failed to update discount usage - discount may have been modified concurrently");
        }
        
        log.info("Applied discount {} to order {}, amount: {}", discountCode, orderId, discountAmount);
        
        return discountAmount;
    }
    
    @Override
    public void recordDiscountUsage(Integer tenantId, Long discountId, Long orderId, Long userId, BigDecimal discountAmount, String username) {
        Long newUsageId = idGeneratorService.generateId(tenantId, "DISCOUNT_USAGE");
        String auditCode = userService.getAuditCode(username);
        
        DiscountUsage usage = new DiscountUsage();
        usage.setTenantId(tenantId);
        usage.setUsageId(newUsageId);
        usage.setDiscountId(discountId);
        usage.setOrderId(orderId);
        usage.setUserId(userId);
        usage.setDiscountAmount(discountAmount);
        usage.setUsedDate(LocalDateTime.now());
        usage.setStatus("0");
        usage.setCreatedBy(auditCode);
        usage.setUpdatedBy(auditCode);
        
        discountUsageRepository.save(usage);
        log.info("Recorded discount usage for discount {} in order {}", discountId, orderId);
    }
    
    @Override
    public void deactivateExpiredDiscounts(Integer tenantId) {
        List<DiscountCode> expiredDiscounts = discountCodeRepository.findExpiredDiscountCodes(tenantId, LocalDateTime.now(), "0");
        
        for (DiscountCode discount : expiredDiscounts) {
            discount.setStatus("-1");
            discount.setUpdatedBy("sy");
            discountCodeRepository.save(discount);
        }
        
        if (!expiredDiscounts.isEmpty()) {
            log.info("Deactivated {} expired discount codes for tenant {}", expiredDiscounts.size(), tenantId);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getDiscountUsageCount(Integer tenantId, Long discountId) {
        return discountUsageRepository.countUsageByDiscountId(tenantId, discountId, "0");
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalDiscountAmountUsed(Integer tenantId, Long discountId) {
        Double total = discountUsageRepository.getTotalDiscountAmountUsed(tenantId, discountId, "0");
        return total != null ? BigDecimal.valueOf(total) : BigDecimal.ZERO;
    }
    
    private DiscountCodeResponse mapToResponse(DiscountCode discountCode) {
        DiscountCodeResponse response = new DiscountCodeResponse();
        response.setDiscountId(discountCode.getDiscountId());
        response.setTenantId(discountCode.getTenantId());
        response.setCode(discountCode.getCode());
        response.setDiscountType(discountCode.getDiscountType());
        response.setDiscountValue(discountCode.getDiscountValue());
        response.setMinOrderAmount(discountCode.getMinOrderAmount());
        response.setMaxUses(discountCode.getMaxUses());
        response.setUsedCount(discountCode.getUsedCount());
        response.setValidFrom(discountCode.getValidFrom());
        response.setValidTo(discountCode.getValidTo());
        response.setStatus(discountCode.getStatus());
        response.setCreated(discountCode.getCreated());
        response.setUpdated(discountCode.getUpdated());
        response.setActive(discountCode.isActive());
        response.setValid(discountCode.isValid());
        response.setHasUsesRemaining(discountCode.hasUsesRemaining());
        response.setRemainingUses(discountCode.getMaxUses() == 0 ? Integer.MAX_VALUE : 
                                 Math.max(0, discountCode.getMaxUses() - discountCode.getUsedCount()));
        return response;
    }
} 