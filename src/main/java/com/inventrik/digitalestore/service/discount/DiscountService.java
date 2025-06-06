package com.inventrik.digitalestore.service.discount;

import com.inventrik.digitalestore.dto.request.DiscountCodeRequest;
import com.inventrik.digitalestore.dto.request.ValidateDiscountRequest;
import com.inventrik.digitalestore.dto.response.DiscountCodeResponse;
import com.inventrik.digitalestore.dto.response.DiscountValidationResponse;

import java.math.BigDecimal;
import java.util.List;

public interface DiscountService {
    
    DiscountCodeResponse createDiscountCode(Integer tenantId, DiscountCodeRequest request, String username);
    
    DiscountCodeResponse updateDiscountCode(Integer tenantId, Long discountId, DiscountCodeRequest request, String username);
    
    DiscountCodeResponse getDiscountCode(Integer tenantId, Long discountId);
    
    DiscountCodeResponse getDiscountCodeByCode(Integer tenantId, String code);
    
    List<DiscountCodeResponse> getAllDiscountCodes(Integer tenantId);
    
    List<DiscountCodeResponse> getActiveDiscountCodes(Integer tenantId);
    
    void deleteDiscountCode(Integer tenantId, Long discountId, String username);
    
    DiscountValidationResponse validateDiscountCode(Integer tenantId, ValidateDiscountRequest request);
    
    BigDecimal applyDiscountToOrder(Integer tenantId, String discountCode, Long orderId, Long userId, BigDecimal orderAmount, String username);
    
    void recordDiscountUsage(Integer tenantId, Long discountId, Long orderId, Long userId, BigDecimal discountAmount, String username);
    
    void deactivateExpiredDiscounts(Integer tenantId);
    
    long getDiscountUsageCount(Integer tenantId, Long discountId);
    
    BigDecimal getTotalDiscountAmountUsed(Integer tenantId, Long discountId);
} 