package com.inventrik.digitalestore.service.discount;

import com.inventrik.digitalestore.dto.request.DiscountCodeRequest;
import com.inventrik.digitalestore.dto.request.ValidateDiscountRequest;
import com.inventrik.digitalestore.dto.response.DiscountCodeResponse;
import com.inventrik.digitalestore.dto.response.DiscountValidationResponse;

import java.math.BigDecimal;
import java.util.List;

public interface DiscountService {

    DiscountCodeResponse createDiscountCode(Integer tenantId, DiscountCodeRequest request, String username);

    DiscountCodeResponse updateDiscountCode(Integer tenantId, String discountId, DiscountCodeRequest request, String username);

    DiscountCodeResponse getDiscountCode(Integer tenantId, String discountId);

    // Get all discount codes for a tenant with optional filters (code, status)
    List<DiscountCodeResponse> getAllDiscountCodes(Integer tenantId, String code, String status);

    void deleteDiscountCode(Integer tenantId, String discountId, String username);

    DiscountValidationResponse validateDiscountCode(Integer tenantId, ValidateDiscountRequest request);

    BigDecimal applyDiscountToOrder(Integer tenantId, String discountCode, String orderId, String userId, BigDecimal orderAmount, String username);

    void recordDiscountUsage(Integer tenantId, String discountId, String orderId, String userId, BigDecimal discountAmount, String username);

    void deactivateExpiredDiscounts(Integer tenantId);

    long getDiscountUsageCount(Integer tenantId, String discountId);

    BigDecimal getTotalDiscountAmountUsed(Integer tenantId, String discountId);
} 